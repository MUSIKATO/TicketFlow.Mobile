package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

// ponytail: Dashboard de Usuario — UI estática (saludo, métricas mock, tickets recientes)
// Los insets se aplican para que el header quede debajo de la barra de estado (notch/cámara).
class UserDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        LayoutInsets.withBottomNav(
            root = findViewById(R.id.userDashboardCoordinator),
            scroll = findViewById(R.id.userDashboardScroll),
            fab = findViewById(R.id.fabNewTicket),
            bottomNav = findViewById(R.id.bottomNav)
        )

        val greeting = findViewById<TextView>(R.id.txtGreeting)
        val userName = SupabaseClient.perfil?.nombre ?: "Usuario"
        greeting.text = getString(R.string.dashboard_greeting, userName)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home
        setupUserBottomNav(bottomNav, this, this::class.java)

        findViewById<View>(R.id.cardCreateTicket).setOnClickListener { openCreateTicket() }
        findViewById<View>(R.id.fabNewTicket).setOnClickListener { openCreateTicket() }
        findViewById<View>(R.id.txtViewAll).setOnClickListener { openTickets() }
        findViewById<View>(R.id.cardTicket1).setOnClickListener { openDetail() }
        findViewById<View>(R.id.cardTicket2).setOnClickListener { openDetail() }
        findViewById<View>(R.id.cardTicket3).setOnClickListener { openDetail() }
    }

    private fun openCreateTicket() {
        startActivity(Intent(this, UserCreateTicketActivity::class.java))
    }

    private fun openTickets() {
        startActivity(Intent(this, UserTicketsActivity::class.java))
        finish()
    }

    private fun openDetail() {
        startActivity(Intent(this, UserTicketDetailActivity::class.java))
    }
}