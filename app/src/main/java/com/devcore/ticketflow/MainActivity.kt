package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.devcore.ticketflow.ui.navigation.AppNavigation
import com.devcore.ticketflow.ui.theme.TicketFlowTheme
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {

    // Registro en memoria del ltimo Intent procesado para evitar reprocesamiento
    private var lastHandledIntentData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()

            // Procesar deep link en arranque en fro solo si no es una recreacin de estado
            if (savedInstanceState == null) {
                intent?.let { handleAuthIntent(it) }
            }

            setContent {
                TicketFlowTheme {
                    AppNavigation()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al inicializar la aplicacin", e)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
    }

    private fun handleAuthIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme == "ticketflow" && uri.host == "auth") {
            val uriString = uri.toString()
            if (uriString == lastHandledIntentData) {
                Log.d("MainActivity", "Deep link ya procesado previamente, omitiendo duplicado.")
                return
            }
            lastHandledIntentData = uriString
            Log.d("MainActivity", "Procesando deep link de autenticacin de forma segura")
            try {
                SupabaseClient.client.handleDeeplinks(
                    intent = intent,
                    onSessionSuccess = { userSession ->
                        Log.d("MainActivity", "Sesin OAuth obtenida exitosamente")
                    },
                    onError = { error ->
                        Log.e("MainActivity", "Error procesando deep link OAuth", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Excepcin al procesar handleDeeplinks", e)
            }
        } else {
            Log.d("MainActivity", "Intent recibido sin deep link de auth relevante")
        }
    }
}