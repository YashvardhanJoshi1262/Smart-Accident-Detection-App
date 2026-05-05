package com.yashvardhan.smartaccidentdetection

import android.content.Context

object SettingsManager {

    private const val PREF = "accident_settings"

    private const val KEY_VOLUME = "alarm_volume"
    private const val KEY_SENSITIVITY = "detection_sensitivity"
    private const val KEY_LOCATION = "location_enabled"

    fun setVolume(context: Context, value: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_VOLUME, value).apply()
    }

    fun getVolume(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_VOLUME, 2) // default medium
    }

    fun setSensitivity(context: Context, value: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_SENSITIVITY, value).apply()
    }

    fun getSensitivity(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_SENSITIVITY, 2)
    }

    fun setLocationEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_LOCATION, enabled).apply()
    }

    fun isLocationEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_LOCATION, true)
    }
}