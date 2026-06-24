package com.windfreak.synthhd.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.HopPoint
import com.windfreak.synthhd.domain.SynthConstants
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.ui.components.Section

@Composable
fun ListScreen(
    state: SynthDeviceState,
    onAdd: (HopPoint) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("List / Hop Table") {
            Text("${state.hopList.size} / ${SynthConstants.MAX_HOP_POINTS} points")
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onAdd(HopPoint(1_000.0 + state.hopList.size, 0.0, 10)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add Point")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Clear")
            }
            Spacer(Modifier.height(12.dp))
            state.hopList.take(20).forEachIndexed { index, point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "${index + 1}. ${point.frequencyMhz} MHz, ${point.powerDbm} dBm, ${point.dwellMs} ms",
                        modifier = Modifier.weight(1f),
                    )
                    Button(onClick = { onRemove(index) }) {
                        Text("Delete")
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
