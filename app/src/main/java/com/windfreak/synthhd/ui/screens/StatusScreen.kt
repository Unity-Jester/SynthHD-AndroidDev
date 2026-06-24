package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.DeviceStatus
import com.windfreak.synthhd.ui.components.Section

@Composable
fun StatusScreen(status: DeviceStatus) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Connection") {
            StatusLine("Connection", status.connectedLabel)
            StatusLine("Model", status.model)
            StatusLine("Serial", status.serial)
            StatusLine("Firmware", status.firmware)
        }
        Section("Health") {
            StatusLine("Calibration", status.calibrationDate)
            StatusLine("Temperature", "${status.temperatureC} C")
            StatusLine("Reference", status.referenceMode.toString())
            StatusLine("Lock Detect", if (status.lockDetect) "Locked" else "Unlocked")
            StatusLine("Level", if (status.levelOk) "OK" else "Warning")
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Text("$label: $value")
    Spacer(Modifier.height(6.dp))
}
