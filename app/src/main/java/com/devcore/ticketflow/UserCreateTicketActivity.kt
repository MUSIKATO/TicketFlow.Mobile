package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

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

        val dropdownEquipo = findViewById<MaterialAutoCompleteTextView>(R.id.dropdownEquipo)
        val equipos = resources.getStringArray(R.array.opciones_equipos)
        dropdownEquipo.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, equipos))
        dropdownEquipo.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            findViewById<View>(R.id.layoutEquipoOtro).visibility =
                if (selected == getString(R.string.opcion_equipo_otro)) View.VISIBLE else View.GONE
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSendTicket).setOnClickListener { crearTicket() }
    }

    private fun crearTicket() {
        val userId = SupabaseClient.perfil?.id
        if (userId == null) {
            Toast.makeText(this, R.string.error_crear_ticket_sesion, Toast.LENGTH_SHORT).show()
            return
        }

        val layoutEquipoOtro = findViewById<View>(R.id.layoutEquipoOtro)
        val dropdownEquipo = findViewById<MaterialAutoCompleteTextView>(R.id.dropdownEquipo)
        val equipo = if (layoutEquipoOtro.visibility == View.VISIBLE) {
            findViewById<TextInputEditText>(R.id.inputEquipoOtro).text?.toString()?.trim().orEmpty()
        } else {
            dropdownEquipo.text?.toString()?.trim().orEmpty()
        }
        val tipoIncidencia = findViewById<TextInputEditText>(R.id.inputTipoIncidencia).text?.toString()?.trim().orEmpty()
        val descripcion = findViewById<TextInputEditText>(R.id.inputDescripcion).text?.toString()?.trim().orEmpty()
        val prioridad = prioridadSeleccionada()

        if (equipo.isEmpty() || equipo == getString(R.string.hint_equipo) ||
            tipoIncidencia.isEmpty() || descripcion.isEmpty() || prioridad == null
        ) {
            Toast.makeText(this, R.string.error_campos_requeridos, Toast.LENGTH_SHORT).show()
            return
        }

        val payload = buildJsonObject {
            put("equipo", JsonPrimitive(equipo))
            put("tipo_incidencia", JsonPrimitive(tipoIncidencia))
            put("descripcion", JsonPrimitive(descripcion))
            put("prioridad", JsonPrimitive(prioridad))
            put("profile", JsonPrimitive(userId))
        }
        Log.d("TicketFlowInsert", "profile(id) enviado al insert: $userId")
        Log.d("TicketFlowInsert", "auth.uid() (currentUser): ${SupabaseClient.client.auth.currentUserOrNull()?.id}")
        Log.d("TicketFlowInsert", "payload: $payload")

        val btnSend = findViewById<View>(R.id.btnSendTicket)
        btnSend.isEnabled = false
        lifecycleScope.launch {
            try {
                // select() dentro de insert: pide que la BD devuelva la fila insertada (Prefer: return=representation)
                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("tickets")
                        .insert(JsonArray(listOf(payload))) { select() }
                }
                val nuevoId = Json.parseToJsonElement(result.data).jsonArray
                    .firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.longOrNull
                startActivity(
                    Intent(this@UserCreateTicketActivity, UserTicketDetailActivity::class.java)
                        .putExtra(UserTicketDetailActivity.EXTRA_TICKET_CODE, nuevoId?.let { "#$it" } ?: "#nuevo")
                )
                finish()
            } catch (e: Exception) {
                Log.e("TicketFlowError", "Fallo al crear el ticket", e)
                Toast.makeText(this@UserCreateTicketActivity, R.string.error_crear_ticket, Toast.LENGTH_LONG).show()
            } finally {
                btnSend.isEnabled = true
            }
        }
    }

    // ponytail: "Crítica" del chip lleva tilde; el enum de la BD es 'Critica', así que se mapea manualmente.
    private fun prioridadSeleccionada(): String? = when {
        findViewById<Chip>(R.id.chipPrioridadBaja).isChecked -> "Baja"
        findViewById<Chip>(R.id.chipPrioridadMedia).isChecked -> "Media"
        findViewById<Chip>(R.id.chipPrioridadAlta).isChecked -> "Alta"
        findViewById<Chip>(R.id.chipPrioridadCritica).isChecked -> "Critica"
        else -> null
    }
}