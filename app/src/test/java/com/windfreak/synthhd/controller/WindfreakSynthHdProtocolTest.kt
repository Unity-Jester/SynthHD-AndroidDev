package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.SweepDirection
import com.windfreak.synthhd.domain.SweepState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WindfreakSynthHdProtocolTest {
    @Test
    fun formatsGeneratorCommandsWithoutLineTerminators() {
        assertEquals("C0f2450.125", WindfreakSynthHdProtocol.setFrequency(ChannelId.A, 2450.125))
        assertEquals("C1W-3.0", WindfreakSynthHdProtocol.setPower(ChannelId.B, -3.0))
        assertEquals("C0~90.25", WindfreakSynthHdProtocol.setPhase(ChannelId.A, 90.25))

        assertFalse(WindfreakSynthHdProtocol.setFrequency(ChannelId.A, 1000.0).contains("\n"))
        assertFalse(WindfreakSynthHdProtocol.setFrequency(ChannelId.A, 1000.0).contains("\r"))
    }

    @Test
    fun formatsRfOutputUsingDocumentedPllAndPaCommands() {
        assertEquals("C0E1r1", WindfreakSynthHdProtocol.setRfEnabled(ChannelId.A, true))
        assertEquals("C1E0r0", WindfreakSynthHdProtocol.setRfEnabled(ChannelId.B, false))
    }

    @Test
    fun formatsReferenceModeCommands() {
        assertEquals("x1", WindfreakSynthHdProtocol.setReference(ReferenceMode.Internal))
        assertEquals("x0", WindfreakSynthHdProtocol.setReference(ReferenceMode.External))
    }

    @Test
    fun formatsLinearSweepSetupAndRunCommands() {
        val sweep = SweepState(
            startMhz = 1000.0,
            stopMhz = 2000.0,
            stepMhz = 10.0,
            dwellMs = 25,
            direction = SweepDirection.Up,
        )

        assertEquals(
            "C0l1000.0u2000.0s10.0t25[0.0]0.0^1X0",
            WindfreakSynthHdProtocol.configureLinearSweep(ChannelId.A, sweep, powerDbm = 0.0),
        )
        assertEquals("C0c1g1", WindfreakSynthHdProtocol.startContinuousSweep(ChannelId.A))
        assertEquals("C0g0", WindfreakSynthHdProtocol.stopSweep(ChannelId.A))
    }

    @Test
    fun formatsQueryCommands() {
        assertEquals("+", WindfreakSynthHdProtocol.queryModel())
        assertEquals("-", WindfreakSynthHdProtocol.querySerial())
        assertEquals("v0", WindfreakSynthHdProtocol.queryFirmware())
        assertEquals("z", WindfreakSynthHdProtocol.queryTemperature())
        assertEquals("C1p", WindfreakSynthHdProtocol.queryPhaseLock(ChannelId.B))
    }
}
