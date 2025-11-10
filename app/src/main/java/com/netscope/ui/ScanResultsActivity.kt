package com.netscope.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.netscope.R
import com.netscope.adapter.DeviceAdapter
import com.netscope.utils.NetworkScanner
import com.netscope.utils.PDFExporter
import com.google.android.material.button.MaterialButton

class ScanResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_results)

        recyclerView = findViewById(R.id.deviceRecyclerView)
        val exportBtn = findViewById<MaterialButton>(R.id.btnExportPDF)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DeviceAdapter(emptyList())
        recyclerView.adapter = adapter

        exportBtn.setOnClickListener {
            PDFExporter.exportToPDF(this, adapter.deviceList)
        }

        startRealScan()
    }

    private fun startRealScan() {
        Toast.makeText(this, "Scanning network...", Toast.LENGTH_SHORT).show()
        NetworkScanner.scanNetwork(this) { devices ->
            if (devices.isEmpty()) {
                Toast.makeText(this, "No devices found. Try hotspot method.", Toast.LENGTH_LONG).show()
            }
            adapter = DeviceAdapter(devices)
            recyclerView.adapter = adapter
        }
    }
}