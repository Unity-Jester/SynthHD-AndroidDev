package com.windfreak.synthhd.domain

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SynthJsonTest {
    @Test
    fun fullRoundTripPreservesRepresentativeValues() {
        val state = SynthDeviceState(
            activeChannel = ChannelId.B,
            channelB = ChannelState(
                frequencyMhz = 2_450.5,
                powerDbm = -12.25,
                phaseDegrees = 180.0,
                rfEnabled = true,
                locked = true,
            ),
            sweep = SweepState(
                startMhz = 2_000.0,
                stopMhz = 1_000.0,
                stepMhz = 2.5,
                dwellMs = 25,
                direction = SweepDirection.Down,
                runMode = RunMode.Armed,
            ),
            hopList = listOf(HopPoint(frequencyMhz = 1_234.5, powerDbm = -6.0, dwellMs = 30)),
            modulation = ModulationState(
                pulseEnabled = true,
                amEnabled = true,
                fmEnabled = true,
                chirpEnabled = true,
                pulseWidthUs = 12.5,
                amDepthPercent = 75.0,
                fmDeviationKhz = 250.0,
            ),
            trigger = TriggerState(
                source = TriggerSource.External,
                edge = TriggerEdge.Falling,
                mode = RunMode.Running,
            ),
            status = DeviceStatus(
                connectedLabel = "USB simulator",
                model = "SynthHD Pro",
                serial = "SIM-1234",
                firmware = "sim-1.0",
                calibrationDate = "2026-06-24",
                temperatureC = 45.5,
                referenceMode = ReferenceMode.External,
                lockDetect = false,
                levelOk = false,
            ),
            savedSnapshot = ChannelId.B,
        )

        val restored = synthDeviceStateFromJson(state.toJson())

        assertEquals(state, restored)
    }

    @Test
    fun missingFieldsReturnDefaultsWithoutThrowing() {
        val restored = synthDeviceStateFromJson(JSONObject())

        assertEquals(SynthDeviceState(), restored)
    }

    @Test
    fun badEnumStringsFallBackOnlyForThoseFields() {
        val json = JSONObject()
            .put("activeChannel", "C")
            .put(
                "channelB",
                JSONObject()
                    .put("frequencyMhz", 2_400.0)
                    .put("rfEnabled", true),
            )
            .put(
                "sweep",
                JSONObject()
                    .put("direction", "Sideways")
                    .put("runMode", RunMode.Armed.name)
                    .put("startMhz", 2_000.0),
            )
            .put(
                "trigger",
                JSONObject()
                    .put("source", TriggerSource.External.name)
                    .put("edge", "Both")
                    .put("mode", RunMode.Running.name),
            )
            .put(
                "status",
                JSONObject()
                    .put("referenceMode", "Gps")
                    .put("connectedLabel", "Still valid"),
            )
            .put("savedSnapshot", "C")

        val restored = synthDeviceStateFromJson(json)

        assertEquals(ChannelId.A, restored.activeChannel)
        assertEquals(2_400.0, restored.channelB.frequencyMhz, 0.0)
        assertTrue(restored.channelB.rfEnabled)
        assertEquals(SweepDirection.Up, restored.sweep.direction)
        assertEquals(RunMode.Armed, restored.sweep.runMode)
        assertEquals(2_000.0, restored.sweep.startMhz, 0.0)
        assertEquals(TriggerSource.External, restored.trigger.source)
        assertEquals(TriggerEdge.Rising, restored.trigger.edge)
        assertEquals(RunMode.Running, restored.trigger.mode)
        assertEquals(ReferenceMode.Internal, restored.status.referenceMode)
        assertEquals("Still valid", restored.status.connectedLabel)
        assertNull(restored.savedSnapshot)
    }

    @Test
    fun nonFiniteNumericValuesFallBackToDefaults() {
        val json = JSONObject()
            .put(
                "channelA",
                JSONObject()
                    .put("frequencyMhz", "NaN")
                    .put("powerDbm", "Infinity")
                    .put("phaseDegrees", 90.0),
            )
            .put(
                "sweep",
                JSONObject()
                    .put("startMhz", "NaN")
                    .put("stopMhz", "Infinity")
                    .put("stepMhz", 0.25)
                    .put("dwellMs", -1),
            )
            .put(
                "hopList",
                JSONArray().put(
                    JSONObject()
                        .put("frequencyMhz", "Infinity")
                        .put("powerDbm", "-Infinity")
                        .put("dwellMs", 0),
                ),
            )
            .put(
                "modulation",
                JSONObject()
                    .put("pulseWidthUs", "NaN")
                    .put("amDepthPercent", "Infinity")
                    .put("fmDeviationKhz", 125.0),
            )
            .put(
                "status",
                JSONObject()
                    .put("temperatureC", "NaN"),
            )

        val restored = synthDeviceStateFromJson(json)

        assertEquals(1_000.0, restored.channelA.frequencyMhz, 0.0)
        assertEquals(0.0, restored.channelA.powerDbm, 0.0)
        assertEquals(90.0, restored.channelA.phaseDegrees, 0.0)
        assertEquals(1_000.0, restored.sweep.startMhz, 0.0)
        assertEquals(2_000.0, restored.sweep.stopMhz, 0.0)
        assertEquals(0.25, restored.sweep.stepMhz, 0.0)
        assertEquals(10, restored.sweep.dwellMs)
        assertEquals(1, restored.hopList.size)
        assertEquals(1_000.0, restored.hopList.single().frequencyMhz, 0.0)
        assertEquals(0.0, restored.hopList.single().powerDbm, 0.0)
        assertEquals(10, restored.hopList.single().dwellMs)
        assertEquals(10.0, restored.modulation.pulseWidthUs, 0.0)
        assertEquals(50.0, restored.modulation.amDepthPercent, 0.0)
        assertEquals(125.0, restored.modulation.fmDeviationKhz, 0.0)
        assertEquals(40.0, restored.status.temperatureC, 0.0)
        assertFalse(restored.channelA.rfEnabled)
    }
}
