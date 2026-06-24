package com.windfreak.synthhd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class SynthHdAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensToSimulatorGenerator() {
        composeRule.onNodeWithText("SynthHD Pro Simulator").assertIsDisplayed()
        composeRule.onNodeWithText("Offline simulator").assertIsDisplayed()
        composeRule.onNodeWithText("Generator").assertIsDisplayed()
    }

    @Test
    fun navigatesAcrossMajorScreens() {
        clickTab("Sweep")
        composeRule.onNodeWithText("Linear Sweep").assertIsDisplayed()

        clickTab("List")
        composeRule.onNodeWithText("List / Hop Table").assertIsDisplayed()

        clickTab("Mod")
        composeRule.onNodeWithText("Pulse Modulation").assertIsDisplayed()

        clickTab("Trigger")
        composeRule.onNodeWithText("Trigger Status").assertIsDisplayed()

        clickTab("Status")
        composeRule.onNodeWithText("Connection: Offline simulator").assertIsDisplayed()

        clickTab("Extras")
        composeRule.onNodeWithText("Last saved channel: None").assertIsDisplayed()
    }

    private fun clickTab(label: String) {
        composeRule
            .onNode(hasText(label) and hasClickAction())
            .performScrollTo()
            .performClick()
    }
}
