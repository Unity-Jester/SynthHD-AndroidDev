package com.windfreak.synthhd.domain

fun validateFrequencyMhz(value: Double): ValidationResult =
    if (value in SynthConstants.MIN_FREQUENCY_MHZ..SynthConstants.MAX_FREQUENCY_MHZ) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Frequency must be between 10 MHz and 24000 MHz.")
    }

fun validatePowerDbm(value: Double): ValidationResult =
    if (value in SynthConstants.MIN_POWER_DBM..SynthConstants.MAX_POWER_DBM) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Power must be between -40 dBm and 18 dBm.")
    }

fun validatePhaseDegrees(value: Double): ValidationResult =
    if (value in SynthConstants.MIN_PHASE_DEGREES..SynthConstants.MAX_PHASE_DEGREES) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Phase must be between 0 and 360 degrees.")
    }

fun validatePositiveDwellMs(value: Int): ValidationResult =
    if (value > 0) ValidationResult(true) else ValidationResult(false, "Dwell time must be greater than 0 ms.")

fun validateHopListSize(size: Int): ValidationResult =
    if (size <= SynthConstants.MAX_HOP_POINTS) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "List mode supports up to 500 points.")
    }
