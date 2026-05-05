package com.yashvardhan.smartaccidentdetection

data class Incident(
    val accidentId: String? = null,
    val userName: String? = null,
    val phone: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val lat: Double? = null,
    val lng: Double? = null,
    val status: String = "ACTIVE",
    val source: String = "ANDROID_APP"
)

