package com.netscope.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netscope.R
import com.netscope.adapter.DeviceAdapter
import com.netscope.utils.NetworkScanner
import com.netscope.utils.PDFExporter
import com.google.android.material.button.MaterialButton

class ScanResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_results)

        val recyclerView = findViewById<RecyclerView>(R.id.deviceRecyclerView)
        val exportBtn = findViewById<MaterialButton>(R.id.btnExportPDF)

        val deviceList = NetworkScanner.scanNetwork(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DeviceAdapter(deviceList)

        exportBtn.setOnClickListener {
            PDFExporter.exportToPDF(this, deviceList)
        }
    }
}