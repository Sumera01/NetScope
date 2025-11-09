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

        val wifiInfo = NetworkScanner.getWifiDetails(this)
        wifiInfoText.text = wifiInfo
    }

    private fun checkPermission(): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }
}