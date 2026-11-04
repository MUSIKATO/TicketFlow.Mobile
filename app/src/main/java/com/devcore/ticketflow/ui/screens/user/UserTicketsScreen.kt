package com.devcore.ticketflow.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devcore.ticketflow.ui.components.TicketFlowErrorBanner
import com.devcore.ticketflow.ui.components.TicketFlowLoading
import com.devcore.ticketflow.ui.theme.CardStroke
import com.devcore.ticketflow.ui.theme.PrimaryNavy
import com.devcore.ticketflow.ui.theme.PriorityCritical
import com.devcore.ticketflow.ui.theme.PriorityCriticalBg
import com.devcore.ticketflow.ui.theme.PriorityHigh
import com.devcore.ticketflow.ui.theme.PriorityHighBg
import com.devcore.ticketflow.ui.theme.PriorityLow
import com.devcore.ticketflow.ui.theme.PriorityLowBg
import com.devcore.ticketflow.ui.theme.PriorityMedium
import com.devcore.ticketflow.ui.theme.PriorityMediumBg
import com.devcore.ticketflow.ui.theme.StatusInfoBlue
import com.devcore.ticketflow.ui.theme.StatusInfoBlueBg
import com.devcore.ticketflow.ui.theme.StatusPending
import com.devcore.ticketflow.ui.theme.StatusPendingBg
import com.devcore.ticketflow.ui.theme.StatusSuccessGreen
import com.devcore.ticketflow.ui.theme.StatusSuccessGreenBg
import com.devcore.ticketflow.ui.theme.TextPrimary
import com.devcore.ticketflow.ui.theme.TextSecondary

@Composable
fun UserTicketsScreen(
    onNavigateToDetail: (ticketId: Long) -> Unit,
    onNavigateToCreateTicket: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserTicketsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = onNavigateToCreateTicket,
                containerColor = PrimaryNavy,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear nuevo ticket"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ====== SECCIÓN 1: CABECERA ======
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mis Tickets",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gestiona tus solicitudes de soporte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = viewModel::refrescar,
                    enabled = !state.isRefreshing
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryNavy
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar datos",
                            tint = PrimaryNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Error banner si existe
            state.errorMessage?.let { error ->
                TicketFlowErrorBanner(message = error)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ====== SECCIÓN 2: FILTROS POR ESTADO ======
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "Todos",
                    selected = state.selectedFilter == null,
                    onClick = { viewModel.onFilterSelected(null) }
                )
                FilterChipItem(
                    label = "Pendientes",
                    selected = state.selectedFilter == "Pendiente",
                    onClick = { viewModel.onFilterSelected("Pendiente") }
                )
                FilterChipItem(
                    label = "En proceso",
                    selected = state.selectedFilter == "En proceso",
                    onClick = { viewModel.onFilterSelected("En proceso") }
                )
                FilterChipItem(
                    label = "Solucionados",
                    selected = state.selectedFilter == "Solucionado",
                    onClick = { viewModel.onFilterSelected("Solucionado") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ====== SECCIÓN 3: LISTADO DE TICKETS ======
            when {
                state.isRefreshing && state.tickets.isEmpty() -> {
                    TicketFlowLoading(message = "Cargando tickets...")
                }
                state.tickets.isEmpty() -> {
                    EmptyTicketsCard()
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.tickets.forEach { ticket ->
                            TicketCard(
                                ticket = ticket,
                                onClick = { onNavigateToDetail(ticket.id) }
                            )
                        }
                    }
                }
            }

            // Espacio final para evitar solapamiento con el FAB flotante
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryNavy,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = CardStroke,
            selectedBorderColor = PrimaryNavy
        )
    )
}

@Composable
private fun TicketCard(
    ticket: TicketItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${ticket.id}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    // Badge de Prioridad
                    val (prioColor, prioBg) = prioridadColor(ticket.prioridad)
                    BadgeBox(
                        text = ticket.prioridad.ifEmpty { "Normal" },
                        textColor = prioColor,
                        backgroundColor = prioBg
                    )

                    // Badge de Estado
                    val (estColor, estBg) = estadoColor(ticket.estado)
                    BadgeBox(
                        text = ticket.estado.ifEmpty { "Pendiente" },
                        textColor = estColor,
                        backgroundColor = estBg
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver detalle",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ticket.tipo_incidencia.ifEmpty { "Sin tipo de incidencia" },
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ticket.equipo.ifEmpty { "Sin equipo especificado" },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = ticket.descripcion.ifEmpty { "Sin descripción" },
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BadgeBox(
    text: String,
    textColor: Color,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyTicketsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No tienes tickets en esta categoría",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Crea un nuevo ticket para registrar una incidencia.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

private fun prioridadColor(prioridad: String): Pair<Color, Color> = when (prioridad.lowercase()) {
    "baja" -> StatusSuccessGreen to StatusSuccessGreenBg
    "media" -> PriorityMedium to PriorityMediumBg
    "alta" -> PriorityHigh to PriorityHighBg
    "critica", "crítica" -> PriorityCritical to PriorityCriticalBg
    else -> TextSecondary to CardStroke
}

private fun estadoColor(estado: String): Pair<Color, Color> = when (estado.lowercase()) {
    "pendiente" -> StatusPending to StatusPendingBg
    "en proceso" -> StatusInfoBlue to StatusInfoBlueBg
    "solucionado" -> StatusSuccessGreen to StatusSuccessGreenBg
    else -> TextSecondary to CardStroke
}
