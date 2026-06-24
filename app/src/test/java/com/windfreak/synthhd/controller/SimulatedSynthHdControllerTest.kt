package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedSynthHdControllerTest {
    @Test
    fun updatesOnlyTheActiveChannelGeneratorFields() {
        val controller = SimulatedSynthHdController()

        controller.selectChannel(ChannelId.B)
        controller.setFrequencyMhz(2_450.0)
        controller.setPowerDbm(-3.0)
        controller.setRfEnabled(true)

        val state = controller.state
        assertEquals(ChannelId.B, state.activeChannel)
        assertEquals(1_000.0, state.channelA.frequencyMhz, 0.0)
        assertEquals(2_450.0, state.channelB.frequencyMhz, 0.0)
        assertEquals(-3.0, state.channelB.powerDbm, 0.0)
        assertTrue(state.channelB.rfEnabled)
    }

    @Test
    fun invalidFrequencyDoesNotApply() {
        val controller = SimulatedSynthHdController()

        val result = controller.setFrequencyMhz(1.0)

        assertFalse(result.isValid)
        assertEquals(1_000.0, controller.state.channelA.frequencyMhz, 0.0)
    }

    @Test
    fun listModeCapsAtFiveHundredPoints() {
        val controller = SimulatedSynthHdController()
        repeat(500) { index ->
            controller.addHopPoint(HopPoint(100.0 + index, 0.0, 10))
        }

        val result = controller.addHopPoint(HopPoint(1_000.0, 0.0, 10))

        assertFalse(result.isValid)
        assertEquals(500, controller.state.hopList.size)
    }

    @Test
    fun softwareTriggerRunsAndCompletesArmedSweep() {
        val controller = SimulatedSynthHdController()

        controller.setSweep(SweepState(runMode = RunMode.Armed))
        controller.softwareTrigger()

        assertEquals(RunMode.Complete, controller.state.sweep.runMode)
        assertEquals(RunMode.Complete, controller.state.trigger.mode)
    }
}
