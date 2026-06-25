package com.windfreak.synthhd.controller

import com.hoho.android.usbserial.driver.UsbSerialPort
import java.nio.charset.StandardCharsets

class UsbSerialWindfreakTransport(
    private val port: UsbSerialPort,
    private val writeTimeoutMs: Int = 1_000,
    private val readTimeoutMs: Int = 1_000,
) : WindfreakSerialTransport {
    override fun writePacket(packet: String) {
        port.write(packet.toByteArray(StandardCharsets.US_ASCII), writeTimeoutMs)
    }

    override fun query(packet: String): String {
        writePacket(packet)
        val response = StringBuilder()
        val buffer = ByteArray(256)
        val deadline = System.currentTimeMillis() + readTimeoutMs
        while (System.currentTimeMillis() < deadline) {
            val count = port.read(buffer, readTimeoutMs)
            if (count <= 0) continue
            val text = String(buffer, 0, count, StandardCharsets.US_ASCII)
            response.append(text)
            if (text.contains('\n')) break
        }
        return response.toString().trim()
    }

    override fun close() {
        port.close()
    }
}
