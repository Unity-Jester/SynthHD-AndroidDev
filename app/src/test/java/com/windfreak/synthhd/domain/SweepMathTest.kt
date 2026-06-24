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

    @Test
    fun includesDecimalStepEndpointWithinPrecision() {
        val sweep = SweepState(startMhz = 100.0, stopMhz = 100.3, stepMhz = 0.1)

        assertEquals(4, sweepPointCount(sweep))
        assertEquals(listOf(100.0, 100.1, 100.2, 100.3), generateSweepFrequencies(sweep))
    }

    @Test
    fun stopsNonDivisibleSpanBeforeExceedingStop() {
        val sweep = SweepState(startMhz = 100.0, stopMhz = 101.0, stepMhz = 0.3)

        assertEquals(4, sweepPointCount(sweep))
        assertEquals(listOf(100.0, 100.3, 100.6, 100.9), generateSweepFrequencies(sweep))
    }

    @Test
    fun reportsRealPointCountWhileCappingGeneratedFrequencies() {
        val sweep = SweepState(startMhz = 100.0, stopMhz = 200.0, stepMhz = 0.000001)

        assertEquals(100_000_001, sweepPointCount(sweep))
        assertEquals(MAX_GENERATED_SWEEP_POINTS, generateSweepFrequencies(sweep).size)
    }

    @Test
    fun usesRealPointCountForLargeDurationWhenItFitsInInt() {
        val sweep = SweepState(startMhz = 100.0, stopMhz = 200.0, stepMhz = 0.000001, dwellMs = 10)

        assertEquals(1_000_000_010, sweepDurationMs(sweep))
    }

    @Test
    fun saturatesDurationOverflow() {
        val sweep = SweepState(
            startMhz = 100.0,
            stopMhz = 200.0,
            stepMhz = 0.000001,
            dwellMs = Int.MAX_VALUE,
        )

        assertEquals(Int.MAX_VALUE, sweepDurationMs(sweep))
    }
}
