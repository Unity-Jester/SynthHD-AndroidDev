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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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

/**
 * Toggles a leading minus sign on numeric draft text. Android numeric keypads
 * offer no minus key, so signed fields expose this through a +/- button.
 */
fun toggleSign(text: String): String =
    if (text.startsWith("-")) text.removePrefix("-") else "-$text"

@Composable
private fun SignToggleButton(text: String, onTextChange: (String) -> Unit) {
    TextButton(onClick = { onTextChange(toggleSign(text)) }) {
        Text("±")
    }
}

@Composable
fun NumberField(
    label: String,
    value: Double,
    suffix: String,
    allowNegative: Boolean = false,
    validator: ((Double) -> ValidationResult)? = null,
    onApply: (Double) -> Unit,
) {
    var text by remember { mutableStateOf(value.toString()) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        // Sync external changes, but never clobber an edit in progress.
        if (!focused) text = value.toString()
    }

    val parsed = text.toDoubleOrNull()
    val errorMessage = when {
        parsed == null -> "Enter a valid number"
        else -> validator?.invoke(parsed)?.takeIf { !it.isValid }?.message
    }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label) },
            suffix = { Text(suffix) },
            leadingIcon = if (allowNegative) {
                { SignToggleButton(text) { text = it } }
            } else {
                null
            },
            isError = errorMessage != null,
            supportingText = { errorMessage?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { parsed?.let(onApply) },
            enabled = errorMessage == null,
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
    var text by remember { mutableStateOf(value.toString()) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        if (!focused) text = value.toString()
    }

    val parsed = text.toIntOrNull()
    val errorMessage = when {
        parsed == null -> "Enter a valid whole number"
        else -> validator?.invoke(parsed)?.takeIf { !it.isValid }?.message
    }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label) },
            suffix = { Text(suffix) },
            isError = errorMessage != null,
            supportingText = { errorMessage?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { parsed?.let(onApply) },
            enabled = errorMessage == null,
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
