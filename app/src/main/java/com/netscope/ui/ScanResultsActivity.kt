package com.netscope.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.netscope.R
import com.netscope.adapter.DeviceAdapter
import com.netscope.utils.NetworkScanner
import com.netscope.utils.PDFExporter

class ScanResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceAdapter
    private lateinit var progressContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_results)

        // UI Elements
        recyclerView = findViewById(R.id.deviceRecyclerView)
        val exportBtn = findViewById<MaterialButton>(R.id.btnExportPDF)
        progressContainer = findViewById(R.id.progressContainer)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DeviceAdapter(emptyList())
        recyclerView.adapter = adapter

        // Export Button
        exportBtn.setOnClickListener {
            PDFExporter.exportToPDF(this, adapter.deviceList)
        }

        // Start Scan
        startRealScan()
    }

    private fun startRealScan() {
        progressContainer.visibility = View.VISIBLE
        progressBar.progress = 0
        progressText.text = "Scanning: 0/254"
        Toast.makeText(this, "Scanning network...", Toast.LENGTH_SHORT).show()

        NetworkScanner.scanNetwork(this) { devices ->
            runOnUiThread {
                hideProgress()
                adapter = DeviceAdapter(devices)
                recyclerView.adapter = adapter
                Toast.makeText(this, "Scan complete: ${devices.size} devices", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Called from NetworkScanner
    fun updateProgress(scanned: Int, total: Int) {
        progressBar.progress = scanned
        progressText.text = "Scanning: $scanned/$total"
    }

    // Called when scan finishes
    fun hideProgress() {
        progressContainer.visibility = View.GONE
    }
}