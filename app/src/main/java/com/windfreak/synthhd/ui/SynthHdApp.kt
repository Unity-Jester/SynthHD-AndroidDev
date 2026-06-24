package com.windfreak.synthhd.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.ChannelId

private val tabLabels = listOf("Generator", "Sweep", "List", "Mod", "Trigger", "Status", "Extras")

@Composable
fun SynthHdApp(viewModel: SynthHdViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val state = viewModel.state

    MaterialTheme {
        Scaffold(
            topBar = {
                Column(Modifier.padding(16.dp)) {
                    Text("SynthHD Pro Simulator", style = MaterialTheme.typography.titleLarge)
                    Text(state.status.connectedLabel, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        AssistChip(
                            onClick = { viewModel.selectChannel(ChannelId.A) },
                            label = { Text("Channel A ${if (state.activeChannel == ChannelId.A) "active" else ""}") },
                        )
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = { viewModel.selectChannel(ChannelId.B) },
                            label = { Text("Channel B ${if (state.activeChannel == ChannelId.B) "active" else ""}") },
                        )
                    }
                }
            },
            bottomBar = {
                Surface(tonalElevation = 3.dp) {
                    Text(
                        text = "Readback: Temp ${state.status.temperatureC} C | Ref ${state.status.referenceMode} | Level ${if (state.status.levelOk) "OK" else "WARN"} | ${viewModel.message}",
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                    )
                }
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabLabels.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(label) },
                        )
                    }
                }
                Text(
                    text = tabLabels[selectedTab],
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}
