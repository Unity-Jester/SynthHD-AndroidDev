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
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.TriggerEdge
import com.windfreak.synthhd.domain.TriggerSource
import com.windfreak.synthhd.domain.TriggerState
import com.windfreak.synthhd.ui.components.Section

@Composable
fun TriggerScreen(
    trigger: TriggerState,
    onTrigger: (TriggerState) -> Unit,
    onSoftwareTrigger: () -> Unit,
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Trigger Status") {
            Text("Source: ${trigger.source}")
            Text("Edge: ${trigger.edge}")
            Text("State: ${trigger.mode}")
        }
        Section("Source") {
            Button(
                onClick = { onTrigger(trigger.copy(source = TriggerSource.Software)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Software Source")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onTrigger(trigger.copy(source = TriggerSource.External)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("External Source")
            }
        }
        Section("Edge") {
            Button(
                onClick = { onTrigger(trigger.copy(edge = TriggerEdge.Rising)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Rising Edge")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onTrigger(trigger.copy(edge = TriggerEdge.Falling)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Falling Edge")
            }
        }
        Section("Actions") {
            Button(
                onClick = { onTrigger(trigger.copy(mode = RunMode.Armed)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Arm")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onSoftwareTrigger,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Software Trigger")
            }
        }
    }
}
