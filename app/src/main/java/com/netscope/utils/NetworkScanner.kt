package com.netscope.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import com.netscope.model.DeviceInfo
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress
import kotlinx.coroutines.*

object NetworkScanner {

    fun getWifiDetails(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) return "Wi-Fi is OFF"

        val info = wifiManager.connectionInfo
        val ip = intToIP(info.ipAddress)
        val ssid = info.ssid.trim('"')
        return "Connected WiFi: $ssid\nIP: $ip"
    }

    fun scanNetwork(context: Context, callback: (List<DeviceInfo>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val deviceList = mutableListOf<DeviceInfo>()
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                withContext(Dispatchers.Main) { callback(emptyList()) }
                return@launch
            }

            val ip = wifiManager.connectionInfo.ipAddress
            val prefix = intToIP(ip).substring(0, intToIP(ip).lastIndexOf(".") + 1)

            // Try to read ARP table first (fastest)
            deviceList.addAll(readArpTable())

            // If ARP is empty (common on Android 12+), do active ping scan
            if (deviceList.size < 2) {
                deviceList.clear()
                for (i in 1..254) {
                    val testIp = "$prefix$i"
                    if (testIp == intToIP(ip)) continue // skip own device

                    try {
                        val address = InetAddress.getByName(testIp)
                        if (address.reachability(200)) { // 200ms timeout
                            val mac = getMacFromArp(testIp) ?: "Unknown"
                            val vendor = getVendor(mac) ?: "Unknown Vendor"
                            deviceList.add(DeviceInfo(testIp, mac, vendor))
                        }
                    } catch (_: Exception) {}
                }
            }

            // Always include gateway (router)
            val gateway = wifiManager.dhcpInfo.gateway
            if (gateway != 0) {
                val gatewayIp = intToIP(gateway)
                if (!deviceList.any { it.ip == gatewayIp }) {
                    val mac = getMacFromArp(gatewayIp) ?: "Router"
                    deviceList.add(DeviceInfo(gatewayIp, mac, "Router"))
                }
            }

            withContext(Dispatchers.Main) {
                callback(deviceList.sortedBy { it.ip })
            }
        }
    }

    private fun readArpTable(): List<DeviceInfo> {
        val list = mutableListOf<DeviceInfo>()
        try {
            BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(Regex("\\s+"))
                    if (parts.size >= 4) {
                        val ip = parts[0]
                        val mac = parts[3]
                        if (mac.matches(Regex("..:..:..:..:..:.."))) {
                            val vendor = getVendor(mac) ?: "Unknown"
                            list.add(DeviceInfo(ip, mac, vendor))
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return list
    }

    private fun getMacFromArp(ip: String): String? {
        try {
            BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.contains(ip)) {
                        val parts = line!!.split(Regex("\\s+"))
                        if (parts.size >= 4) return parts[3].takeIf { it.matches(Regex("..:..:..:..:..:..")) }
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun getVendor(mac: String): String? {
        val prefix = mac.substring(0, 8).uppercase()
        return when (prefix) {
            "00:1A:79" -> "Samsung Electronics"
            "00:1C:BF" -> "Apple Inc."
            "00:0F:E2" -> "Intel Corp."
            "B8:27:EB" -> "Raspberry Pi"
            "DC:A6:32" -> "Raspberry Pi"
            "E4:5F:01" -> "Raspberry Pi"
            "00:50:56" -> "VMware"
            "08:00:27" -> "VirtualBox"
            else -> null
        }
    }

    private fun intToIP(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)
    }

    private fun InetAddress.reachability(timeout: Int): Boolean {
        return try {
            this.isReachable(timeout)
        } catch (e: Exception) {
            false
        }
    }
}