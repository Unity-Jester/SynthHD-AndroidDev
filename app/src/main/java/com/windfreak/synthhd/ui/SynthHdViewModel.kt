package com.windfreak.synthhd.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.windfreak.synthhd.controller.SimulatedSynthHdController
import com.windfreak.synthhd.controller.SynthHdController
import com.windfreak.synthhd.domain.ChannelId
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
    private val controller: SynthHdController = SimulatedSynthHdController(store.load()),
) : ViewModel() {
    var state by mutableStateOf(controller.state)
        private set

    var message by mutableStateOf("Offline simulator ready")
        private set

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
    fun softwareTrigger() = applyChange { controller.softwareTrigger() }
    fun saveToDevice() = applyChange("Simulated settings saved") { controller.saveToDevice() }
    fun resetToDefaults() = applyChange("Simulator reset") { controller.resetToDefaults() }

    fun replaceState(newState: SynthDeviceState) = applyChange { controller.replaceState(newState) }

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
}
