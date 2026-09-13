package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        cargarMetricas()
    }

    private fun cargarMetricas() {
        lifecycleScope.launch {
            val userId = SupabaseClient.perfil?.id
            if (userId == null) {
                Log.e("TicketFlowError", "Perfil nulo, no se pudieron cargar las métricas")
                return@launch
            }

            try {
                val (pendientes, enProceso, solucionados) = withContext(Dispatchers.IO) {
                    val inicioMes = LocalDate.now().withDayOfMonth(1)
                        .atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    val inicioSiguienteMes = LocalDate.now().plusMonths(1).withDayOfMonth(1)
                        .atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    Triple(
                        contarEstado(userId, "Pendiente", null, null),
                        contarEstado(userId, "En proceso", null, null),
                        contarEstado(userId, "Solucionado", inicioMes, inicioSiguienteMes)
                    )
                }

                findViewById<TextView>(R.id.txtPendingCount).text = pendientes.toString()
                findViewById<TextView>(R.id.txtInProcessCount).text = enProceso.toString()
                findViewById<TextView>(R.id.txtResolvedCount).text = solucionados.toString()
            } catch (e: Exception) {
                Log.e("TicketFlowError", "Fallo al cargar métricas", e)
                Toast.makeText(this@UserDashboardActivity, R.string.error_cargar_metricas, Toast.LENGTH_LONG).show()
            }
        }
    }

    // ponytail: límite de mes en hora UTC del servidor; si el negocio exige timezone local, pasarlo como offset.
    private suspend fun contarEstado(userId: String, estado: String, desde: String?, hasta: String?): Int {
        val result = SupabaseClient.client.postgrest.from("tickets").select {
            count(Count.EXACT)
            filter {
                eq("profile", userId)
                eq("estado", estado)
                if (desde != null) gte("created_at", desde)
                if (hasta != null) lt("created_at", hasta)
            }
        }
        return result.countOrNull()?.toInt() ?: 0
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