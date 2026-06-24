package com.windfreak.synthhd.domain

import kotlin.math.floor
import kotlin.math.round

const val MAX_GENERATED_SWEEP_POINTS = 100_000

private const val SWEEP_POINT_EPSILON = 1e-9
private const val SWEEP_FREQUENCY_ROUNDING_SCALE = 1_000_000_000.0

private fun realSweepPointCount(sweep: SweepState): Long {
    if (sweep.stepMhz <= 0.0) return 0
    val span = when (sweep.direction) {
        SweepDirection.Up -> sweep.stopMhz - sweep.startMhz
        SweepDirection.Down -> sweep.startMhz - sweep.stopMhz
    }
    if (span < 0.0) return 0
    val intervals = floor(span / sweep.stepMhz + SWEEP_POINT_EPSILON)
    if (!intervals.isFinite() || intervals >= Long.MAX_VALUE - 1) return Long.MAX_VALUE
    return intervals.toLong() + 1
}

fun sweepPointCount(sweep: SweepState): Int {
    val realCount = realSweepPointCount(sweep)
    return if (realCount > Int.MAX_VALUE) Int.MAX_VALUE else realCount.toInt()
}

fun sweepDurationMs(sweep: SweepState): Int {
    if (sweep.dwellMs <= 0) return 0
    val realCount = realSweepPointCount(sweep)
    if (realCount > Int.MAX_VALUE / sweep.dwellMs) return Int.MAX_VALUE
    val durationMs = realCount * sweep.dwellMs.toLong()
    return if (durationMs > Int.MAX_VALUE) Int.MAX_VALUE else durationMs.toInt()
}

fun generateSweepFrequencies(sweep: SweepState): List<Double> {
    val count = minOf(sweepPointCount(sweep), MAX_GENERATED_SWEEP_POINTS)
    if (count == 0) return emptyList()
    return List(count) { index ->
        val frequency = when (sweep.direction) {
            SweepDirection.Up -> sweep.startMhz + index * sweep.stepMhz
            SweepDirection.Down -> sweep.startMhz - index * sweep.stepMhz
        }
        round(frequency * SWEEP_FREQUENCY_ROUNDING_SCALE) / SWEEP_FREQUENCY_ROUNDING_SCALE
    }
}
