package com.devcore.ticketflow

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.devcore.ticketflow.ui.navigation.AppNavigation
import com.devcore.ticketflow.ui.theme.TicketFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
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
}