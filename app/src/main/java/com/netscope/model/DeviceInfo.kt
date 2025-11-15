// app/src/main/java/com/netscope/model/DeviceInfo.kt
package com.netscope.model

data class DeviceInfo(
    val ip: String,
    val mac: String,
    val vendor: String,
    val ports: String = ""          // <-- NEW
)