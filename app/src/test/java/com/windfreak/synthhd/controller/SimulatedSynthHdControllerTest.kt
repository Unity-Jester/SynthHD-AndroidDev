package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ChannelState
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepDirection
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.TriggerState
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
    fun editsExistingHopPointWithValidation() {
        val controller = SimulatedSynthHdController()
        controller.addHopPoint(HopPoint(1_000.0, 0.0, 10))

        val result = controller.updateHopPoint(0, HopPoint(2_450.0, -3.0, 25))

        assertTrue(result.isValid)
        assertEquals(HopPoint(2_450.0, -3.0, 25), controller.state.hopList.single())
    }

    @Test
    fun invalidHopPointEditDoesNotApply() {
        val controller = SimulatedSynthHdController()
        controller.addHopPoint(HopPoint(1_000.0, 0.0, 10))

        val result = controller.updateHopPoint(0, HopPoint(1.0, -3.0, 25))

        assertFalse(result.isValid)
        assertEquals(HopPoint(1_000.0, 0.0, 10), controller.state.hopList.single())
    }

    @Test
    fun movesHopPointsUpAndDown() {
        val controller = SimulatedSynthHdController()
        val first = HopPoint(1_000.0, 0.0, 10)
        val second = HopPoint(2_000.0, -3.0, 20)
        controller.addHopPoint(first)
        controller.addHopPoint(second)

        controller.moveHopPoint(1, -1)

        assertEquals(listOf(second, first), controller.state.hopList)

        controller.moveHopPoint(0, 1)

        assertEquals(listOf(first, second), controller.state.hopList)
    }

    @Test
    fun startsAndStopsRunnableHopListRunState() {
        val controller = SimulatedSynthHdController()
        controller.addHopPoint(HopPoint(1_000.0, 0.0, 10))

        controller.startHopList()

        assertEquals(RunMode.Running, controller.state.listRunMode)

        controller.stopHopList()

        assertEquals(RunMode.Idle, controller.state.listRunMode)
    }

    @Test
    fun emptyHopListDoesNotStartRunState() {
        val controller = SimulatedSynthHdController()

        controller.startHopList()

        assertEquals(RunMode.Idle, controller.state.listRunMode)
    }

    @Test
    fun removingLastHopPointStopsListRunState() {
        val controller = SimulatedSynthHdController(
            SynthDeviceState(
                hopList = listOf(HopPoint(1_000.0, 0.0, 10)),
                listRunMode = RunMode.Running,
            ),
        )

        controller.removeHopPoint(0)

        assertEquals(RunMode.Idle, controller.state.listRunMode)
    }

    @Test
    fun sanitizingEmptyHopListStopsListRunState() {
        val controller = SimulatedSynthHdController(
            SynthDeviceState(listRunMode = RunMode.Running),
        )

        assertEquals(RunMode.Idle, controller.state.listRunMode)
    }

    @Test
    fun softwareTriggerRunsAndCompletesArmedSweep() {
        val controller = SimulatedSynthHdController()

        controller.setSweep(SweepState(runMode = RunMode.Armed))
        controller.softwareTrigger()

        assertEquals(RunMode.Complete, controller.state.sweep.runMode)
        assertEquals(RunMode.Complete, controller.state.trigger.mode)
    }

    @Test
    fun softwareTriggerDoesNotCompleteIdleSweep() {
        val controller = SimulatedSynthHdController()

        controller.softwareTrigger()

        assertEquals(RunMode.Idle, controller.state.sweep.runMode)
        assertEquals(RunMode.Idle, controller.state.trigger.mode)
    }

    @Test
    fun startingContinuousSweepClearsArmedTriggerState() {
        val controller = SimulatedSynthHdController(
            SynthDeviceState(
                sweep = SweepState(runMode = RunMode.Armed),
                trigger = TriggerState(mode = RunMode.Armed),
            ),
        )

        controller.startSweep()

        assertEquals(RunMode.Running, controller.state.sweep.runMode)
        assertEquals(RunMode.Idle, controller.state.trigger.mode)
    }

    @Test
    fun stoppingSweepClearsTriggerRunState() {
        val controller = SimulatedSynthHdController(
            SynthDeviceState(
                sweep = SweepState(runMode = RunMode.Armed),
                trigger = TriggerState(mode = RunMode.Armed),
            ),
        )

        controller.stopSweep()

        assertEquals(RunMode.Idle, controller.state.sweep.runMode)
        assertEquals(RunMode.Idle, controller.state.trigger.mode)
    }

    @Test
    fun armingTriggerAlsoArmsSweep() {
        val controller = SimulatedSynthHdController()

        controller.setTrigger(TriggerState(mode = RunMode.Armed))

        assertEquals(RunMode.Armed, controller.state.trigger.mode)
        assertEquals(RunMode.Armed, controller.state.sweep.runMode)
    }

    @Test
    fun invalidSweepStepDoesNotApply() {
        val controller = SimulatedSynthHdController()
        val previousSweep = controller.state.sweep

        val result = controller.setSweep(SweepState(stepMhz = Double.NaN))

        assertFalse(result.isValid)
        assertEquals(previousSweep, controller.state.sweep)
    }

    @Test
    fun invalidPulseWidthDoesNotApply() {
        val controller = SimulatedSynthHdController()
        val previousModulation = controller.state.modulation

        val result = controller.setModulation(ModulationState(pulseWidthUs = -1.0))

        assertFalse(result.isValid)
        assertEquals(previousModulation, controller.state.modulation)
    }

    @Test
    fun invalidAmDepthDoesNotApply() {
        val controller = SimulatedSynthHdController()
        val previousModulation = controller.state.modulation

        val result = controller.setModulation(ModulationState(amDepthPercent = 101.0))

        assertFalse(result.isValid)
        assertEquals(previousModulation, controller.state.modulation)
    }

    @Test
    fun invalidFmDeviationDoesNotApply() {
        val controller = SimulatedSynthHdController()
        val previousModulation = controller.state.modulation

        val result = controller.setModulation(ModulationState(fmDeviationKhz = -1.0))

        assertFalse(result.isValid)
        assertEquals(previousModulation, controller.state.modulation)
    }

    @Test
    fun constructorSanitizesInvalidInitialModulation() {
        val controller = SimulatedSynthHdController(
            SynthDeviceState(modulation = ModulationState(amDepthPercent = 101.0)),
        )

        assertEquals(ModulationState(), controller.state.modulation)
    }

    @Test
    fun upwardSweepRejectsStartAboveStop() {
        val controller = SimulatedSynthHdController()
        val previousSweep = controller.state.sweep

        val result = controller.setSweep(
            SweepState(startMhz = 200.0, stopMhz = 100.0, direction = SweepDirection.Up),
        )

        assertFalse(result.isValid)
        assertEquals(previousSweep, controller.state.sweep)
    }

    @Test
    fun downwardSweepRejectsStartBelowStop() {
        val controller = SimulatedSynthHdController()

        val result = controller.setSweep(
            SweepState(startMhz = 100.0, stopMhz = 200.0, direction = SweepDirection.Down),
        )

        assertFalse(result.isValid)
    }

    @Test
    fun replaceStateSanitizesInvalidPersistedValues() {
        val controller = SimulatedSynthHdController()

        controller.replaceState(
            SynthDeviceState(
                channelA = ChannelState(frequencyMhz = Double.POSITIVE_INFINITY),
                sweep = SweepState(stepMhz = Double.NaN),
                hopList = listOf(HopPoint(Double.NaN, 0.0, 10)),
                modulation = ModulationState(amDepthPercent = 200.0),
            ),
        )

        assertEquals(ChannelState(), controller.state.channelA)
        assertEquals(SweepState(), controller.state.sweep)
        assertTrue(controller.state.hopList.isEmpty())
        assertEquals(ModulationState(), controller.state.modulation)
    }
}
