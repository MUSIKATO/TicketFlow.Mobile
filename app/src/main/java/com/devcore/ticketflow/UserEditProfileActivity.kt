package com.devcore.ticketflow

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

// ponytail: Editar información — actualiza nombre/correo en la tabla 'profiles' y refleja el cambio en memoria.
class UserEditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit_profile)

        LayoutInsets.withPinnedButton(
            root = findViewById(R.id.userEditCoordinator),
            scroll = findViewById(R.id.userEditScroll),
            pinned = findViewById(R.id.btnGuardar)
        )

        SupabaseClient.perfil?.let {
            findViewById<TextInputEditText>(R.id.inputNombre).setText(it.nombre)
            findViewById<TextInputEditText>(R.id.inputCorreo).setText(it.correo)
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnGuardar).setOnClickListener { guardar() }
    }

    private fun guardar() {
        val nombre = findViewById<TextInputEditText>(R.id.inputNombre).text?.toString()?.trim().orEmpty()
        val correo = findViewById<TextInputEditText>(R.id.inputCorreo).text?.toString()?.trim().orEmpty()
        val usuarioId = SupabaseClient.perfil?.id

        if (usuarioId == null) {
            Toast.makeText(this, R.string.error_crear_ticket_sesion, Toast.LENGTH_SHORT).show()
            return
        }
        if (nombre.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, R.string.error_campos_requeridos, Toast.LENGTH_SHORT).show()
            return
        }

        val btnGuardar = findViewById<View>(R.id.btnGuardar)
        val progress = findViewById<View>(R.id.progressGuardar)
        btnGuardar.isEnabled = false
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val payload = buildJsonObject {
                    put("nombre", JsonPrimitive(nombre))
                    put("correo", JsonPrimitive(correo))
                }
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("profiles")
                        .update(payload) { filter { eq("id", usuarioId) } }
                }
                SupabaseClient.perfil = SupabaseClient.perfil?.copy(nombre = nombre, correo = correo)
                Toast.makeText(this@UserEditProfileActivity, R.string.perfil_actualizado, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("TicketFlowError", "Fallo al actualizar el perfil", e)
                Toast.makeText(this@UserEditProfileActivity, R.string.error_actualizar_perfil, Toast.LENGTH_LONG).show()
            } finally {
                btnGuardar.isEnabled = true
                progress.visibility = View.GONE
            }
        }
    }
}