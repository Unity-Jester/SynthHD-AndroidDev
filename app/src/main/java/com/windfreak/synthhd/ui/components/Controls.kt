package com.windfreak.synthhd.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: Double,
    suffix: String,
    onApply: (Double) -> Unit,
) {
    val text = remember(value) { mutableStateOf(value.toString()) }
    Row(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text(label) },
            suffix = { Text(suffix) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = { text.value.toDoubleOrNull()?.let(onApply) }) {
            Text("Apply")
        }
    }
}

@Composable
fun IntField(
    label: String,
    value: Int,
    suffix: String,
    onApply: (Int) -> Unit,
) {
    val text = remember(value) { mutableStateOf(value.toString()) }
    Row(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text(label) },
            suffix = { Text(suffix) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = { text.value.toIntOrNull()?.let(onApply) }) {
            Text("Apply")
        }
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
