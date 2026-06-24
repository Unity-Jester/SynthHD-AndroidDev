package com.windfreak.synthhd.domain

import kotlin.math.floor
import kotlin.math.round

const val MAX_GENERATED_SWEEP_POINTS = 100_000

private const val SWEEP_POINT_EPSILON = 1e-9
private const val SWEEP_FREQUENCY_ROUNDING_SCALE = 1_000_000_000.0

fun sweepPointCount(sweep: SweepState): Int {
    if (sweep.stepMhz <= 0.0 || sweep.dwellMs <= 0) return 0
    val span = when (sweep.direction) {
        SweepDirection.Up -> sweep.stopMhz - sweep.startMhz
        SweepDirection.Down -> sweep.startMhz - sweep.stopMhz
    }
    if (span < 0.0) return 0
    val intervals = floor(span / sweep.stepMhz + SWEEP_POINT_EPSILON)
    if (intervals >= MAX_GENERATED_SWEEP_POINTS - 1) return MAX_GENERATED_SWEEP_POINTS
    return intervals.toInt() + 1
}

fun sweepDurationMs(sweep: SweepState): Int {
    val durationMs = sweepPointCount(sweep).toLong() * sweep.dwellMs.toLong()
    return if (durationMs > Int.MAX_VALUE) Int.MAX_VALUE else durationMs.toInt()
}

fun generateSweepFrequencies(sweep: SweepState): List<Double> {
    val count = sweepPointCount(sweep)
    if (count == 0) return emptyList()
    return List(count) { index ->
        val frequency = when (sweep.direction) {
            SweepDirection.Up -> sweep.startMhz + index * sweep.stepMhz
            SweepDirection.Down -> sweep.startMhz - index * sweep.stepMhz
        }
        round(frequency * SWEEP_FREQUENCY_ROUNDING_SCALE) / SWEEP_FREQUENCY_ROUNDING_SCALE
    }
}
