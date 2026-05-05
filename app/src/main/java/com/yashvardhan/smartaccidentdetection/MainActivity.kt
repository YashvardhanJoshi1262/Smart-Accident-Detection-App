package com.yashvardhan.smartaccidentdetection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnStartMonitoring: Button
    private lateinit var btnStopMonitoring: Button
    private lateinit var btnContacts: Button
    private lateinit var btnSettings: Button

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSmsPermission()

        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnStartMonitoring = findViewById(R.id.btnStartMonitoring)
        btnStopMonitoring = findViewById(R.id.btnStopMonitoring)
        btnContacts = findViewById(R.id.btnContacts)
        btnSettings = findViewById(R.id.btnSettings)

        ensureLocationPermission()

        btnStartMonitoring.setOnClickListener {
            startMonitoring()
        }

        btnStopMonitoring.setOnClickListener {
            stopMonitoring()
        }

        btnContacts.setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun startMonitoring() {

        val intent = Intent(this, AccidentMonitoringService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        tvStatus.text = "Monitoring: ON"

        Toast.makeText(this, "Accident detection started", Toast.LENGTH_SHORT).show()
    }

    private fun stopMonitoring() {

        val intent = Intent(this, AccidentMonitoringService::class.java)

        stopService(intent)

        tvStatus.text = "Monitoring: OFF"

        Toast.makeText(this, "Accident detection stopped", Toast.LENGTH_SHORT).show()
    }


    private fun requestSmsPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                200
            )
        }
    }

    private fun ensureLocationPermission() {

        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }
}