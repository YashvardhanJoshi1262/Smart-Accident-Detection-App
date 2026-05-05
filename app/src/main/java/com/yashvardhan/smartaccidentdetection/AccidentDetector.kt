package com.yashvardhan.smartaccidentdetection

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class AccidentDetector(
    context: Context,
    private val onAccidentDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastTriggerTime = 0L

    // VERY LOW threshold for demo
    private val shakeThreshold = 5f

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        event ?: return

        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTriggerTime < 3000) {
            return
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(x * x + y * y + z * z)

        val acceleration = magnitude - SensorManager.GRAVITY_EARTH

        if (acceleration > shakeThreshold) {

            lastTriggerTime = currentTime

            onAccidentDetected.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}