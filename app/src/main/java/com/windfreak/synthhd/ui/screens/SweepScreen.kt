package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.RunMode
import com.windfreak.synthhd.domain.SweepDirection
import com.windfreak.synthhd.domain.SweepState
import com.windfreak.synthhd.domain.ValidationResult
import com.windfreak.synthhd.domain.sweepDurationMs
import com.windfreak.synthhd.domain.sweepPointCount
import com.windfreak.synthhd.domain.validateFrequencyMhz
import com.windfreak.synthhd.domain.validatePositiveDwellMs
import com.windfreak.synthhd.ui.components.IntField
import com.windfreak.synthhd.ui.components.NumberField
import com.windfreak.synthhd.ui.components.Section

@Composable
fun SweepScreen(
    sweep: SweepState,
    onSweep: (SweepState) -> Unit,
    onStart: () -> Unit,
    onArmTriggered: () -> Unit,
    onStop: () -> Unit,
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Linear Sweep") {
            NumberField("Start", sweep.startMhz, "MHz", validator = ::validateFrequencyMhz) {
                onSweep(sweep.copy(startMhz = it))
            }
            Spacer(Modifier.height(12.dp))
            NumberField("Stop", sweep.stopMhz, "MHz", validator = ::validateFrequencyMhz) {
                onSweep(sweep.copy(stopMhz = it))
            }
            Spacer(Modifier.height(12.dp))
            NumberField("Step", sweep.stepMhz, "MHz", validator = ::validateSweepStepMhz) {
                onSweep(sweep.copy(stepMhz = it))
            }
            Spacer(Modifier.height(12.dp))
            IntField("Dwell", sweep.dwellMs, "ms", validator = ::validatePositiveDwellMs) {
                onSweep(sweep.copy(dwellMs = it))
            }
            Spacer(Modifier.height(12.dp))
            Text("Direction: ${sweep.direction}")
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onSweep(sweep.withDirection(SweepDirection.Up)) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Direction Up")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onSweep(sweep.withDirection(SweepDirection.Down)) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Direction Down")
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Points: ${sweepPointCount(sweep)}")
            Text("Duration: ${sweepDurationMs(sweep)} ms")
            Text("State: ${sweep.runMode}")
            Text("Run behavior: ${if (sweep.runMode == RunMode.Armed) "Triggered" else "Continuous"}")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continuous Run")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onArmTriggered,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Arm Triggered")
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

private fun validateSweepStepMhz(value: Double): ValidationResult =
    if (value.isFinite() && value > 0.0) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Sweep step must be greater than 0 MHz.")
    }

private fun SweepState.withDirection(direction: SweepDirection): SweepState =
    when {
        direction == SweepDirection.Up && startMhz > stopMhz -> copy(
            startMhz = stopMhz,
            stopMhz = startMhz,
            direction = direction,
        )
        direction == SweepDirection.Down && startMhz < stopMhz -> copy(
            startMhz = stopMhz,
            stopMhz = startMhz,
            direction = direction,
        )
        else -> copy(direction = direction)
    }
