package com.netscope.utils

import android.content.Context
import android.net.wifi.WifiManager
import com.netscope.model.DeviceInfo

object NetworkScanner {

    fun getWifiDetails(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        return "Connected WiFi: ${info.ssid}\nIP: ${intToIP(info.ipAddress)}"
    }

    fun scanNetwork(context: Context): List<DeviceInfo> {
        // Simulated scan results (for demonstration)
        return listOf(
            DeviceInfo("192.168.1.2", "00:1A:79:AB:23:45", "Samsung Electronics"),
            DeviceInfo("192.168.1.3", "00:1C:BF:CD:67:89", "Apple Inc."),
            DeviceInfo("192.168.1.4", "00:0F:E2:DE:98:12", "Intel Corp.")
        )
    }

    private fun intToIP(i: Int): String {
        return ((i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF))
    }
}
