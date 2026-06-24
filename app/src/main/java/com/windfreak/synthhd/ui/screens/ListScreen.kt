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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                editorKey = "new-${state.hopList.size}",
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
            if (state.hopList.isNotEmpty()) {
                SelectedHopPointEditor(
                    points = state.hopList,
                    onUpdate = onUpdate,
                    onMove = onMove,
                    onRemove = onRemove,
                )
                Spacer(Modifier.height(12.dp))
            }
            state.hopList.take(20).forEachIndexed { index, point ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "${index + 1}. ${point.frequencyMhz} MHz, ${point.powerDbm} dBm, ${point.dwellMs} ms",
                    )
                }
            }
            if (state.hopList.size > 20) {
                Spacer(Modifier.height(8.dp))
                Text("Showing first 20 summaries")
            }
        }
    }
}

internal fun coerceHopPointIndex(index: Int, pointCount: Int): Int =
    if (pointCount <= 0) 0 else index.coerceIn(0, pointCount - 1)

@Composable
private fun SelectedHopPointEditor(
    points: List<HopPoint>,
    onUpdate: (Int, HopPoint) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedPointIndex = coerceHopPointIndex(selectedIndex, points.size)
    val selectedPoint = points[selectedPointIndex]

    Text("Selected point: ${selectedPointIndex + 1} of ${points.size}")
    PointSelector(
        selectedIndex = selectedPointIndex,
        pointCount = points.size,
        onSelect = { selectedIndex = it },
    )
    Spacer(Modifier.height(8.dp))
    HopPointEditor(
        editorKey = "point-$selectedPointIndex",
        point = selectedPoint,
        frequencyLabel = "Point ${selectedPointIndex + 1} Frequency",
        powerLabel = "Point ${selectedPointIndex + 1} Power",
        dwellLabel = "Point ${selectedPointIndex + 1} Dwell",
        actionLabel = "Update Point ${selectedPointIndex + 1}",
        onSubmit = { onUpdate(selectedPointIndex, it) },
    )
    Row(Modifier.fillMaxWidth()) {
        Button(
            onClick = {
                onMove(selectedPointIndex, -1)
                selectedIndex = coerceHopPointIndex(selectedPointIndex - 1, points.size)
            },
            enabled = selectedPointIndex > 0,
            modifier = Modifier.weight(1f),
        ) {
            Text("Move Point ${selectedPointIndex + 1} Up")
        }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                onMove(selectedPointIndex, 1)
                selectedIndex = coerceHopPointIndex(selectedPointIndex + 1, points.size)
            },
            enabled = selectedPointIndex < points.lastIndex,
            modifier = Modifier.weight(1f),
        ) {
            Text("Move Point ${selectedPointIndex + 1} Down")
        }
    }
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            onRemove(selectedPointIndex)
            selectedIndex = coerceHopPointIndex(selectedPointIndex, points.size - 1)
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Delete Point ${selectedPointIndex + 1}")
    }
}

@Composable
private fun PointSelector(
    selectedIndex: Int,
    pointCount: Int,
    onSelect: (Int) -> Unit,
) {
    val selectedText = remember(selectedIndex, pointCount) { mutableStateOf((selectedIndex + 1).toString()) }
    val errorText = remember(selectedIndex, pointCount) { mutableStateOf<String?>(null) }

    OutlinedTextField(
        value = selectedText.value,
        onValueChange = {
            selectedText.value = it
            errorText.value = null
        },
        label = { Text("Selected Point") },
        suffix = { Text("1-$pointCount") },
        isError = errorText.value != null,
        supportingText = { errorText.value?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            val selectedPointNumber = selectedText.value.toIntOrNull()
            errorText.value = when {
                selectedPointNumber == null -> "Enter a valid whole number"
                selectedPointNumber !in 1..pointCount -> "Point must be between 1 and $pointCount."
                else -> null
            }
            if (selectedPointNumber != null && errorText.value == null) {
                onSelect(selectedPointNumber - 1)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Select Point")
    }
}

@Composable
private fun HopPointEditor(
    editorKey: Any,
    point: HopPoint,
    frequencyLabel: String,
    powerLabel: String,
    dwellLabel: String,
    actionLabel: String,
    onSubmit: (HopPoint) -> Unit,
) {
    val frequencyText = remember(editorKey, point.frequencyMhz) { mutableStateOf(point.frequencyMhz.toString()) }
    val powerText = remember(editorKey, point.powerDbm) { mutableStateOf(point.powerDbm.toString()) }
    val dwellText = remember(editorKey, point.dwellMs) { mutableStateOf(point.dwellMs.toString()) }
    val frequencyError = remember(editorKey, point.frequencyMhz) { mutableStateOf<String?>(null) }
    val powerError = remember(editorKey, point.powerDbm) { mutableStateOf<String?>(null) }
    val dwellError = remember(editorKey, point.dwellMs) { mutableStateOf<String?>(null) }

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
