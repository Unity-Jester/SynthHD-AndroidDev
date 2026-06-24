package com.windfreak.synthhd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
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

    @Test
    fun generatorShowsRangeErrorAtFrequencyField() {
        replaceTextField(0, "1")
        clickText("Apply Frequency")

        composeRule
            .onNodeWithText("Frequency must be between 10 MHz and 24000 MHz.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun sweepExposesDirectionAndTriggeredRunControls() {
        clickTab("Sweep")

        composeRule.onNodeWithText("Direction: Up").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Direction Down").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Continuous Run").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Arm Triggered").performScrollTo().assertIsDisplayed()

        clickText("Direction Down")
        composeRule.onNodeWithText("Direction: Down").performScrollTo().assertIsDisplayed()

        clickText("Arm Triggered")

        composeRule.onNodeWithText("State: Armed").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun listScreenCanAddEditMoveAndRunPoints() {
        clickTab("List")

        composeRule.onNodeWithText("New Frequency").performScrollTo().assertIsDisplayed()
        replaceTextField(0, "2450")
        replaceTextField(1, "-3")
        replaceTextField(2, "25")
        clickText("Add Point")

        composeRule.onNodeWithText("Point 1 Frequency").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Update Point 1").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Run List").performScrollTo().assertIsDisplayed()

        replaceTextField(3, "2500")
        clickText("Update Point 1")

        composeRule.onNodeWithText("2500.0 MHz", substring = true).performScrollTo().assertIsDisplayed()

        replaceTextField(0, "2600")
        clickText("Add Point")
        composeRule.onNodeWithText("Move Point 2 Up").performScrollTo().assertIsDisplayed()

        clickText("Run List")
        composeRule.onNodeWithText("List state: Running").performScrollTo().assertIsDisplayed()

        clickText("Stop List")
        composeRule.onNodeWithText("List state: Idle").performScrollTo().assertIsDisplayed()
    }

    private fun clickTab(label: String) {
        composeRule
            .onNode(hasText(label) and hasClickAction())
            .performScrollTo()
            .performClick()
    }

    private fun replaceTextField(index: Int, value: String) {
        composeRule
            .onAllNodes(hasSetTextAction())[index]
            .performScrollTo()
            .performTextReplacement(value)
    }

    private fun clickText(label: String) {
        composeRule
            .onNodeWithText(label)
            .performScrollTo()
            .performClick()
    }
}
