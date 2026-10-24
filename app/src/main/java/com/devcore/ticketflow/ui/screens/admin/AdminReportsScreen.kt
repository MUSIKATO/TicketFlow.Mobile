package com.devcore.ticketflow.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devcore.ticketflow.ui.components.TicketFlowErrorBanner
import com.devcore.ticketflow.ui.components.TicketFlowLoading
import com.devcore.ticketflow.ui.theme.CardStroke
import com.devcore.ticketflow.ui.theme.CorporateNavy
import com.devcore.ticketflow.ui.theme.PriorityCritical
import com.devcore.ticketflow.ui.theme.PriorityCriticalBg
import com.devcore.ticketflow.ui.theme.PriorityHigh
import com.devcore.ticketflow.ui.theme.PriorityHighBg
import com.devcore.ticketflow.ui.theme.PriorityLow
import com.devcore.ticketflow.ui.theme.PriorityLowBg
import com.devcore.ticketflow.ui.theme.PriorityMedium
import com.devcore.ticketflow.ui.theme.PriorityMediumBg
import com.devcore.ticketflow.ui.theme.StatusPending
import com.devcore.ticketflow.ui.theme.StatusPendingBg
import com.devcore.ticketflow.ui.theme.StatusSuccessGreen
import com.devcore.ticketflow.ui.theme.StatusSuccessGreenBg
import com.devcore.ticketflow.ui.theme.TextPrimary
import com.devcore.ticketflow.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminReportsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reportes y Estadísticas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::cargarDatos,
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CorporateNavy
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recargar",
                                tint = CorporateNavy
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                TicketFlowLoading(message = "Cargando estadísticas...")
                return@Scaffold
            }

            state.errorMessage?.let { error ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TicketFlowErrorBanner(
                        message = error,
                        onDismiss = viewModel::dismissError
                    )
                }
            }

            // Tarjetas de indicadores principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Tickets",
                    value = "${state.totalTickets}",
                    icon = Icons.Default.ConfirmationNumber,
                    bgColor = CorporateNavy,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Críticos",
                    value = "${state.criticalTickets}",
                    icon = Icons.Default.HourglassEmpty,
                    bgColor = PriorityCritical,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Solucionados",
                    value = "${state.resolvedTickets}",
                    icon = Icons.Default.CheckCircle,
                    bgColor = StatusSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pendientes",
                    value = "${state.pendingTickets}",
                    icon = Icons.Default.HourglassEmpty,
                    bgColor = StatusPending,
                    modifier = Modifier.weight(1f)
                )
            }

            // Distribución por estado
            Text(
                text = "Distribución por Estado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (state.totalTickets > 0) {
                        ProgressItem(
                            label = "Pendientes",
                            count = state.pendingTickets,
                            total = state.totalTickets,
                            color = StatusPending,
                            bgColor = StatusPendingBg
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressItem(
                            label = "En Proceso",
                            count = state.inProcessTickets,
                            total = state.totalTickets,
                            color = com.devcore.ticketflow.ui.theme.StatusInfoBlue,
                            bgColor = com.devcore.ticketflow.ui.theme.StatusInfoBlueBg
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressItem(
                            label = "Solucionados",
                            count = state.resolvedTickets,
                            total = state.totalTickets,
                            color = StatusSuccessGreen,
                            bgColor = StatusSuccessGreenBg
                        )
                    } else {
                        Text(
                            text = "No hay datos disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Distribución por prioridad
            Text(
                text = "Distribución por Prioridad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (state.totalTickets > 0) {
                        ProgressItem(
                            label = "Baja",
                            count = state.lowPriorityTickets,
                            total = state.totalTickets,
                            color = PriorityLow,
                            bgColor = PriorityLowBg
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressItem(
                            label = "Media",
                            count = state.mediumPriorityTickets,
                            total = state.totalTickets,
                            color = PriorityMedium,
                            bgColor = PriorityMediumBg
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressItem(
                            label = "Alta",
                            count = state.highPriorityTickets,
                            total = state.totalTickets,
                            color = PriorityHigh,
                            bgColor = PriorityHighBg
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressItem(
                            label = "Crítica",
                            count = state.criticalTickets,
                            total = state.totalTickets,
                            color = PriorityCritical,
                            bgColor = PriorityCriticalBg
                        )
                    } else {
                        Text(
                            text = "No hay datos disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Equipo con más incidencias
            Text(
                text = "Equipo con Más Incidencias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = CorporateNavy,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = state.topEquipment ?: "Sin datos",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${state.topEquipmentCount} incidencias",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ProgressItem(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    bgColor: Color
) {
    val percentage = if (total > 0) count.toFloat() / total.toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "$count (${
                    (percentage * 100).toInt()
                }%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(bgColor, RoundedCornerShape(4.dp))
        ) {
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = Color.Transparent
            )
        }
    }
}
