// app/src/main/java/com/netscope/utils/NetworkScanner.kt
package com.netscope.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.netscope.model.DeviceInfo
import com.netscope.ui.ScanResultsActivity
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

object NetworkScanner {

    private const val TAG = "NetScope"
    private val COMMON_PORTS = listOf(
        // Core Services
        21,    // FTP
        22,    // SSH
        23,    // Telnet
        25,    // SMTP
        53,    // DNS
        80,    // HTTP
        110,   // POP3
        143,   // IMAP
        443,   // HTTPS
        993,   // IMAPS
        995,   // POP3S

        // Web & Admin Panels
        8080,  // HTTP Alternate
        8443,  // HTTPS Alternate
        3000,  // Node.js / React
        5000,  // Flask / Python
        8000,  // Django / HTTP
        8888,  // Jupyter / HTTP

        // Remote Access
        3389,  // RDP (Windows Remote Desktop)
        5900,  // VNC
        5901,  // VNC (alt)
        5800,  // VNC over HTTP

        // Databases
        3306,  // MySQL
        5432,  // PostgreSQL
        27017, // MongoDB
        6379,  // Redis
        9200,  // Elasticsearch

        // IoT / Smart Home
        1883,  // MQTT
        8883,  // MQTT over SSL
        5683,  // CoAP (IoT)

        // Misc
        161,   // SNMP
        502,   // Modbus
        47808  // BACnet (Building Automation)
    )

    /* --------------------------------------------------------------- */
    fun getWifiDetails(context: Context): String {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) return "Wi-Fi is OFF"
        val info = wifiManager.connectionInfo
        val ip = intToIP(info.ipAddress)
        val ssid = info.ssid?.trim('"') ?: "?"
        return "Connected WiFi: $ssid\nIP: $ip"
    }

    /* --------------------------------------------------------------- */
    fun scanNetwork(context: Context, callback: (List<DeviceInfo>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val deviceList = mutableListOf<DeviceInfo>()
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (!wifiManager.isWifiEnabled) {
                withContext(Dispatchers.Main) { callback(emptyList()) }
                return@launch
            }

            val myIp = wifiManager.connectionInfo.ipAddress
            val prefix = intToIP(myIp).substringBeforeLast('.') + '.'
            Log.d(TAG, "Scanning prefix: $prefix")

            // trigger a Wi-Fi scan so ARP gets populated
            try { wifiManager.startScan(); delay(1000) } catch (e: Exception) {}

            var scanned = 0
            val total = 254

            for (i in 1..total) {
                val testIp = "$prefix$i"
                if (testIp == intToIP(myIp)) continue

                if (isReachable(testIp)) {
                    val hostname = getHostname(testIp, wifiManager) ?: "Unknown"
                    val openPorts = scanPorts(testIp)
                    deviceList.add(
                        DeviceInfo(
                            ip = testIp,
                            mac = "MAC Hidden",
                            vendor = hostname,
                            ports = openPorts.joinToString(", ")
                        )
                    )
                    Log.d(TAG, "Found $testIp | Host: $hostname | Ports: ${openPorts.joinToString()}")
                }

                scanned++
                if (scanned % 10 == 0 || scanned == total) {
                    withContext(Dispatchers.Main) {
                        (context as? ScanResultsActivity)?.updateProgress(scanned, total)
                    }
                }
            }

            // ---- gateway (router) ----
            val gatewayIp = intToIP(wifiManager.dhcpInfo.gateway)
            if (gatewayIp != "0.0.0.0" && !deviceList.any { it.ip == gatewayIp }) {
                val hostname = getHostname(gatewayIp, wifiManager) ?: "Router"
                val openPorts = scanPorts(gatewayIp)
                deviceList.add(
                    DeviceInfo(gatewayIp, "MAC Hidden", hostname, openPorts.joinToString(", "))
                )
            }

            withContext(Dispatchers.Main) {
                (context as? ScanResultsActivity)?.hideProgress()
                callback(deviceList.sortedBy { it.ip })
            }
        }
    }

    /* --------------------------------------------------------------- */
    private fun scanPorts(ip: String): List<Int> {
        val open = mutableListOf<Int>()
        for (port in COMMON_PORTS) {
            try {
                Socket().use { s -> s.connect(InetSocketAddress(ip, port), 500) }
                open.add(port)
            } catch (_: Exception) { /* ignore */ }
        }
        return open
    }

    /* --------------------------------------------------------------- */
    private fun isReachable(ip: String): Boolean = try {
        val p = Runtime.getRuntime().exec("ping -c 1 -W 1 $ip")
        p.waitFor() == 0
    } catch (e: Exception) {
        false
    }

    /* --------------------------------------------------------------- */
    private fun getHostname(ip: String, wifiManager: WifiManager): String? {
        // 1. quick reverse-DNS (rarely works on home routers)
        try {
            val name = InetAddress.getByName(ip).hostName
            if (name != ip && !name.endsWith(".in-addr.arpa")) return name
        } catch (_: Exception) {}

        // 2. mDNS – gives real device names (My-Laptop.local, etc.)
        val lock = wifiManager.createMulticastLock("NetScope_mDNS")
        lock.setReferenceCounted(true)
        lock.acquire()

        return try {
            var foundName: String? = null
            val jmdns = JmDNS.create(InetAddress.getByName("0.0.0.0"))

            val syncLock = Object()          // <-- plain lock object
            val listener = object : ServiceListener {
                override fun serviceAdded(event: ServiceEvent?) {}
                override fun serviceRemoved(event: ServiceEvent?) {}
                override fun serviceResolved(event: ServiceEvent) {
                    val info = event.info ?: return
                    if (info.inet4Addresses.any { it.hostAddress == ip }) {
                        foundName = info.name
                        synchronized(syncLock) { syncLock.notifyAll() }
                    }
                }
            }

            jmdns.addServiceListener("_http._tcp.local.", listener)
            jmdns.addServiceListener("_workstation._tcp.local.", listener)

            synchronized(syncLock) {
                try { syncLock.wait(1500) } catch (_: InterruptedException) {}
            }

            jmdns.removeServiceListener("_http._tcp.local.", listener)
            jmdns.removeServiceListener("_workstation._tcp.local.", listener)
            jmdns.close()
            lock.release()

            foundName ?: "Unknown"
        } catch (e: Exception) {
            Log.e(TAG, "mDNS error for $ip", e)
            lock.release()
            "Unknown"
        }
    }

    /* --------------------------------------------------------------- */
    private fun intToIP(i: Int): String =
        "${i and 0xFF}.${i shr 8 and 0xFF}.${i shr 16 and 0xFF}.${i shr 24 and 0xFF}"
}