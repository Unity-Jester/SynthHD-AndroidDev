package com.windfreak.synthhd.domain

enum class ChannelId { A, B }
enum class SweepDirection { Up, Down }
enum class RunMode { Idle, Armed, Running, Complete }
enum class TriggerSource { Software, External }
enum class TriggerEdge { Rising, Falling }
enum class ReferenceMode { Internal, External }

data class ValidationResult(val isValid: Boolean, val message: String = "")

data class ChannelState(
    val frequencyMhz: Double = 1_000.0,
    val powerDbm: Double = 0.0,
    val phaseDegrees: Double = 0.0,
    val rfEnabled: Boolean = false,
    val locked: Boolean = false,
)

data class SweepState(
    val startMhz: Double = 1_000.0,
    val stopMhz: Double = 2_000.0,
    val stepMhz: Double = 10.0,
    val dwellMs: Int = 10,
    val direction: SweepDirection = SweepDirection.Up,
    val runMode: RunMode = RunMode.Idle,
)

data class HopPoint(
    val frequencyMhz: Double,
    val powerDbm: Double,
    val dwellMs: Int,
)

data class ModulationState(
    val pulseEnabled: Boolean = false,
    val amEnabled: Boolean = false,
    val fmEnabled: Boolean = false,
    val chirpEnabled: Boolean = false,
    val pulseWidthUs: Double = 10.0,
    val amDepthPercent: Double = 50.0,
    val fmDeviationKhz: Double = 100.0,
)

data class TriggerState(
    val source: TriggerSource = TriggerSource.Software,
    val edge: TriggerEdge = TriggerEdge.Rising,
    val mode: RunMode = RunMode.Idle,
)

data class DeviceStatus(
    val connectedLabel: String = "Offline simulator",
    val model: String = "SynthHD Pro V2",
    val serial: String = "SIM-0001",
    val firmware: String = "sim-0.1",
    val calibrationDate: String = "Simulated",
    val temperatureC: Double = 40.0,
    val referenceMode: ReferenceMode = ReferenceMode.Internal,
    val lockDetect: Boolean = true,
    val levelOk: Boolean = true,
)

data class SynthDeviceState(
    val activeChannel: ChannelId = ChannelId.A,
    val channelA: ChannelState = ChannelState(),
    val channelB: ChannelState = ChannelState(),
    val sweep: SweepState = SweepState(),
    val hopList: List<HopPoint> = emptyList(),
    val modulation: ModulationState = ModulationState(),
    val trigger: TriggerState = TriggerState(),
    val status: DeviceStatus = DeviceStatus(),
    val savedSnapshot: ChannelId? = null,
)
