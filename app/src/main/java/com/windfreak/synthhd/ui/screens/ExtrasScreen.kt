package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.ui.components.Section

@Composable
fun ExtrasScreen(state: SynthDeviceState, onSave: () -> Unit, onReset: () -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Simulator") {
            Text("Last saved channel: ${state.savedSnapshot?.name ?: "None"}")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Simulated Settings")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset Simulator")
            }
        }
    }
}
