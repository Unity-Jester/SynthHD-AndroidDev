package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepDirection
import com.windfreak.synthhd.domain.SweepState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WindfreakSynthHdControllerTest {
    @Test
    fun generatorControlsWriteDocumentedPacketsForActiveChannel() {
        val transport = FakeWindfreakTransport()
        val controller = WindfreakSynthHdController(transport)

        controller.selectChannel(ChannelId.B)
        controller.setFrequencyMhz(2450.125)
        controller.setPowerDbm(-3.0)
        controller.setPhaseDegrees(90.0)
        controller.setRfEnabled(true)

        assertEquals(
            listOf("C1", "C1f2450.125", "C1W-3.0", "C1~90.0", "C1E1r1"),
            transport.writes,
        )
        assertEquals(2450.125, controller.state.channelB.frequencyMhz, 0.0)
        assertEquals(-3.0, controller.state.channelB.powerDbm, 0.0)
        assertTrue(controller.state.channelB.rfEnabled)
    }

    @Test
    fun invalidGeneratorValueDoesNotWriteToDevice() {
        val transport = FakeWindfreakTransport()
        val controller = WindfreakSynthHdController(transport)

        val result = controller.setFrequencyMhz(1.0)

        assertEquals(false, result.isValid)
        assertEquals(emptyList<String>(), transport.writes)
    }

    @Test
    fun referenceAndSweepControlsWriteDocumentedPackets() {
        val transport = FakeWindfreakTransport()
        val controller = WindfreakSynthHdController(transport)
        val sweep = SweepState(
            startMhz = 2000.0,
            stopMhz = 1000.0,
            stepMhz = 25.0,
            dwellMs = 50,
            direction = SweepDirection.Down,
        )

        controller.setReferenceMode(ReferenceMode.External)
        controller.setSweep(sweep)
        controller.startSweep()
        controller.stopSweep()

        assertEquals(
            listOf("x0", "C0l2000.0u1000.0s25.0t50[0.0]0.0^0X0", "C0c1g1", "C0g0"),
            transport.writes,
        )
        assertEquals(RunMode.Idle, controller.state.sweep.runMode)
        assertEquals(ReferenceMode.External, controller.state.status.referenceMode)
    }

    @Test
    fun refreshStatusQueriesModelSerialFirmwareTemperatureAndLock() {
        val transport = FakeWindfreakTransport(
            responses = mapOf(
                "+" to "SynthHD Pro V2",
                "-" to "206A3670",
                "v0" to "2.0",
                "z" to "41.25",
                "C0p" to "1",
            ),
        )
        val controller = WindfreakSynthHdController(transport)

        controller.refreshStatus()

        assertEquals(listOf("+", "-", "v0", "z", "C0p"), transport.queries)
        assertEquals("USB hardware", controller.state.status.connectedLabel)
        assertEquals("SynthHD Pro V2", controller.state.status.model)
        assertEquals("206A3670", controller.state.status.serial)
        assertEquals("2.0", controller.state.status.firmware)
        assertEquals(41.25, controller.state.status.temperatureC, 0.0)
        assertEquals(true, controller.state.status.lockDetect)
    }
}

private class FakeWindfreakTransport(
    private val responses: Map<String, String> = emptyMap(),
) : WindfreakSerialTransport {
    val writes = mutableListOf<String>()
    val queries = mutableListOf<String>()

    override fun writePacket(packet: String) {
        writes += packet
    }

    override fun query(packet: String): String {
        queries += packet
        return responses.getValue(packet)
    }

    override fun close() = Unit
}
