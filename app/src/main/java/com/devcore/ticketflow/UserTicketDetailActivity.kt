package com.devcore.ticketflow

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class UserTicketDetailActivity : AppCompatActivity() {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_ticket_detail)

        LayoutInsets.scrollOnly(findViewById(R.id.userDetailCoordinator))

        val codigo = intent.getStringExtra(EXTRA_TICKET_CODE) ?: getString(R.string.ticket_id_001)
        findViewById<TextView>(R.id.txtTicketCode).text = getString(R.string.title_ticket_detalle, codigo)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        intent.getStringExtra(EXTRA_TICKET_CODE)?.filter(Char::isDigit)?.toLongOrNull()?.let {
            cargarTicket(it)
        }
    }

    private fun cargarTicket(id: Long) {
        val userId = SupabaseClient.perfil?.id
        if (userId == null) {
            Toast.makeText(this, R.string.error_crear_ticket_sesion, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val ticket = withContext(Dispatchers.IO) {
                    json.decodeFromString<List<TicketDetalle>>(
                        SupabaseClient.client.postgrest.from("tickets").select {
                            filter {
                                eq("id", id)
                                eq("profile", userId)
                            }
                            limit(1)
                        }.data
                    ).firstOrNull()
                }
                if (ticket != null) {
                    pintar(ticket)
                } else {
                    Toast.makeText(this@UserTicketDetailActivity, R.string.error_cargar_metricas, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("TicketFlowError", "Fallo al cargar el ticket $id", e)
                Toast.makeText(this@UserTicketDetailActivity, R.string.error_cargar_metricas, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pintar(ticket: TicketDetalle) {
        findViewById<TextView>(R.id.txtIncidenteTitulo).text = ticket.tipo_incidencia
        findViewById<TextView>(R.id.txtSpecEquipo).text = ticket.equipo
        findViewById<TextView>(R.id.txtSpecDescripcion).text = ticket.descripcion
        findViewById<TextView>(R.id.txtFechaReporte).text =
            getString(R.string.ticket_fecha_reporte_fmt, formatoFecha(ticket.created_at))
        aplicarBadge(findViewById(R.id.txtBadgePriority), prioridadBadge(this, ticket.prioridad))
        aplicarBadge(findViewById(R.id.txtBadgeStatus), estadoBadge(this, ticket.estado))
        ticket.evidencia_url?.let { cargarEvidencia(it) }
    }

    // ponytail: URL publica del bucket; HttpURLConnection nativo, sin libreria de imagenes. Probar con permisos de red.
    private fun cargarEvidencia(url: String) {
        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val bytes = (URL(url).openConnection() as HttpURLConnection).inputStream.use { it.readBytes() }
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                    var sampleSize = 1
                    while (opts.outWidth / sampleSize > 1536) sampleSize *= 2 // evita OOM con fotos de alta resolucion
                    opts.inJustDecodeBounds = false
                    opts.inSampleSize = sampleSize
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                }
                findViewById<ImageView>(R.id.imgEvidencia).apply {
                    setImageBitmap(bitmap)
imageTintList = null // el tint azul es solo del placeholder
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(0, 0, 0, 0)
                }
            } catch (e: Exception) {
                Log.e("TicketFlowError", "Fallo al cargar la evidencia", e)
            }
        }
    }

    private fun formatoFecha(iso: String): String = try {
        OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
    } catch (e: Exception) {
        iso.take(10)
    }

    companion object {
        const val EXTRA_TICKET_CODE = "ticket_code"
    }
}

@Serializable
data class TicketDetalle(
    val id: Long = 0,
    val equipo: String = "",
    val tipo_incidencia: String = "",
    val descripcion: String = "",
    val prioridad: String = "",
    val estado: String = "",
    val created_at: String = "",
    val evidencia_url: String? = null
)