package com.devcore.ticketflow

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView

// ponytail: Crear Ticket — formulario mock; el envío se conectará a Supabase luego.
class UserCreateTicketActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_create_ticket)

        LayoutInsets.withPinnedButton(
            root = findViewById(R.id.userCreateCoordinator),
            scroll = findViewById(R.id.userCreateScroll),
            pinned = findViewById(R.id.btnSendTicket)
        )

        val equipos = resources.getStringArray(R.array.opciones_equipos)
        findViewById<MaterialAutoCompleteTextView>(R.id.dropdownEquipo).setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, equipos)
        )

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSendTicket).setOnClickListener {
            Toast.makeText(this, getString(R.string.fake_submit_ticket), Toast.LENGTH_SHORT).show()
        }
    }
}