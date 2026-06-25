package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.controller.HardwareDevice
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.ui.components.Section

@Composable
fun ExtrasScreen(
    state: SynthDeviceState,
    hardwareDevices: List<HardwareDevice>,
    isHardwareConnected: Boolean,
    onScanUsb: () -> Unit,
    onConnectUsb: () -> Unit,
    onDisconnectHardware: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
) {
    var confirmingReset by remember { mutableStateOf(false) }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Hardware Control") {
            Text("Mode: ${if (isHardwareConnected) "USB hardware" else "Offline simulator"}")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onScanUsb,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Scan USB Devices")
            }
            Spacer(Modifier.height(8.dp))
            if (hardwareDevices.isEmpty()) {
                Text("Detected devices: None")
            } else {
                hardwareDevices.forEach { device ->
                    Text("${device.label} (${device.vendorId.toString(16)}:${device.productId.toString(16)})")
                    Spacer(Modifier.height(4.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onConnectUsb,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Connect USB Device")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDisconnectHardware,
                modifier = Modifier.fillMaxWidth(),
                enabled = isHardwareConnected,
            ) {
                Text("Disconnect Hardware")
            }
        }
        Section("Settings") {
            Text("Last saved channel: ${state.savedSnapshot?.name ?: "None"}")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    confirmingReset = false
                    onSave()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Settings to Device")
            }
            Spacer(Modifier.height(8.dp))
            if (confirmingReset) {
                Text("Confirm reset settings?")
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
                OutlinedButton(
                    onClick = { confirmingReset = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(),
                ) {
                    Text("Reset Settings")
                }
            }
        }
    }
}
