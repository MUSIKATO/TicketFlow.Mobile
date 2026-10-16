package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

// ponytail: Mis Tickets — lista estática con filtros mock sin lógica de negocio.
class UserTicketsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_tickets)

        LayoutInsets.withBottomNav(
            root = findViewById(R.id.userTicketsCoordinator),
            scroll = findViewById(R.id.userTicketsScroll),
            fab = findViewById(R.id.fabNewTicket),
            bottomNav = findViewById(R.id.bottomNav)
        )

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_tickets
        setupUserBottomNav(bottomNav, this, this::class.java)

        findViewById<View>(R.id.fabNewTicket).setOnClickListener { openCreate() }
        findViewById<View>(R.id.cardTicket1).setOnClickListener { openDetail() }
        findViewById<View>(R.id.cardTicket2).setOnClickListener { openDetail() }
        findViewById<View>(R.id.cardTicket3).setOnClickListener { openDetail() }
    }

    private fun openCreate() {
        startActivity(Intent(this, UserCreateTicketActivity::class.java))
    }

    private fun openDetail() {
        startActivity(Intent(this, UserTicketDetailActivity::class.java))
    }
}