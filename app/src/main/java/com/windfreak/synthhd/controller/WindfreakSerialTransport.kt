package com.windfreak.synthhd.controller

interface WindfreakSerialTransport {
    fun writePacket(packet: String)
    fun query(packet: String): String
    fun close()
}
