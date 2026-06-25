package com.windfreak.synthhd.controller

import com.windfreak.synthhd.domain.SynthDeviceState

data class HardwareDevice(
    val label: String,
    val vendorId: Int,
    val productId: Int,
)

sealed interface HardwareConnectResult {
    data class Connected(val controller: WindfreakSynthHdController) : HardwareConnectResult
    data class PermissionRequested(val deviceLabel: String) : HardwareConnectResult
    data class Failed(val message: String) : HardwareConnectResult
}

interface HardwareControllerFactory {
    fun scan(): List<HardwareDevice>
    fun connectFirst(initialState: SynthDeviceState): HardwareConnectResult
}
