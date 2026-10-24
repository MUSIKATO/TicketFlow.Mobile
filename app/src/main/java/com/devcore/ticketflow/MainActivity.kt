package com.devcore.ticketflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.devcore.ticketflow.ui.navigation.AppNavigation
import com.devcore.ticketflow.ui.theme.TicketFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            TicketFlowTheme {
                AppNavigation()
            }
        }
    }
}