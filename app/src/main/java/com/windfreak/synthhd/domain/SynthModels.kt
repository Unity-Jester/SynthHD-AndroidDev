package com.windfreak.synthhd.domain

import org.json.JSONArray
import org.json.JSONObject

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

fun SynthDeviceState.toJson(): JSONObject = JSONObject()
    .put("activeChannel", activeChannel.name)
    .put("channelA", channelA.toJson())
    .put("channelB", channelB.toJson())
    .put("sweep", sweep.toJson())
    .put("hopList", JSONArray().also { array -> hopList.forEach { array.put(it.toJson()) } })
    .put("modulation", modulation.toJson())
    .put("trigger", trigger.toJson())
    .put("status", status.toJson())
    .put("savedSnapshot", savedSnapshot?.name ?: "")

fun synthDeviceStateFromJson(json: JSONObject): SynthDeviceState = SynthDeviceState(
    activeChannel = ChannelId.valueOf(json.optString("activeChannel", ChannelId.A.name)),
    channelA = channelStateFromJson(json.optJSONObject("channelA") ?: JSONObject()),
    channelB = channelStateFromJson(json.optJSONObject("channelB") ?: JSONObject()),
    sweep = sweepStateFromJson(json.optJSONObject("sweep") ?: JSONObject()),
    hopList = hopListFromJson(json.optJSONArray("hopList") ?: JSONArray()),
    modulation = modulationStateFromJson(json.optJSONObject("modulation") ?: JSONObject()),
    trigger = triggerStateFromJson(json.optJSONObject("trigger") ?: JSONObject()),
    status = deviceStatusFromJson(json.optJSONObject("status") ?: JSONObject()),
    savedSnapshot = json.optString("savedSnapshot").takeIf { it.isNotBlank() }?.let(ChannelId::valueOf),
)

private fun ChannelState.toJson(): JSONObject = JSONObject()
    .put("frequencyMhz", frequencyMhz)
    .put("powerDbm", powerDbm)
    .put("phaseDegrees", phaseDegrees)
    .put("rfEnabled", rfEnabled)
    .put("locked", locked)

private fun channelStateFromJson(json: JSONObject): ChannelState = ChannelState(
    frequencyMhz = json.optDouble("frequencyMhz", 1_000.0),
    powerDbm = json.optDouble("powerDbm", 0.0),
    phaseDegrees = json.optDouble("phaseDegrees", 0.0),
    rfEnabled = json.optBoolean("rfEnabled", false),
    locked = json.optBoolean("locked", false),
)

private fun SweepState.toJson(): JSONObject = JSONObject()
    .put("startMhz", startMhz)
    .put("stopMhz", stopMhz)
    .put("stepMhz", stepMhz)
    .put("dwellMs", dwellMs)
    .put("direction", direction.name)
    .put("runMode", runMode.name)

private fun sweepStateFromJson(json: JSONObject): SweepState = SweepState(
    startMhz = json.optDouble("startMhz", 1_000.0),
    stopMhz = json.optDouble("stopMhz", 2_000.0),
    stepMhz = json.optDouble("stepMhz", 10.0),
    dwellMs = json.optInt("dwellMs", 10),
    direction = SweepDirection.valueOf(json.optString("direction", SweepDirection.Up.name)),
    runMode = RunMode.valueOf(json.optString("runMode", RunMode.Idle.name)),
)

private fun HopPoint.toJson(): JSONObject = JSONObject()
    .put("frequencyMhz", frequencyMhz)
    .put("powerDbm", powerDbm)
    .put("dwellMs", dwellMs)

private fun hopPointFromJson(json: JSONObject): HopPoint = HopPoint(
    frequencyMhz = json.optDouble("frequencyMhz", 1_000.0),
    powerDbm = json.optDouble("powerDbm", 0.0),
    dwellMs = json.optInt("dwellMs", 10),
)

private fun hopListFromJson(array: JSONArray): List<HopPoint> =
    List(array.length()) { index -> hopPointFromJson(array.optJSONObject(index) ?: JSONObject()) }

private fun ModulationState.toJson(): JSONObject = JSONObject()
    .put("pulseEnabled", pulseEnabled)
    .put("amEnabled", amEnabled)
    .put("fmEnabled", fmEnabled)
    .put("chirpEnabled", chirpEnabled)
    .put("pulseWidthUs", pulseWidthUs)
    .put("amDepthPercent", amDepthPercent)
    .put("fmDeviationKhz", fmDeviationKhz)

private fun modulationStateFromJson(json: JSONObject): ModulationState = ModulationState(
    pulseEnabled = json.optBoolean("pulseEnabled", false),
    amEnabled = json.optBoolean("amEnabled", false),
    fmEnabled = json.optBoolean("fmEnabled", false),
    chirpEnabled = json.optBoolean("chirpEnabled", false),
    pulseWidthUs = json.optDouble("pulseWidthUs", 10.0),
    amDepthPercent = json.optDouble("amDepthPercent", 50.0),
    fmDeviationKhz = json.optDouble("fmDeviationKhz", 100.0),
)

private fun TriggerState.toJson(): JSONObject = JSONObject()
    .put("source", source.name)
    .put("edge", edge.name)
    .put("mode", mode.name)

private fun triggerStateFromJson(json: JSONObject): TriggerState = TriggerState(
    source = TriggerSource.valueOf(json.optString("source", TriggerSource.Software.name)),
    edge = TriggerEdge.valueOf(json.optString("edge", TriggerEdge.Rising.name)),
    mode = RunMode.valueOf(json.optString("mode", RunMode.Idle.name)),
)

private fun DeviceStatus.toJson(): JSONObject = JSONObject()
    .put("connectedLabel", connectedLabel)
    .put("model", model)
    .put("serial", serial)
    .put("firmware", firmware)
    .put("calibrationDate", calibrationDate)
    .put("temperatureC", temperatureC)
    .put("referenceMode", referenceMode.name)
    .put("lockDetect", lockDetect)
    .put("levelOk", levelOk)

private fun deviceStatusFromJson(json: JSONObject): DeviceStatus = DeviceStatus(
    connectedLabel = json.optString("connectedLabel", "Offline simulator"),
    model = json.optString("model", "SynthHD Pro V2"),
    serial = json.optString("serial", "SIM-0001"),
    firmware = json.optString("firmware", "sim-0.1"),
    calibrationDate = json.optString("calibrationDate", "Simulated"),
    temperatureC = json.optDouble("temperatureC", 40.0),
    referenceMode = ReferenceMode.valueOf(json.optString("referenceMode", ReferenceMode.Internal.name)),
    lockDetect = json.optBoolean("lockDetect", true),
    levelOk = json.optBoolean("levelOk", true),
)
