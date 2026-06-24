package com.windfreak.synthhd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.windfreak.synthhd.persistence.SynthStateStore
import com.windfreak.synthhd.ui.SynthHdApp
import com.windfreak.synthhd.ui.SynthHdViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SynthStateStore(applicationContext)
        setContent {
            val viewModel = remember { SynthHdViewModel(store) }
            SynthHdApp(viewModel)
        }
    }
}
