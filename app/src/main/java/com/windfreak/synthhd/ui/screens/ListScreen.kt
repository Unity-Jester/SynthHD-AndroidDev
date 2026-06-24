package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.SynthConstants
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.validateFrequencyMhz
import com.windfreak.synthhd.domain.validatePositiveDwellMs
import com.windfreak.synthhd.domain.validatePowerDbm
import com.windfreak.synthhd.ui.components.Section

@Composable
fun ListScreen(
    state: SynthDeviceState,
    onAdd: (HopPoint) -> Unit,
    onUpdate: (Int, HopPoint) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("List / Hop Table") {
            Text("${state.hopList.size} / ${SynthConstants.MAX_HOP_POINTS} points")
            Text("List state: ${state.listRunMode}")
            Spacer(Modifier.height(12.dp))
            HopPointEditor(
                point = HopPoint(1_000.0 + state.hopList.size, 0.0, 10),
                frequencyLabel = "New Frequency",
                powerLabel = "New Power",
                dwellLabel = "New Dwell",
                actionLabel = "Add Point",
                onSubmit = onAdd,
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Run List")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Stop List")
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                Text("Clear")
            }
            Spacer(Modifier.height(12.dp))
            state.hopList.take(20).forEachIndexed { index, point ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "${index + 1}. ${point.frequencyMhz} MHz, ${point.powerDbm} dBm, ${point.dwellMs} ms",
                    )
                    HopPointEditor(
                        point = point,
                        frequencyLabel = "Point ${index + 1} Frequency",
                        powerLabel = "Point ${index + 1} Power",
                        dwellLabel = "Point ${index + 1} Dwell",
                        actionLabel = "Update Point ${index + 1}",
                        onSubmit = { onUpdate(index, it) },
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onMove(index, -1) },
                            enabled = index > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Move Point ${index + 1} Up")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { onMove(index, 1) },
                            enabled = index < state.hopList.lastIndex,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Move Point ${index + 1} Down")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onRemove(index) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Delete Point ${index + 1}")
                    }
                }
            }
            if (state.hopList.size > 20) {
                Spacer(Modifier.height(8.dp))
                Text("Showing first 20 points")
            }
        }
    }
}

@Composable
private fun HopPointEditor(
    point: HopPoint,
    frequencyLabel: String,
    powerLabel: String,
    dwellLabel: String,
    actionLabel: String,
    onSubmit: (HopPoint) -> Unit,
) {
    val frequencyText = remember(point.frequencyMhz) { mutableStateOf(point.frequencyMhz.toString()) }
    val powerText = remember(point.powerDbm) { mutableStateOf(point.powerDbm.toString()) }
    val dwellText = remember(point.dwellMs) { mutableStateOf(point.dwellMs.toString()) }
    val frequencyError = remember(point.frequencyMhz) { mutableStateOf<String?>(null) }
    val powerError = remember(point.powerDbm) { mutableStateOf<String?>(null) }
    val dwellError = remember(point.dwellMs) { mutableStateOf<String?>(null) }

    OutlinedTextField(
        value = frequencyText.value,
        onValueChange = {
            frequencyText.value = it
            frequencyError.value = null
        },
        label = { Text(frequencyLabel) },
        suffix = { Text("MHz") },
        isError = frequencyError.value != null,
        supportingText = { frequencyError.value?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = powerText.value,
        onValueChange = {
            powerText.value = it
            powerError.value = null
        },
        label = { Text(powerLabel) },
        suffix = { Text("dBm") },
        isError = powerError.value != null,
        supportingText = { powerError.value?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = dwellText.value,
        onValueChange = {
            dwellText.value = it
            dwellError.value = null
        },
        label = { Text(dwellLabel) },
        suffix = { Text("ms") },
        isError = dwellError.value != null,
        supportingText = { dwellError.value?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            val frequency = frequencyText.value.toDoubleOrNull()
            val power = powerText.value.toDoubleOrNull()
            val dwell = dwellText.value.toIntOrNull()

            frequencyError.value = when {
                frequency == null -> "Enter a valid number"
                !validateFrequencyMhz(frequency).isValid -> validateFrequencyMhz(frequency).message
                else -> null
            }
            powerError.value = when {
                power == null -> "Enter a valid number"
                !validatePowerDbm(power).isValid -> validatePowerDbm(power).message
                else -> null
            }
            dwellError.value = when {
                dwell == null -> "Enter a valid whole number"
                !validatePositiveDwellMs(dwell).isValid -> validatePositiveDwellMs(dwell).message
                else -> null
            }

            if (frequency != null && power != null && dwell != null &&
                frequencyError.value == null && powerError.value == null && dwellError.value == null
            ) {
                onSubmit(HopPoint(frequency, power, dwell))
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(actionLabel)
    }
}
