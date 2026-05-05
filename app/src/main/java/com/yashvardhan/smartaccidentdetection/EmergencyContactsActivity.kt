package com.yashvardhan.smartaccidentdetection

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EmergencyContactsActivity : AppCompatActivity() {

    private lateinit var etPrimary: EditText
    private lateinit var etPolice: EditText
    private lateinit var etSms: EditText
    private lateinit var listContacts: ListView

    private val contacts = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        etPrimary = findViewById(R.id.etPrimary)
        etPolice = findViewById(R.id.etPolice)
        etSms = findViewById(R.id.etSms)

        listContacts = findViewById(R.id.listContacts)

        val btnAdd = findViewById<Button>(R.id.btnAddContact)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val btnSavePrimary = findViewById<Button>(R.id.btnSavePrimary)
        val btnSavePolice = findViewById<Button>(R.id.btnSavePolice)
        val btnSaveSms = findViewById<Button>(R.id.btnSaveSms)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contacts)
        listContacts.adapter = adapter

        loadSaved()

        // Save buttons
        btnSavePrimary.setOnClickListener {
            saveContacts()
            Toast.makeText(this,"Primary saved",Toast.LENGTH_SHORT).show()
        }

        btnSavePolice.setOnClickListener {
            saveContacts()
            Toast.makeText(this,"Police saved",Toast.LENGTH_SHORT).show()
        }

        btnSaveSms.setOnClickListener {
            saveContacts()
            Toast.makeText(this,"SMS number saved",Toast.LENGTH_SHORT).show()
        }

        // Add contact
        btnAdd.setOnClickListener {
            showAddDialog()
        }

        // click to call
        listContacts.setOnItemClickListener { _, _, position, _ ->
            dialNumber(contacts[position])
        }

        // long press delete
        listContacts.setOnItemLongClickListener { _, _, position, _ ->
            contacts.removeAt(position)
            adapter.notifyDataSetChanged()
            saveContacts()
            true
        }

        btnBack.setOnClickListener {
            saveContacts()
            finish()
        }
    }

    private fun dialNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    private fun showAddDialog() {

        val input = EditText(this)
        input.hint = "Enter phone number"

        AlertDialog.Builder(this)
            .setTitle("Add Emergency Contact")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->

                val number = input.text.toString()

                if (number.isNotEmpty()) {
                    contacts.add(number)
                    adapter.notifyDataSetChanged()
                    saveContacts()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveContacts() {

        val prefs = getSharedPreferences("emergency", MODE_PRIVATE)

        prefs.edit()
            .putString("primary", etPrimary.text.toString())
            .putString("police", etPolice.text.toString())
            .putString("sms", etSms.text.toString())
            .putStringSet("contacts", contacts.toSet())
            .apply()
    }

    private fun loadSaved() {

        val prefs = getSharedPreferences("emergency", MODE_PRIVATE)

        etPrimary.setText(prefs.getString("primary", ""))
        etPolice.setText(prefs.getString("police", "112"))
        etSms.setText(prefs.getString("sms", ""))

        val saved = prefs.getStringSet("contacts", emptySet())

        contacts.clear()
        contacts.addAll(saved!!)

        adapter.notifyDataSetChanged()
    }
}