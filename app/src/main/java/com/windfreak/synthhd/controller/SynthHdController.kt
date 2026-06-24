package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.domain.ValidationResult

interface SynthHdController {
    val state: SynthDeviceState

    fun replaceState(state: SynthDeviceState)
    fun selectChannel(channelId: ChannelId)
    fun setFrequencyMhz(value: Double): ValidationResult
    fun setPowerDbm(value: Double): ValidationResult
    fun setPhaseDegrees(value: Double): ValidationResult
    fun setRfEnabled(enabled: Boolean)
    fun setChannelLocked(locked: Boolean)
    fun setReferenceMode(referenceMode: ReferenceMode)
    fun setSweep(sweep: SweepState): ValidationResult
    fun startSweep()
    fun stopSweep()
    fun addHopPoint(point: HopPoint): ValidationResult
    fun removeHopPoint(index: Int)
    fun clearHopList()
    fun setModulation(modulation: ModulationState): ValidationResult
    fun setTrigger(trigger: TriggerState)
    fun softwareTrigger()
    fun saveToDevice()
    fun resetToDefaults()
}
