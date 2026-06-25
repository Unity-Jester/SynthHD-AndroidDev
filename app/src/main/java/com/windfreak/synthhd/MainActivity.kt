package com.windfreak.synthhd

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.windfreak.synthhd.controller.AndroidUsbHardwareControllerFactory
import com.windfreak.synthhd.persistence.SynthStateStore
import com.windfreak.synthhd.ui.SynthHdApp
import com.windfreak.synthhd.ui.SynthHdViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SynthStateStore(applicationContext)
        val hardwareFactory = AndroidUsbHardwareControllerFactory(applicationContext)
        setContent {
            val viewModel = remember { SynthHdViewModel(store, hardwareFactory = hardwareFactory) }
            SynthHdApp(viewModel)
        }
        window.decorView.post { hideAndroidStatusBar() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideAndroidStatusBar()
        }
    }

    private fun hideAndroidStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}
