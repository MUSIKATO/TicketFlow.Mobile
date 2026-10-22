package com.devcore.ticketflow.ui.screens.user

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devcore.ticketflow.ui.components.TicketCard
import com.devcore.ticketflow.ui.components.TicketFlowErrorBanner
import com.devcore.ticketflow.ui.components.TicketFlowLoading
import com.devcore.ticketflow.ui.components.formatTicketDate
import com.devcore.ticketflow.ui.theme.CardStroke
import com.devcore.ticketflow.ui.theme.PrimaryNavy
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
            FloatingActionButton(
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToDashboard,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Ya estamos en Mis Tickets */ },
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "Mis Tickets") },
                    label = { Text("Mis Tickets") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryNavy,
                        selectedTextColor = PrimaryNavy,
                        indicatorColor = PrimaryNavy.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        // LazyColumn: solo se dibujan las tarjetas visibles en pantalla.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ====== SECCIÓN 1: CABECERA ======
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
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
            }

            // Error banner si existe
            state.errorMessage?.let { error ->
                item(key = "error") {
                    TicketFlowErrorBanner(message = error)
                }
            }

            // ====== SECCIÓN 2: FILTROS POR ESTADO ======
            item(key = "filters") {
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
            }

            // ====== SECCIÓN 3: LISTADO DE TICKETS ======
            when {
                // Primera carga (o cambio de filtro sin datos): mostramos el indicador
                state.isLoading && state.tickets.isEmpty() -> {
                    item(key = "loading") {
                        TicketFlowLoading(message = "Cargando tickets...")
                    }
                }
                state.tickets.isEmpty() -> {
                    item(key = "empty") {
                        EmptyTicketsCard()
                    }
                }
                else -> {
                    items(items = state.tickets, key = { it.id }) { ticket ->
                        TicketCard(
                            id = ticket.id,
                            tipoIncidencia = ticket.tipo_incidencia,
                            equipo = ticket.equipo,
                            descripcion = ticket.descripcion,
                            prioridad = ticket.prioridad,
                            estado = ticket.estado,
                            fecha = formatTicketDate(ticket.created_at),
                            onClick = { onNavigateToDetail(ticket.id) }
                        )
                    }
                }
            }
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