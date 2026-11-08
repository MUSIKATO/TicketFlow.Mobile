package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.devcore.ticketflow.ui.navigation.AppNavigation
import com.devcore.ticketflow.ui.theme.TicketFlowTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {

    private var lastHandledIntentData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()

            // Procesar deep link si se abre la app desde el callback de Google
            intent?.let { handleAuthIntent(it, fromOnCreate = true) }

            setContent {
                TicketFlowTheme {
                    AppNavigation()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al inicializar la aplicación", e)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent, fromOnCreate = false)
    }

    private fun handleAuthIntent(intent: Intent, fromOnCreate: Boolean) {
        val uri = intent.data
        Log.d("TicketFlowAuth", "MainActivity.handleAuthIntent (fromOnCreate=) -> URI: ${uri?.scheme}://")
        if (uri == null) return

        if (uri.scheme == "ticketflow" && uri.host == "auth") {
            val uriString = uri.toString()
            if (uriString == lastHandledIntentData) {
                Log.d("TicketFlowAuth", "Deep link ya procesado previamente, omitiendo duplicado.")
                return
            }
            lastHandledIntentData = uriString
            Log.d("TicketFlowAuth", "Procesando deep link OAuth con Supabase SDK...")
            try {
                SupabaseClient.client.handleDeeplinks(
                    intent = intent,
                    onSessionSuccess = { userSession ->
                        val email = userSession.user?.email ?: "desconocido"
                        val uid = userSession.user?.id?.take(8) ?: "N/A"
                        Log.d("TicketFlowAuth", "Sesión OAuth importada con éxito en MainActivity. Usuario: $email, UID parcial: $uid")
                    },
                    onError = { error ->
                        Log.e("TicketFlowAuth", "Error en handleDeeplinks al procesar callback", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("TicketFlowAuth", "Excepción al invocar handleDeeplinks", e)
            }
        } else {
            Log.d("TicketFlowAuth", "Intent no coincide con scheme=ticketflow y host=auth: $uri")
        }
    }
}