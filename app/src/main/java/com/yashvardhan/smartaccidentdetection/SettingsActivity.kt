package com.yashvardhan.smartaccidentdetection

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backBtn = findViewById<Button>(R.id.btnBackSettings)

        backBtn.setOnClickListener {
            finish()
        }
    }
}