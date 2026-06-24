package com.windfreak.synthhd.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SweepMathTest {
    @Test
    fun calculatesInclusiveUpSweepPointCount() {
        val sweep = SweepState(startMhz = 100.0, stopMhz = 110.0, stepMhz = 5.0, dwellMs = 20)

        assertEquals(3, sweepPointCount(sweep))
        assertEquals(60, sweepDurationMs(sweep))
    }

    @Test
    fun calculatesInclusiveDownSweepPointCount() {
        val sweep = SweepState(
            startMhz = 110.0,
            stopMhz = 100.0,
            stepMhz = 5.0,
            dwellMs = 20,
            direction = SweepDirection.Down,
        )

        assertEquals(3, sweepPointCount(sweep))
        assertEquals(listOf(110.0, 105.0, 100.0), generateSweepFrequencies(sweep))
    }

    @Test
    fun rejectsSweepWithZeroStep() {
        val sweep = SweepState(stepMhz = 0.0)

        assertEquals(0, sweepPointCount(sweep))
        assertEquals(emptyList<Double>(), generateSweepFrequencies(sweep))
    }
}
