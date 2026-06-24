package com.windfreak.synthhd.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.ValidationResult

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
    validator: ((Double) -> ValidationResult)? = null,
    onApply: (Double) -> Unit,
) {
    val text = remember(value) { mutableStateOf(value.toString()) }
    val errorMessage = remember(value) { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text.value,
            onValueChange = {
                text.value = it
                errorMessage.value = null
            },
            label = { Text(label) },
            suffix = { Text(suffix) },
            isError = errorMessage.value != null,
            supportingText = {
                errorMessage.value?.let {
                    Text(it)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                text.value.toDoubleOrNull()?.let {
                    val result = validator?.invoke(it) ?: ValidationResult(true)
                    if (result.isValid) {
                        errorMessage.value = null
                        onApply(it)
                    } else {
                        errorMessage.value = result.message
                    }
                } ?: run {
                    errorMessage.value = "Enter a valid number"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Apply $label")
        }
    }
}

@Composable
fun IntField(
    label: String,
    value: Int,
    suffix: String,
    validator: ((Int) -> ValidationResult)? = null,
    onApply: (Int) -> Unit,
) {
    val text = remember(value) { mutableStateOf(value.toString()) }
    val errorMessage = remember(value) { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text.value,
            onValueChange = {
                text.value = it
                errorMessage.value = null
            },
            label = { Text(label) },
            suffix = { Text(suffix) },
            isError = errorMessage.value != null,
            supportingText = {
                errorMessage.value?.let {
                    Text(it)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                text.value.toIntOrNull()?.let {
                    val result = validator?.invoke(it) ?: ValidationResult(true)
                    if (result.isValid) {
                        errorMessage.value = null
                        onApply(it)
                    } else {
                        errorMessage.value = result.message
                    }
                } ?: run {
                    errorMessage.value = "Enter a valid whole number"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Apply $label")
        }
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
            )
            .padding(vertical = 6.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}
