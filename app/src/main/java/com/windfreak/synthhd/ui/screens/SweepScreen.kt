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
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.sweepDurationMs
import com.windfreak.synthhd.domain.sweepPointCount
import com.windfreak.synthhd.ui.components.IntField
import com.windfreak.synthhd.ui.components.NumberField
import com.windfreak.synthhd.ui.components.Section

@Composable
fun SweepScreen(
    sweep: SweepState,
    onSweep: (SweepState) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Linear Sweep") {
            NumberField("Start", sweep.startMhz, "MHz") { onSweep(sweep.copy(startMhz = it)) }
            Spacer(Modifier.height(12.dp))
            NumberField("Stop", sweep.stopMhz, "MHz") { onSweep(sweep.copy(stopMhz = it)) }
            Spacer(Modifier.height(12.dp))
            NumberField("Step", sweep.stepMhz, "MHz") { onSweep(sweep.copy(stepMhz = it)) }
            Spacer(Modifier.height(12.dp))
            IntField("Dwell", sweep.dwellMs, "ms") { onSweep(sweep.copy(dwellMs = it)) }
            Spacer(Modifier.height(12.dp))
            Text("Points: ${sweepPointCount(sweep)}")
            Text("Duration: ${sweepDurationMs(sweep)} ms")
            Text("State: ${sweep.runMode}")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Run")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Stop")
            }
        }
    }
}
