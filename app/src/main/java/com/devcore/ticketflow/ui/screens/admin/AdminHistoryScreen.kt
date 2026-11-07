package com.devcore.ticketflow.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devcore.ticketflow.ui.components.TicketCard
import com.devcore.ticketflow.ui.components.TicketFlowErrorBanner
import com.devcore.ticketflow.ui.components.TicketFlowLoading
import com.devcore.ticketflow.ui.components.TicketFlowTextField
import com.devcore.ticketflow.ui.components.formatTicketDate
import com.devcore.ticketflow.ui.theme.PrimaryNavy
import com.devcore.ticketflow.ui.theme.TextPrimary
import com.devcore.ticketflow.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminHistoryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val filteredTickets = remember(state.tickets, state.searchQuery) {
        val query = state.searchQuery.trim()
        if (query.isBlank()) {
            state.tickets
        } else {
            state.tickets.filter { ticket ->
                ticket.id.toString().contains(query, ignoreCase = true) ||
                    ticket.equipo.contains(query, ignoreCase = true) ||
                    ticket.tipoIncidencia.contains(query, ignoreCase = true) ||
                    ticket.descripcion.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de registros",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryNavy
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::refrescar,
                        enabled = !state.isRefreshing && !state.isLoading
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(12.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryNavy
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar historial",
                                tint = PrimaryNavy
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
        ) {
            TicketFlowTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = "Buscar en el historial",
                placeholder = "ID, equipo, tipo o descripción...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            state.errorMessage?.let { message ->
                TicketFlowErrorBanner(
                    message = message,
                    onDismiss = viewModel::dismissError,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            when {
                state.isLoading && state.tickets.isEmpty() -> {
                    TicketFlowLoading(
                        message = "Cargando historial...",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                filteredTickets.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                                Text(
                                    text = if (state.errorMessage != null && state.tickets.isEmpty()) {
                                        "No fue posible confirmar los registros almacenados."
                                    } else if (state.tickets.isEmpty()) {
                                        "Aún no hay tickets registrados."
                                    } else {
                                        "No hay registros que coincidan con la búsqueda."
                                    },
                                    color = TextSecondary
                                )
                                if (state.errorMessage != null && state.tickets.isEmpty()) {
                                    androidx.compose.material3.TextButton(
                                        onClick = viewModel::refrescar
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                                if (state.searchQuery.isNotBlank() && state.tickets.isNotEmpty() && !state.endReached) {
                                    androidx.compose.material3.TextButton(
                                        onClick = viewModel::cargarMas,
                                        enabled = !state.isLoadingMore
                                    ) {
                                        Text("Buscar en más registros")
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = if (state.searchQuery.isBlank()) {
                                    "${filteredTickets.size} registros · del más reciente al más antiguo"
                                } else {
                                    "${filteredTickets.size} coincidencias entre los registros cargados"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        items(filteredTickets, key = { it.id }) { ticket ->
                            TicketCard(
                                id = ticket.id,
                                tipoIncidencia = ticket.tipoIncidencia,
                                equipo = ticket.equipo,
                                descripcion = ticket.descripcion,
                                prioridad = ticket.prioridad,
                                estado = ticket.estado,
                                fecha = formatTicketDate(ticket.createdAt),
                                onClick = { onNavigateToDetail(ticket.id) }
                            )
                        }
                        if (!state.endReached) {
                            item(key = "cargar_mas") {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.isLoadingMore) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(8.dp),
                                            strokeWidth = 2.dp,
                                            color = PrimaryNavy
                                        )
                                    } else {
                                        androidx.compose.material3.TextButton(onClick = viewModel::cargarMas) {
                                            Text("Cargar más registros")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}