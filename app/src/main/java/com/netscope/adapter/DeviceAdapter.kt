package com.netscope.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netscope.R
import com.netscope.model.DeviceInfo

class DeviceAdapter(val deviceList: List<DeviceInfo>) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceVendor: TextView = view.findViewById(R.id.deviceVendor)
        val deviceIP: TextView = view.findViewById(R.id.deviceIP)
        val deviceMAC: TextView = view.findViewById(R.id.deviceMAC)
        val devicePorts: TextView  = view.findViewById(R.id.devicePorts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.deviceVendor.text = device.vendor
        holder.deviceIP.text = "IP: ${device.ip}"
        holder.deviceMAC.text = "MAC: ${device.mac}"
        holder.devicePorts.text  = if (device.ports.isNotBlank()) "Ports: ${device.ports}"
        else "Ports: none"
    }

    override fun getItemCount(): Int = deviceList.size
}
