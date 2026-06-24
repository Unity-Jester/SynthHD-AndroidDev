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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.ui.screens.ExtrasScreen
import com.windfreak.synthhd.ui.screens.GeneratorScreen
import com.windfreak.synthhd.ui.screens.ListScreen
import com.windfreak.synthhd.ui.screens.ModulationScreen
import com.windfreak.synthhd.ui.screens.StatusScreen
import com.windfreak.synthhd.ui.screens.SweepScreen
import com.windfreak.synthhd.ui.screens.TriggerScreen

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
                PrimaryScrollableTabRow(selectedTabIndex = selectedTab) {
                    tabLabels.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(label) },
                        )
                    }
                }
                when (selectedTab) {
                    0 -> GeneratorScreen(
                        state = state,
                        onFrequency = viewModel::setFrequencyMhz,
                        onPower = viewModel::setPowerDbm,
                        onPhase = viewModel::setPhaseDegrees,
                        onRf = viewModel::setRfEnabled,
                        onLock = viewModel::setChannelLocked,
                        onReference = viewModel::setReferenceMode,
                    )
                    1 -> SweepScreen(
                        sweep = state.sweep,
                        onSweep = viewModel::setSweep,
                        onStart = viewModel::startSweep,
                        onArmTriggered = viewModel::armTriggeredSweep,
                        onStop = viewModel::stopSweep,
                    )
                    2 -> ListScreen(
                        state = state,
                        onAdd = viewModel::addHopPoint,
                        onUpdate = viewModel::updateHopPoint,
                        onMove = viewModel::moveHopPoint,
                        onRemove = viewModel::removeHopPoint,
                        onClear = viewModel::clearHopList,
                        onStart = viewModel::startHopList,
                        onStop = viewModel::stopHopList,
                    )
                    3 -> ModulationScreen(state.modulation, viewModel::setModulation)
                    4 -> TriggerScreen(
                        state.trigger,
                        viewModel::setTrigger,
                        viewModel::softwareTrigger,
                    )
                    5 -> StatusScreen(state.status)
                    6 -> ExtrasScreen(state, viewModel::saveToDevice, viewModel::resetToDefaults)
                }
            }
        }
    }
}
