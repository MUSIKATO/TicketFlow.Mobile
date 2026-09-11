package com.devcore.ticketflow

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

// ponytail: Detalle Ticket — vista estática del detalle mock de un ticket.
class UserTicketDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_ticket_detail)

        LayoutInsets.scrollOnly(findViewById(R.id.userDetailCoordinator))

        val codigo = intent.getStringExtra(EXTRA_TICKET_CODE) ?: getString(R.string.ticket_id_001)
        findViewById<TextView>(R.id.txtTicketCode).text = getString(R.string.title_ticket_detalle, codigo)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_TICKET_CODE = "ticket_code"
    }
}