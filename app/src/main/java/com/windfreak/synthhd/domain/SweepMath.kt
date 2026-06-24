package com.windfreak.synthhd.domain

import kotlin.math.floor

fun sweepPointCount(sweep: SweepState): Int {
    if (sweep.stepMhz <= 0.0 || sweep.dwellMs <= 0) return 0
    val span = when (sweep.direction) {
        SweepDirection.Up -> sweep.stopMhz - sweep.startMhz
        SweepDirection.Down -> sweep.startMhz - sweep.stopMhz
    }
    if (span < 0.0) return 0
    return floor(span / sweep.stepMhz).toInt() + 1
}

fun sweepDurationMs(sweep: SweepState): Int = sweepPointCount(sweep) * sweep.dwellMs

fun generateSweepFrequencies(sweep: SweepState): List<Double> {
    val count = sweepPointCount(sweep)
    if (count == 0) return emptyList()
    return List(count) { index ->
        when (sweep.direction) {
            SweepDirection.Up -> sweep.startMhz + index * sweep.stepMhz
            SweepDirection.Down -> sweep.startMhz - index * sweep.stepMhz
        }
    }
}
