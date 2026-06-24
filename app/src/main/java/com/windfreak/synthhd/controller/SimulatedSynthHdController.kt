package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ChannelState
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepDirection
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.SynthConstants
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.domain.ValidationResult
import com.windfreak.synthhd.domain.validateAmDepthPercent
import com.windfreak.synthhd.domain.validateFrequencyMhz
import com.windfreak.synthhd.domain.validateFmDeviationKhz
import com.windfreak.synthhd.domain.validateHopListSize
import com.windfreak.synthhd.domain.validatePhaseDegrees
import com.windfreak.synthhd.domain.validatePositiveDwellMs
import com.windfreak.synthhd.domain.validatePowerDbm
import com.windfreak.synthhd.domain.validatePulseWidthUs

class SimulatedSynthHdController(initialState: SynthDeviceState = SynthDeviceState()) : SynthHdController {
    override var state: SynthDeviceState = initialState
        private set

    override fun replaceState(state: SynthDeviceState) {
        this.state = sanitizeState(state)
    }

    override fun selectChannel(channelId: ChannelId) {
        state = state.copy(activeChannel = channelId)
    }

    override fun setFrequencyMhz(value: Double): ValidationResult =
        applyIfValid(validateFrequencyMhz(value)) { channel -> channel.copy(frequencyMhz = value) }

    override fun setPowerDbm(value: Double): ValidationResult =
        applyIfValid(validatePowerDbm(value)) { channel -> channel.copy(powerDbm = value) }

    override fun setPhaseDegrees(value: Double): ValidationResult =
        applyIfValid(validatePhaseDegrees(value)) { channel -> channel.copy(phaseDegrees = value) }

    override fun setRfEnabled(enabled: Boolean) {
        updateActiveChannel { it.copy(rfEnabled = enabled) }
    }

    override fun setChannelLocked(locked: Boolean) {
        updateActiveChannel { it.copy(locked = locked) }
    }

    override fun setReferenceMode(referenceMode: ReferenceMode) {
        state = state.copy(status = state.status.copy(referenceMode = referenceMode))
    }

    override fun setSweep(sweep: SweepState): ValidationResult {
        val result = validateSweep(sweep)
        if (!result.isValid) return result
        state = state.copy(sweep = sweep)
        return result
    }

    override fun startSweep() {
        state = state.copy(sweep = state.sweep.copy(runMode = RunMode.Running))
    }

    override fun stopSweep() {
        state = state.copy(sweep = state.sweep.copy(runMode = RunMode.Idle))
    }

    override fun addHopPoint(point: HopPoint): ValidationResult {
        val size = validateHopListSize(state.hopList.size + 1)
        if (!size.isValid) return size
        val frequency = validateFrequencyMhz(point.frequencyMhz)
        if (!frequency.isValid) return frequency
        val power = validatePowerDbm(point.powerDbm)
        if (!power.isValid) return power
        val dwell = validatePositiveDwellMs(point.dwellMs)
        if (!dwell.isValid) return dwell
        state = state.copy(hopList = state.hopList + point)
        return ValidationResult(true)
    }

    override fun removeHopPoint(index: Int) {
        if (index !in state.hopList.indices) return
        state = state.copy(hopList = state.hopList.toMutableList().also { it.removeAt(index) })
    }

    override fun clearHopList() {
        state = state.copy(hopList = emptyList())
    }

    override fun setModulation(modulation: ModulationState): ValidationResult {
        val result = validateModulation(modulation)
        if (!result.isValid) return result
        state = state.copy(modulation = modulation)
        return result
    }

    override fun setTrigger(trigger: TriggerState) {
        state = state.copy(trigger = trigger)
    }

    override fun softwareTrigger() {
        state = state.copy(
            sweep = state.sweep.copy(runMode = RunMode.Complete),
            trigger = state.trigger.copy(mode = RunMode.Complete),
        )
    }

    override fun saveToDevice() {
        state = state.copy(savedSnapshot = state.activeChannel)
    }

    override fun resetToDefaults() {
        state = SynthDeviceState()
    }

    private fun applyIfValid(
        result: ValidationResult,
        update: (ChannelState) -> ChannelState,
    ): ValidationResult {
        if (!result.isValid) return result
        updateActiveChannel(update)
        return result
    }

    private fun updateActiveChannel(update: (ChannelState) -> ChannelState) {
        state = when (state.activeChannel) {
            ChannelId.A -> state.copy(channelA = update(state.channelA))
            ChannelId.B -> state.copy(channelB = update(state.channelB))
        }
    }

    private fun sanitizeState(state: SynthDeviceState): SynthDeviceState {
        val defaults = SynthDeviceState()
        return state.copy(
            channelA = sanitizeChannel(state.channelA),
            channelB = sanitizeChannel(state.channelB),
            sweep = if (validateSweep(state.sweep).isValid) state.sweep else defaults.sweep,
            hopList = sanitizeHopList(state.hopList),
            modulation = if (validateModulation(state.modulation).isValid) state.modulation else defaults.modulation,
        )
    }

    private fun sanitizeChannel(channel: ChannelState): ChannelState =
        if (isValidChannel(channel)) channel else ChannelState()

    private fun sanitizeHopList(points: List<HopPoint>): List<HopPoint> =
        points
            .filter(::isValidHopPoint)
            .take(SynthConstants.MAX_HOP_POINTS)

    private fun isValidChannel(channel: ChannelState): Boolean =
        validateFrequencyMhz(channel.frequencyMhz).isValid &&
            validatePowerDbm(channel.powerDbm).isValid &&
            validatePhaseDegrees(channel.phaseDegrees).isValid

    private fun isValidHopPoint(point: HopPoint): Boolean =
        validateFrequencyMhz(point.frequencyMhz).isValid &&
            validatePowerDbm(point.powerDbm).isValid &&
            validatePositiveDwellMs(point.dwellMs).isValid

    private fun validateSweep(sweep: SweepState): ValidationResult {
        if (!sweep.startMhz.isFinite() || !sweep.stopMhz.isFinite() || !sweep.stepMhz.isFinite()) {
            return ValidationResult(false, "Sweep values must be finite.")
        }
        val start = validateFrequencyMhz(sweep.startMhz)
        if (!start.isValid) return start
        val stop = validateFrequencyMhz(sweep.stopMhz)
        if (!stop.isValid) return stop
        val dwell = validatePositiveDwellMs(sweep.dwellMs)
        if (!dwell.isValid) return dwell
        if (sweep.stepMhz <= 0.0) return ValidationResult(false, "Sweep step must be greater than 0 MHz.")
        if (!sweep.hasValidSpanForDirection()) {
            return ValidationResult(false, "Sweep direction must match start and stop frequencies.")
        }
        return ValidationResult(true)
    }

    private fun validateModulation(modulation: ModulationState): ValidationResult {
        val pulseWidth = validatePulseWidthUs(modulation.pulseWidthUs)
        if (!pulseWidth.isValid) return pulseWidth
        val amDepth = validateAmDepthPercent(modulation.amDepthPercent)
        if (!amDepth.isValid) return amDepth
        val fmDeviation = validateFmDeviationKhz(modulation.fmDeviationKhz)
        if (!fmDeviation.isValid) return fmDeviation
        return ValidationResult(true)
    }

    private fun SweepState.hasValidSpanForDirection(): Boolean =
        when (direction) {
            SweepDirection.Up -> startMhz <= stopMhz
            SweepDirection.Down -> startMhz >= stopMhz
        }
}
