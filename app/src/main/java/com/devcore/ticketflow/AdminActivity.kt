package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val root = findViewById<View>(R.id.adminCoordinator)
        val scroll = findViewById<View>(R.id.adminScroll)
        val fab = findViewById<View>(R.id.fabNewTicket)
        val navHeightPx = resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        val fabGapPx = resources.getDimensionPixelSize(R.dimen.fab_margin)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)

            val scrollLp = scroll.layoutParams as ViewGroup.MarginLayoutParams
            scrollLp.bottomMargin = navHeightPx + systemBars.bottom
            scroll.layoutParams = scrollLp

            val fabLp = fab.layoutParams as ViewGroup.MarginLayoutParams
            fabLp.bottomMargin = navHeightPx + systemBars.bottom + fabGapPx
            fab.layoutParams = fabLp

            insets
        }

        fab.setOnClickListener {
            Toast.makeText(this, "Crear nuevo ticket - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        // Listener para botón de soporte
        findViewById<View>(R.id.btnSupport).setOnClickListener {
            Toast.makeText(this, "Soporte técnico - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        // Listeners para tarjetas de acciones rápidas
        findViewById<View>(R.id.cardManageTickets).setOnClickListener {
            Toast.makeText(this, "Gestionar tickets - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.cardRegisterEquipment).setOnClickListener {
            Toast.makeText(this, "Registrar equipo - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.cardManageUsers).setOnClickListener {
            Toast.makeText(this, "Gestionar usuarios - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.cardViewReports).setOnClickListener {
            Toast.makeText(this, "Ver reportes - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        // Listener para perfil/cerrar sesión
        findViewById<View>(R.id.imgProfileAvatar).setOnClickListener {
            lifecycleScope.launch {
                SupabaseClient.client.auth.signOut()
                val intent = Intent(this@AdminActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // Listener para bottom navigation
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Ya estamos en home
                    true
                }
                R.id.nav_tickets -> {
                    Toast.makeText(this, "Tickets - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_equipment -> {
                    Toast.makeText(this, "Equipment - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_users -> {
                    Toast.makeText(this, "Users - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}