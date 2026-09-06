package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var edtCorreo: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressLogin: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        edtCorreo = findViewById(R.id.edtCorreo)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressLogin = findViewById(R.id.progressLogin)

        comprobarSesion()

        btnLogin.setOnClickListener {
            iniciarSesion()
        }
    }

    private fun iniciarSesion() {

        val correo = edtCorreo.text.toString().trim()
        val contrasena = edtPassword.text.toString()

        if (correo.isEmpty() || contrasena.isEmpty()) {

            Toast.makeText(
                this,
                "Completa todos los campos",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        progressLogin.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        lifecycleScope.launch {

            try {

                SupabaseClient.client.auth.signInWith(Email) {
                    email = correo
                    password = contrasena
                }

                Toast.makeText(
                    this@MainActivity,
                    "Inicio de sesión correcto",
                    Toast.LENGTH_SHORT
                ).show()

                obtenerRolYRedirigir()

            } catch (e: Exception) {

                // ESTA LÍNEA IMPRIMIRÁ EL ERROR REAL EN LOGCAT
                Log.e("TicketFlowError", "Fallo al iniciar sesión", e)

                Toast.makeText(
                    this@MainActivity,
                    "Error: Revisa el Logcat",
                    Toast.LENGTH_LONG
                ).show()

                progressLogin.visibility = View.GONE
                btnLogin.isEnabled = true
            }
        }
    }

    private suspend fun obtenerRolYRedirigir() {

        val usuario =
            SupabaseClient.client.auth.currentUserOrNull()

        if (usuario == null) {

            Toast.makeText(
                this,
                "No se pudo obtener la sesión",
                Toast.LENGTH_SHORT
            ).show()

            progressLogin.visibility = View.GONE
            btnLogin.isEnabled = true

            return
        }

        try {

            val perfil = SupabaseClient.client
                .from("profiles")
                .select {
                    filter {
                        eq("id", usuario.id)
                    }
                }
                .decodeSingle<Profile>()

            when (perfil.rol.lowercase()) {

                "admin" -> {

                    val intent = Intent(
                        this,
                        AdminActivity::class.java
                    )

                    startActivity(intent)
                    finish()
                }

                "usuario" -> {

                    val intent = Intent(
                        this,
                        UserActivity::class.java
                    )

                    startActivity(intent)
                    finish()
                }

                else -> {

                    Toast.makeText(
                        this,
                        "Rol no reconocido",
                        Toast.LENGTH_LONG
                    ).show()

                    SupabaseClient.client.auth.signOut()

                    progressLogin.visibility = View.GONE
                    btnLogin.isEnabled = true
                }
            }

        } catch (e: Exception) {

            // ESTA LÍNEA IMPRIMIRÁ EL ERROR DE PERFIL EN LOGCAT
            Log.e("TicketFlowError", "Fallo al obtener el perfil", e)

            Toast.makeText(
                this,
                "Error de perfil: Revisa el Logcat",
                Toast.LENGTH_LONG
            ).show()

            progressLogin.visibility = View.GONE
            btnLogin.isEnabled = true
        }
    }

    private fun comprobarSesion() {

        lifecycleScope.launch {
            
            // Esperar a que Supabase restaure la sesión de disco
            SupabaseClient.client.auth.awaitInitialization()

            val usuario =
                SupabaseClient.client.auth.currentUserOrNull()

            if (usuario != null) {
                obtenerRolYRedirigir()
            }
        }
    }
}