package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.ModulationState
import com.windfreak.synthhd.domain.validateAmDepthPercent
import com.windfreak.synthhd.domain.validateFmDeviationKhz
import com.windfreak.synthhd.domain.validatePulseWidthUs
import com.windfreak.synthhd.ui.components.NumberField
import com.windfreak.synthhd.ui.components.Section
import com.windfreak.synthhd.ui.components.ToggleRow

@Composable
fun ModulationScreen(modulation: ModulationState, onModulation: (ModulationState) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Pulse") {
            ToggleRow("Pulse Modulation", modulation.pulseEnabled) {
                onModulation(modulation.copy(pulseEnabled = it))
            }
            Spacer(Modifier.height(12.dp))
            NumberField("Pulse Width", modulation.pulseWidthUs, "us", validator = ::validatePulseWidthUs) {
                onModulation(modulation.copy(pulseWidthUs = it))
            }
        }
        Section("AM / FM / Chirp") {
            ToggleRow("AM", modulation.amEnabled) {
                onModulation(modulation.copy(amEnabled = it))
            }
            Spacer(Modifier.height(8.dp))
            NumberField("AM Depth", modulation.amDepthPercent, "%", validator = ::validateAmDepthPercent) {
                onModulation(modulation.copy(amDepthPercent = it))
            }
            Spacer(Modifier.height(12.dp))
            ToggleRow("FM", modulation.fmEnabled) {
                onModulation(modulation.copy(fmEnabled = it))
            }
            Spacer(Modifier.height(8.dp))
            NumberField("FM Deviation", modulation.fmDeviationKhz, "kHz", validator = ::validateFmDeviationKhz) {
                onModulation(modulation.copy(fmDeviationKhz = it))
            }
            Spacer(Modifier.height(12.dp))
            ToggleRow("Chirp", modulation.chirpEnabled) {
                onModulation(modulation.copy(chirpEnabled = it))
            }
        }
    }
}
