package com.yashvardhan.smartaccidentdetection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AccidentMonitoringService : Service() {

    private lateinit var accidentDetector: AccidentDetector

    companion object {
        var alarmRunning = false
        var lastTriggerTime = 0L
    }

    private val cooldown = 20000L // 20 sec cooldown

    override fun onCreate() {
        super.onCreate()

        accidentDetector = AccidentDetector(this) {

            val now = System.currentTimeMillis()

            // prevent multiple alarms
            if (alarmRunning) return@AccidentDetector

            // cooldown protection
            if (now - lastTriggerTime < cooldown) return@AccidentDetector

            lastTriggerTime = now
            alarmRunning = true

            val intent = Intent(this, AlarmActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        accidentDetector.start()

        startForeground(1, createNotification())
    }

    override fun onDestroy() {
        accidentDetector.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {

        val channelId = "accident_detection"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Accident Detection",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smart Accident Detection")
            .setContentText("Monitoring for accidents in background")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
}