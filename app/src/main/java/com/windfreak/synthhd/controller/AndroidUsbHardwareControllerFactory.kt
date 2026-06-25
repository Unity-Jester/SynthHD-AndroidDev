package com.windfreak.synthhd.controller

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.windfreak.synthhd.domain.SynthDeviceState

class AndroidUsbHardwareControllerFactory(context: Context) : HardwareControllerFactory {
    private val appContext = context.applicationContext
    private val usbManager = appContext.getSystemService(Context.USB_SERVICE) as UsbManager

    override fun scan(): List<HardwareDevice> =
        availableDrivers().map { driver ->
            val device = driver.device
            HardwareDevice(
                label = device.displayLabel,
                vendorId = device.vendorId,
                productId = device.productId,
            )
        }

    override fun connectFirst(initialState: SynthDeviceState): HardwareConnectResult {
        val driver = availableDrivers().firstOrNull()
            ?: return HardwareConnectResult.Failed("No USB serial devices found.")
        val device = driver.device

        if (!usbManager.hasPermission(device)) {
            usbManager.requestPermission(device, permissionIntent())
            return HardwareConnectResult.PermissionRequested(device.displayLabel)
        }

        val connection = usbManager.openDevice(device)
            ?: return HardwareConnectResult.Failed("Could not open ${device.displayLabel}.")
        val port = driver.ports.firstOrNull()
            ?: return HardwareConnectResult.Failed("${device.displayLabel} does not expose a serial port.")

        return try {
            port.open(connection)
            port.setParameters(
                115_200,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE,
            )
            val controller = WindfreakSynthHdController(
                transport = UsbSerialWindfreakTransport(port),
                initialState = initialState,
            )
            runCatching { controller.refreshStatus() }
            HardwareConnectResult.Connected(controller)
        } catch (error: Exception) {
            runCatching { port.close() }
            HardwareConnectResult.Failed("USB connect failed: ${error.message ?: error.javaClass.simpleName}")
        }
    }

    private fun availableDrivers(): List<UsbSerialDriver> =
        UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

    private fun permissionIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            0,
            Intent(ACTION_USB_PERMISSION).setPackage(appContext.packageName),
            PendingIntent.FLAG_IMMUTABLE,
        )

    private val UsbDevice.displayLabel: String
        get() = productName ?: deviceName ?: "USB device ${vendorId.toString(16)}:${productId.toString(16)}"

    companion object {
        private const val ACTION_USB_PERMISSION = "com.windfreak.synthhd.USB_PERMISSION"
    }
}
