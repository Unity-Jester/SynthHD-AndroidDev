package com.windfreak.synthhd.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SynthValidationTest {
    @Test
    fun acceptsDocumentedGeneratorRanges() {
        assertTrue(validateFrequencyMhz(10.0).isValid)
        assertTrue(validateFrequencyMhz(24_000.0).isValid)
        assertTrue(validatePowerDbm(-40.0).isValid)
        assertTrue(validatePowerDbm(18.0).isValid)
        assertTrue(validatePhaseDegrees(0.0).isValid)
        assertTrue(validatePhaseDegrees(360.0).isValid)
    }

    @Test
    fun rejectsOutOfRangeGeneratorValues() {
        assertEquals("Frequency must be between 10 MHz and 24000 MHz.", validateFrequencyMhz(9.9).message)
        assertEquals("Power must be between -40 dBm and 18 dBm.", validatePowerDbm(18.1).message)
        assertEquals("Phase must be between 0 and 360 degrees.", validatePhaseDegrees(-0.1).message)
    }

    @Test
    fun rejectsListTablesAboveFiveHundredRows() {
        assertTrue(validateHopListSize(500).isValid)
        assertEquals("List mode supports up to 500 points.", validateHopListSize(501).message)
    }

    @Test
    fun acceptsDocumentedModulationRanges() {
        assertTrue(validatePulseWidthUs(0.1).isValid)
        assertTrue(validateAmDepthPercent(0.0).isValid)
        assertTrue(validateAmDepthPercent(100.0).isValid)
        assertTrue(validateFmDeviationKhz(0.0).isValid)
    }

    @Test
    fun rejectsOutOfRangeModulationValues() {
        assertEquals("Pulse width must be greater than 0 us.", validatePulseWidthUs(0.0).message)
        assertEquals("AM depth must be between 0% and 100%.", validateAmDepthPercent(100.1).message)
        assertEquals("FM deviation must be 0 kHz or greater.", validateFmDeviationKhz(-0.1).message)
    }
}
