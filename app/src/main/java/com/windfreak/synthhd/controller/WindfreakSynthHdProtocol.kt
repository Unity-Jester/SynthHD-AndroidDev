package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.SweepDirection
import com.windfreak.synthhd.domain.SweepState
import java.math.BigDecimal

object WindfreakSynthHdProtocol {
    fun selectChannel(channel: ChannelId): String = "C${channel.index}"

    fun setFrequency(channel: ChannelId, frequencyMhz: Double): String =
        "${selectChannel(channel)}f${formatDecimal(frequencyMhz)}"

    fun setPower(channel: ChannelId, powerDbm: Double): String =
        "${selectChannel(channel)}W${formatDecimal(powerDbm)}"

    fun setPhase(channel: ChannelId, phaseDegrees: Double): String =
        "${selectChannel(channel)}~${formatDecimal(phaseDegrees)}"

    fun setRfEnabled(channel: ChannelId, enabled: Boolean): String {
        val value = if (enabled) "1" else "0"
        return "${selectChannel(channel)}E${value}r$value"
    }

    fun setReference(referenceMode: ReferenceMode): String =
        when (referenceMode) {
            ReferenceMode.Internal -> "x1"
            ReferenceMode.External -> "x0"
        }

    fun configureLinearSweep(channel: ChannelId, sweep: SweepState, powerDbm: Double): String {
        val direction = when (sweep.direction) {
            SweepDirection.Up -> "1"
            SweepDirection.Down -> "0"
        }
        return buildString {
            append(selectChannel(channel))
            append("l").append(formatDecimal(sweep.startMhz))
            append("u").append(formatDecimal(sweep.stopMhz))
            append("s").append(formatDecimal(sweep.stepMhz))
            append("t").append(sweep.dwellMs)
            append("[").append(formatDecimal(powerDbm))
            append("]").append(formatDecimal(powerDbm))
            append("^").append(direction)
            append("X0")
        }
    }

    fun startContinuousSweep(channel: ChannelId): String = "${selectChannel(channel)}c1g1"

    fun startSingleSweep(channel: ChannelId): String = "${selectChannel(channel)}c0g1"

    fun stopSweep(channel: ChannelId): String = "${selectChannel(channel)}g0"

    fun armExternalSweepTrigger(channel: ChannelId): String = "${selectChannel(channel)}w1"

    fun disableTrigger(channel: ChannelId): String = "${selectChannel(channel)}w0"

    fun setPulse(channel: ChannelId, pulseWidthUs: Double, enabled: Boolean): String =
        "${selectChannel(channel)}P${formatDecimal(pulseWidthUs)}j${if (enabled) "1" else "0"}"

    fun setAmEnabled(enabled: Boolean): String = "A${if (enabled) "1" else "0"}"

    fun setFm(channel: ChannelId, deviationKhz: Double, chirp: Boolean, enabled: Boolean): String {
        val shape = if (chirp) "0" else "1"
        val deviationHz = (deviationKhz * 1_000.0).toLong()
        return "${selectChannel(channel)}>$deviationHz;$shape/${if (enabled) "1" else "0"}"
    }

    fun saveToEeprom(): String = "e"

    fun queryModel(): String = "+"

    fun querySerial(): String = "-"

    fun queryFirmware(): String = "v0"

    fun queryTemperature(): String = "z"

    fun queryPhaseLock(channel: ChannelId): String = "${selectChannel(channel)}p"

    private val ChannelId.index: Int
        get() = when (this) {
            ChannelId.A -> 0
            ChannelId.B -> 1
        }

    private fun formatDecimal(value: Double): String {
        val text = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
        return if (text.contains(".")) text else "$text.0"
    }
}
