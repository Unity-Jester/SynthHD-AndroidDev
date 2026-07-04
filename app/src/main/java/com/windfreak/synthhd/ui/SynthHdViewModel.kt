package com.windfreak.synthhd.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.windfreak.synthhd.controller.HardwareConnectResult
import com.windfreak.synthhd.controller.HardwareControllerFactory
import com.windfreak.synthhd.controller.HardwareDevice
import com.windfreak.synthhd.controller.SimulatedSynthHdController
import com.windfreak.synthhd.controller.SynthHdController
import com.windfreak.synthhd.controller.WindfreakSynthHdController
import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.DeviceStatus
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.domain.ValidationResult
import com.windfreak.synthhd.persistence.SynthStateStore

class SynthHdViewModel(
    private val store: SynthStateStore,
    initialController: SynthHdController = SimulatedSynthHdController(store.load()),
    private val hardwareFactory: HardwareControllerFactory? = null,
) : ViewModel() {
    private var controller: SynthHdController = initialController

    var state by mutableStateOf(controller.state)
        private set

    var message by mutableStateOf("Offline simulator ready")
        private set

    var hardwareDevices by mutableStateOf<List<HardwareDevice>>(emptyList())
        private set

    val isHardwareConnected: Boolean
        get() = controller is WindfreakSynthHdController

    fun selectChannel(channelId: ChannelId) = applyChange { controller.selectChannel(channelId) }
    fun setFrequencyMhz(value: Double) = applyValidation(controller.setFrequencyMhz(value))
    fun setPowerDbm(value: Double) = applyValidation(controller.setPowerDbm(value))
    fun setPhaseDegrees(value: Double) = applyValidation(controller.setPhaseDegrees(value))
    fun setRfEnabled(enabled: Boolean) = applyChange { controller.setRfEnabled(enabled) }
    fun setChannelLocked(locked: Boolean) = applyChange { controller.setChannelLocked(locked) }
    fun setReferenceMode(referenceMode: ReferenceMode) = applyChange { controller.setReferenceMode(referenceMode) }
    fun setSweep(sweep: SweepState) = applyValidation(controller.setSweep(sweep))
    fun startSweep() = applyChange { controller.startSweep() }
    fun stopSweep() = applyChange { controller.stopSweep() }
    fun armTriggeredSweep() {
        val result = controller.setSweep(controller.state.sweep.copy(runMode = RunMode.Armed))
        if (!result.isValid) {
            applyValidation(result)
            return
        }
        applyChange { controller.setTrigger(controller.state.trigger.copy(mode = RunMode.Armed)) }
    }
    fun addHopPoint(point: HopPoint) = applyValidation(controller.addHopPoint(point))
    fun updateHopPoint(index: Int, point: HopPoint) = applyValidation(controller.updateHopPoint(index, point))
    fun moveHopPoint(index: Int, offset: Int) = applyChange { controller.moveHopPoint(index, offset) }
    fun removeHopPoint(index: Int) = applyChange { controller.removeHopPoint(index) }
    fun clearHopList() = applyChange { controller.clearHopList() }
    fun startHopList() = applyChange { controller.startHopList() }
    fun stopHopList() = applyChange { controller.stopHopList() }
    fun setModulation(modulation: ModulationState) = applyValidation(controller.setModulation(modulation))
    fun setTrigger(trigger: TriggerState) = applyChange { controller.setTrigger(trigger) }
    fun softwareTrigger() {
        val result = controller.softwareTrigger()
        if (result.isValid) {
            sync("Software trigger fired")
        } else {
            message = result.message
        }
    }
    fun saveToDevice() = applyChange(if (isHardwareConnected) "Hardware settings saved" else "Simulated settings saved") {
        controller.saveToDevice()
    }
    fun resetToDefaults() = applyChange("Simulator reset") { controller.resetToDefaults() }

    fun replaceState(newState: SynthDeviceState) = applyChange { controller.replaceState(newState) }

    fun scanUsbDevices() {
        val factory = hardwareFactory
        if (factory == null) {
            message = "USB hardware control is not available in this build."
            return
        }
        hardwareDevices = factory.scan()
        message = if (hardwareDevices.isEmpty()) {
            "No USB serial devices found"
        } else {
            "Found ${hardwareDevices.size} USB serial device(s)"
        }
    }

    fun connectUsbDevice() {
        val factory = hardwareFactory
        if (factory == null) {
            message = "USB hardware control is not available in this build."
            return
        }
        when (val result = factory.connectFirst(state)) {
            is HardwareConnectResult.Connected -> {
                closeHardwareController()
                controller = result.controller
                sync("USB hardware connected")
            }
            is HardwareConnectResult.PermissionRequested -> {
                message = "USB permission requested for ${result.deviceLabel}. Approve it, then tap Connect again."
            }
            is HardwareConnectResult.Failed -> {
                message = result.message
            }
        }
    }

    fun disconnectHardware() {
        closeHardwareController()
        controller = SimulatedSynthHdController(state.copy(status = DeviceStatus()))
        sync("Returned to offline simulator")
    }

    override fun onCleared() {
        closeHardwareController()
        super.onCleared()
    }

    private fun applyValidation(result: ValidationResult) {
        if (result.isValid) {
            sync("Updated")
        } else {
            message = result.message
        }
    }

    private fun applyChange(nextMessage: String = "Updated", change: () -> Unit) {
        change()
        sync(nextMessage)
    }

    private fun sync(nextMessage: String) {
        state = controller.state
        store.save(state)
        message = nextMessage
    }

    private fun closeHardwareController() {
        (controller as? WindfreakSynthHdController)?.close()
    }
}
