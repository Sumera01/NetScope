package com.netscope.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.netscope.R
import android.content.Intent
import com.netscope.utils.NetworkScanner
import com.google.android.material.button.MaterialButton



class MainActivity : AppCompatActivity() {

    private lateinit var wifiInfoText: TextView
    private lateinit var scanBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiInfoText = findViewById(R.id.wifiInfo)
        scanBtn = findViewById(R.id.btnScan)

        scanBtn.setOnClickListener {
            if (checkPermission()) {
                startActivity(Intent(this, ScanResultsActivity::class.java))
            } else {
                requestPermission()
            }
        }

        val aboutBtn = findViewById<Button>(R.id.btnAbout)
        aboutBtn.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        val wifiInfo = NetworkScanner.getWifiDetails(this)
        wifiInfoText.text = wifiInfo
    }

    private fun checkPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val nearbyDevices = if (android.os.Build.VERSION.SDK_INT >= 31) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        } else true

        return fineLocation && coarseLocation && nearbyDevices
    }

    private fun requestPermission() {
        val permissions = mutableListOf<String>().apply {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ScanResultsActivity::class.java))
            } else {
                Toast.makeText(this, "Required permissions denied. Scan won't work.", Toast.LENGTH_LONG).show()
            }
        }
    }

}