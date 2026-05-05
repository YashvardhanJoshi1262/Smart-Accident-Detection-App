package com.yashvardhan.smartaccidentdetection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.widget.ImageView
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager

class AlarmActivity : AppCompatActivity() {

    private lateinit var tvCountdown: TextView
    private lateinit var btnCancelAlert: Button

    private var countDownTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private val countdownMillis: Long = 30_000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        val imgAlert = findViewById<ImageView>(R.id.imgAlert)

        imgAlert.animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                imgAlert.animate().alpha(1f).duration = 500
            }
            .start()

        tvCountdown = findViewById(R.id.tvCountdown)
        btnCancelAlert = findViewById(R.id.btnCancelAlert)

        startAlarmSound()
        startCountdown()

        btnCancelAlert.setOnClickListener {
            cancelAlert()
        }
    }

    private fun startAlarmSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
        startVibration()
    }

    private fun stopAlarmSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopVibration()
    }

    private fun startCountdown() {

        tvCountdown.text = "30"

        countDownTimer = object : CountDownTimer(countdownMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                tvCountdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {

                tvCountdown.text = "0"
                stopAlarmSound()

                // accident confirmed
                onAccidentConfirmed()
            }
        }.start()
    }

    private fun cancelAlert() {
        countDownTimer?.cancel()
        stopAlarmSound()
        Toast.makeText(this, "Alert cancelled", Toast.LENGTH_SHORT).show()
        finish()
    }

    // ===============================
    // ACCIDENT CONFIRMED
    // ===============================

    private fun onAccidentConfirmed() {

        getCurrentLocation { lat, lng ->

            // send firebase
            pushIncidentToFirebase(lat, lng)

            // send sms
            if (lat != null && lng != null) {
                sendEmergencySms(lat, lng)
            }

            Toast.makeText(
                this,
                "Accident reported",
                Toast.LENGTH_LONG
            ).show()

            // return to home
            finish()
        }
    }

    // ===============================
    // FIREBASE
    // ===============================

    private fun pushIncidentToFirebase(lat: Double?, lng: Double?) {

        val url =
            "https://realtime-accident-updates-default-rtdb.firebaseio.com/incidents.json"

        val client = OkHttpClient()
        val timestamp = System.currentTimeMillis()

        val json = """
        {
          "userName": "Demo User",
          "phone": "9999999999",
          "timestamp": $timestamp,
          "lat": ${lat ?: "null"},
          "lng": ${lng ?: "null"},
          "status": "ACTIVE",
          "source": "ANDROID_APP"
        }
        """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.close()
            }
        })
    }

    // ===============================
    // SMS
    // ===============================

    private fun sendEmergencySms(lat: Double, lng: Double) {

        val prefs = getSharedPreferences("emergency", MODE_PRIVATE)
        val smsNumber = prefs.getString("sms", "") ?: ""

        if (smsNumber.isEmpty()) {
            Toast.makeText(this,"SMS number not set",Toast.LENGTH_LONG).show()
            return
        }

        val message =
            "🚨 ACCIDENT DETECTED\n\n" +
                    "Location:\n" +
                    "https://maps.google.com/?q=$lat,$lng"

        try {

            val smsManager = SmsManager.getDefault()

            smsManager.sendTextMessage(
                smsNumber,
                null,
                message,
                null,
                null
            )

            Toast.makeText(this,"SMS sent",Toast.LENGTH_LONG).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "SMS failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ===============================
    // LOCATION
    // ===============================

    private fun getCurrentLocation(onResult: (Double?, Double?) -> Unit) {

        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val hasPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onResult(null, null)
            return
        }

        val listener = object : android.location.LocationListener {

            override fun onLocationChanged(location: Location) {

                val lat = location.latitude
                val lng = location.longitude

                locationManager.removeUpdates(this)

                onResult(lat, lng)
            }
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                listener
            )
        } catch (e: Exception) {
            onResult(null, null)
        }
    }

    private fun startVibration() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibrator = vibratorManager.defaultVibrator
        } else {
            vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        vibrator?.let {
            val pattern = longArrayOf(0, 500, 300, 500)
            val effect = VibrationEffect.createWaveform(pattern, 0)
            it.vibrate(effect)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    override fun onDestroy() {
        super.onDestroy()

        countDownTimer?.cancel()
        stopAlarmSound()

        AccidentMonitoringService.alarmRunning = false
    }
}