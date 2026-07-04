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
import com.windfreak.synthhd.domain.ChannelId
import com.windfreak.synthhd.domain.ReferenceMode
import com.windfreak.synthhd.domain.SynthConstants
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.validateFrequencyMhz
import com.windfreak.synthhd.domain.validatePhaseDegrees
import com.windfreak.synthhd.domain.validatePowerDbm
import com.windfreak.synthhd.ui.components.NumberField
import com.windfreak.synthhd.ui.components.Section
import com.windfreak.synthhd.ui.components.ToggleRow

@Composable
fun GeneratorScreen(
    state: SynthDeviceState,
    onFrequency: (Double) -> Unit,
    onPower: (Double) -> Unit,
    onPhase: (Double) -> Unit,
    onRf: (Boolean) -> Unit,
    onLock: (Boolean) -> Unit,
    onReference: (ReferenceMode) -> Unit,
) {
    val channel = when (state.activeChannel) {
        ChannelId.A -> state.channelA
        ChannelId.B -> state.channelB
    }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Section("Generator ${state.activeChannel}") {
            NumberField("Frequency", channel.frequencyMhz, "MHz", validator = ::validateFrequencyMhz, onApply = onFrequency)
            Spacer(Modifier.height(12.dp))
            NumberField("Power", channel.powerDbm, "dBm", allowNegative = true, validator = ::validatePowerDbm, onApply = onPower)
            Spacer(Modifier.height(12.dp))
            NumberField("Phase", channel.phaseDegrees, "deg", validator = ::validatePhaseDegrees, onApply = onPhase)
            Spacer(Modifier.height(12.dp))
            ToggleRow("RF Output", channel.rfEnabled, onRf)
            ToggleRow("Channel Lock", channel.locked, onLock)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onReference(ReferenceMode.Internal) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Internal Ref")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onReference(ReferenceMode.External) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("External Ref")
            }
            Spacer(Modifier.height(8.dp))
            Text("Reference: ${state.status.referenceMode}")
            Text(
                "Frequency range: ${SynthConstants.MIN_FREQUENCY_MHZ.toInt()} MHz to " +
                    "${SynthConstants.MAX_FREQUENCY_MHZ.toInt()} MHz",
            )
        }
    }
}
