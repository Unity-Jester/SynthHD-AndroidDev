package com.windfreak.synthhd.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class ListSelectionTest {
    @Test
    fun keepsAnyExistingHopPointSelectable() {
        assertEquals(20, coerceHopPointIndex(20, pointCount = 500))
        assertEquals(499, coerceHopPointIndex(499, pointCount = 500))
    }

    @Test
    fun clampsSelectedHopPointToExistingRows() {
        assertEquals(0, coerceHopPointIndex(-1, pointCount = 500))
        assertEquals(499, coerceHopPointIndex(500, pointCount = 500))
        assertEquals(0, coerceHopPointIndex(0, pointCount = 0))
    }
}
