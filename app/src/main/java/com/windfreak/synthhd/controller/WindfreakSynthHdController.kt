package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ChannelState
import com.windfreak.synthhd.domain.DeviceStatus
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.TriggerSource
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.domain.ValidationResult
import com.windfreak.synthhd.domain.validateFrequencyMhz
import com.windfreak.synthhd.domain.validatePhaseDegrees
import com.windfreak.synthhd.domain.validatePowerDbm

class WindfreakSynthHdController(
    private val transport: WindfreakSerialTransport,
    initialState: SynthDeviceState = SynthDeviceState(),
) : SynthHdController {
    override var state: SynthDeviceState = initialState.copy(
        status = initialState.status.copy(connectedLabel = "USB hardware"),
    )
        private set

    override fun replaceState(state: SynthDeviceState) {
        this.state = state.copy(status = this.state.status)
        uploadGeneratorState(ChannelId.A, this.state.channelA)
        uploadGeneratorState(ChannelId.B, this.state.channelB)
        setReferenceMode(this.state.status.referenceMode)
        setSweep(this.state.sweep)
        setModulation(this.state.modulation)
        setTrigger(this.state.trigger)
    }

    override fun selectChannel(channelId: ChannelId) {
        transport.writePacket(WindfreakSynthHdProtocol.selectChannel(channelId))
        state = state.copy(activeChannel = channelId)
    }

    override fun setFrequencyMhz(value: Double): ValidationResult =
        applyChannelValidation(validateFrequencyMhz(value)) { channelId, channel ->
            transport.writePacket(WindfreakSynthHdProtocol.setFrequency(channelId, value))
            channel.copy(frequencyMhz = value)
        }

    override fun setPowerDbm(value: Double): ValidationResult =
        applyChannelValidation(validatePowerDbm(value)) { channelId, channel ->
            transport.writePacket(WindfreakSynthHdProtocol.setPower(channelId, value))
            channel.copy(powerDbm = value)
        }

    override fun setPhaseDegrees(value: Double): ValidationResult =
        applyChannelValidation(validatePhaseDegrees(value)) { channelId, channel ->
            transport.writePacket(WindfreakSynthHdProtocol.setPhase(channelId, value))
            channel.copy(phaseDegrees = value)
        }

    override fun setRfEnabled(enabled: Boolean) {
        val channelId = state.activeChannel
        transport.writePacket(WindfreakSynthHdProtocol.setRfEnabled(channelId, enabled))
        updateActiveChannel { it.copy(rfEnabled = enabled) }
    }

    override fun setChannelLocked(locked: Boolean) {
        updateActiveChannel { it.copy(locked = locked) }
    }

    override fun setReferenceMode(referenceMode: ReferenceMode) {
        transport.writePacket(WindfreakSynthHdProtocol.setReference(referenceMode))
        state = state.copy(status = state.status.copy(referenceMode = referenceMode))
    }

    override fun setSweep(sweep: SweepState): ValidationResult {
        val activePower = activeChannel.powerDbm
        transport.writePacket(WindfreakSynthHdProtocol.configureLinearSweep(state.activeChannel, sweep, activePower))
        state = state.copy(sweep = sweep)
        return ValidationResult(true)
    }

    override fun startSweep() {
        transport.writePacket(WindfreakSynthHdProtocol.startContinuousSweep(state.activeChannel))
        state = state.copy(
            sweep = state.sweep.copy(runMode = RunMode.Running),
            trigger = state.trigger.copy(mode = RunMode.Idle),
        )
    }

    override fun stopSweep() {
        transport.writePacket(WindfreakSynthHdProtocol.stopSweep(state.activeChannel))
        state = state.copy(
            sweep = state.sweep.copy(runMode = RunMode.Idle),
            trigger = state.trigger.copy(mode = RunMode.Idle),
        )
    }

    override fun addHopPoint(point: HopPoint): ValidationResult {
        state = state.copy(hopList = state.hopList + point)
        return ValidationResult(true)
    }

    override fun updateHopPoint(index: Int, point: HopPoint): ValidationResult {
        if (index !in state.hopList.indices) return ValidationResult(false, "List point does not exist.")
        state = state.copy(hopList = state.hopList.toMutableList().also { it[index] = point })
        return ValidationResult(true)
    }

    override fun moveHopPoint(index: Int, offset: Int) {
        val nextIndex = index + offset
        if (index !in state.hopList.indices || nextIndex !in state.hopList.indices) return
        state = state.copy(
            hopList = state.hopList.toMutableList().also {
                val point = it.removeAt(index)
                it.add(nextIndex, point)
            },
        )
    }

    override fun removeHopPoint(index: Int) {
        if (index !in state.hopList.indices) return
        state = state.copy(hopList = state.hopList.toMutableList().also { it.removeAt(index) })
    }

    override fun clearHopList() {
        state = state.copy(hopList = emptyList(), listRunMode = RunMode.Idle)
    }

    override fun startHopList() {
        if (state.hopList.isEmpty()) return
        transport.writePacket(WindfreakSynthHdProtocol.startContinuousSweep(state.activeChannel))
        state = state.copy(listRunMode = RunMode.Running)
    }

    override fun stopHopList() {
        transport.writePacket(WindfreakSynthHdProtocol.stopSweep(state.activeChannel))
        state = state.copy(listRunMode = RunMode.Idle)
    }

    override fun setModulation(modulation: ModulationState): ValidationResult {
        transport.writePacket(
            WindfreakSynthHdProtocol.setPulse(
                channel = state.activeChannel,
                pulseWidthUs = modulation.pulseWidthUs,
                enabled = modulation.pulseEnabled,
            ),
        )
        transport.writePacket(WindfreakSynthHdProtocol.setAmEnabled(modulation.amEnabled))
        transport.writePacket(
            WindfreakSynthHdProtocol.setFm(
                channel = state.activeChannel,
                deviationKhz = modulation.fmDeviationKhz,
                chirp = modulation.chirpEnabled,
                enabled = modulation.fmEnabled,
            ),
        )
        state = state.copy(modulation = modulation)
        return ValidationResult(true)
    }

    override fun setTrigger(trigger: TriggerState) {
        val packet = if (trigger.source == TriggerSource.External && trigger.mode == RunMode.Armed) {
            WindfreakSynthHdProtocol.armExternalSweepTrigger(state.activeChannel)
        } else {
            WindfreakSynthHdProtocol.disableTrigger(state.activeChannel)
        }
        transport.writePacket(packet)
        state = state.copy(
            trigger = trigger,
            sweep = if (trigger.mode == RunMode.Armed) state.sweep.copy(runMode = RunMode.Armed) else state.sweep,
        )
    }

    override fun softwareTrigger() {
        transport.writePacket(WindfreakSynthHdProtocol.startSingleSweep(state.activeChannel))
        state = state.copy(
            sweep = state.sweep.copy(runMode = RunMode.Complete),
            trigger = state.trigger.copy(mode = RunMode.Complete),
        )
    }

    override fun saveToDevice() {
        transport.writePacket(WindfreakSynthHdProtocol.saveToEeprom())
        state = state.copy(savedSnapshot = state.activeChannel)
    }

    override fun resetToDefaults() {
        replaceState(SynthDeviceState(status = state.status))
    }

    fun refreshStatus() {
        val model = transport.query(WindfreakSynthHdProtocol.queryModel()).ifBlank { state.status.model }
        val serial = transport.query(WindfreakSynthHdProtocol.querySerial()).ifBlank { state.status.serial }
        val firmware = transport.query(WindfreakSynthHdProtocol.queryFirmware()).ifBlank { state.status.firmware }
        val temperature = transport.query(WindfreakSynthHdProtocol.queryTemperature()).toDoubleOrNull()
            ?: state.status.temperatureC
        val locked = transport.query(WindfreakSynthHdProtocol.queryPhaseLock(state.activeChannel)).trim() == "1"
        state = state.copy(
            status = DeviceStatus(
                connectedLabel = "USB hardware",
                model = model.trim(),
                serial = serial.trim(),
                firmware = firmware.trim(),
                calibrationDate = state.status.calibrationDate,
                temperatureC = temperature,
                referenceMode = state.status.referenceMode,
                lockDetect = locked,
                levelOk = state.status.levelOk,
            ),
        )
    }

    fun close() {
        transport.close()
    }

    private fun uploadGeneratorState(channelId: ChannelId, channel: ChannelState) {
        transport.writePacket(WindfreakSynthHdProtocol.setFrequency(channelId, channel.frequencyMhz))
        transport.writePacket(WindfreakSynthHdProtocol.setPower(channelId, channel.powerDbm))
        transport.writePacket(WindfreakSynthHdProtocol.setPhase(channelId, channel.phaseDegrees))
        transport.writePacket(WindfreakSynthHdProtocol.setRfEnabled(channelId, channel.rfEnabled))
    }

    private fun applyChannelValidation(
        result: ValidationResult,
        update: (ChannelId, ChannelState) -> ChannelState,
    ): ValidationResult {
        if (!result.isValid) return result
        val channelId = state.activeChannel
        updateActiveChannel { update(channelId, it) }
        return result
    }

    private fun updateActiveChannel(update: (ChannelState) -> ChannelState) {
        state = when (state.activeChannel) {
            ChannelId.A -> state.copy(channelA = update(state.channelA))
            ChannelId.B -> state.copy(channelB = update(state.channelB))
        }
    }

    private val activeChannel: ChannelState
        get() = when (state.activeChannel) {
            ChannelId.A -> state.channelA
            ChannelId.B -> state.channelB
        }
}
