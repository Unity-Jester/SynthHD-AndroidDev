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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.ui.components.Section

@Composable
fun ExtrasScreen(state: SynthDeviceState, onSave: () -> Unit, onReset: () -> Unit) {
    var confirmingReset by remember { mutableStateOf(false) }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Simulator") {
            Text("Last saved channel: ${state.savedSnapshot?.name ?: "None"}")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    confirmingReset = false
                    onSave()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Simulated Settings")
            }
            Spacer(Modifier.height(8.dp))
            if (confirmingReset) {
                Text("Confirm reset simulator?")
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        confirmingReset = false
                        onReset()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Confirm Reset")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { confirmingReset = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            } else {
                Button(
                    onClick = { confirmingReset = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset Simulator")
                }
            }
        }
    }
}
