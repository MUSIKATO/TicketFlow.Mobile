package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserDashboardActivity : AppCompatActivity() {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

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

        findViewById<RecyclerView>(R.id.recyclerRecentTickets).layoutManager =
            LinearLayoutManager(this)

        cargarMetricas()
        cargarTicketsRecientes()
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

    private fun cargarTicketsRecientes() {
        lifecycleScope.launch {
            val userId = SupabaseClient.perfil?.id
            if (userId == null) {
                Log.e("TicketFlowError", "Perfil nulo, no se pudieron cargar los tickets recientes")
                return@launch
            }

            try {
                val tickets = withContext(Dispatchers.IO) {
                    json.decodeFromString<List<Ticket>>(
                        SupabaseClient.client.postgrest.from("tickets").select {
                            filter { eq("profile", userId) }
                            order("created_at", Order.DESCENDING)
                            limit(3)
                        }.data
                    )
                }
                findViewById<RecyclerView>(R.id.recyclerRecentTickets).adapter =
                    RecentTicketsAdapter(tickets) { openDetail(it) }
            } catch (e: Exception) {
                Log.e("TicketFlowError", "Fallo al cargar tickets recientes", e)
                Toast.makeText(this@UserDashboardActivity, R.string.error_cargar_metricas, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openCreateTicket() {
        startActivity(Intent(this, UserCreateTicketActivity::class.java))
    }

    private fun openTickets() {
        startActivity(Intent(this, UserTicketsActivity::class.java))
        finish()
    }

    private fun openDetail(ticket: Ticket) {
        startActivity(
            Intent(this, UserTicketDetailActivity::class.java)
                .putExtra(UserTicketDetailActivity.EXTRA_TICKET_CODE, "#${ticket.id}")
        )
    }
}

@Serializable
data class Ticket(
    val id: Long = 0,
    val descripcion: String = "",
    val prioridad: String = "",
    val estado: String = ""
)

// ponytail: badges asignados a colores ya existentes en colors.xml; prioridad/estado desconocidos van en gris neutro.
private class RecentTicketsAdapter(
    private val tickets: List<Ticket>,
    private val onClick: (Ticket) -> Unit
) : RecyclerView.Adapter<RecentTicketsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_recent_ticket, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(tickets[position])

    override fun getItemCount() = tickets.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtId = itemView.findViewById<TextView>(R.id.txtTicketId)
        private val txtPriority = itemView.findViewById<TextView>(R.id.txtBadgePriority)
        private val txtStatus = itemView.findViewById<TextView>(R.id.txtBadgeStatus)
        private val txtDescription = itemView.findViewById<TextView>(R.id.txtTicketDescription)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onClick(tickets[pos])
            }
        }

        fun bind(ticket: Ticket) {
            val ctx = itemView.context
            txtId.text = "#${ticket.id}"
            aplicarBadge(txtPriority, prioridadBadge(ctx, ticket.prioridad))
            aplicarBadge(txtStatus, estadoBadge(ctx, ticket.estado))
            txtDescription.text = ticket.descripcion
        }
    }
}