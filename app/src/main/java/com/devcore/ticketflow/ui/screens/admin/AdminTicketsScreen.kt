package com.devcore.ticketflow.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTicketsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (ticketId: Long) -> Unit,
    onNavigateToCreate: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AdminTicketsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var ticketParaCambiarEstado by remember { mutableStateOf<AdminTicketItem?>(null) }
    var ticketParaCambiarPrioridad by remember { mutableStateOf<AdminTicketItem?>(null) }
    var ticketParaEliminar by remember { mutableStateOf<AdminTicketItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestión de Tickets",
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
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryNavy
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recargar lista",
                                tint = PrimaryNavy
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = PrimaryNavy
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear ticket",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filtros de Estado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusFilters = listOf(
                    "Todos los estados" to null,
                    "Pendiente" to "Pendiente",
                    "En proceso" to "En proceso",
                    "Solucionado" to "Solucionado"
                )

                statusFilters.forEach { (label, value) ->
                    val isSelected = state.selectedStatusFilter == value
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onStatusFilterSelected(value) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryNavy,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PrimaryNavy else CardStroke
                        )
                    )
                }
            }

            // Filtros de Prioridad
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val priorityFilters = listOf(
                    "Todas las prioridades" to null,
                    "Baja" to "Baja",
                    "Media" to "Media",
                    "Alta" to "Alta",
                    "Crítica" to "Critica"
                )

                priorityFilters.forEach { (label, value) ->
                    val isSelected = state.selectedPriorityFilter == value
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onPriorityFilterSelected(value) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryNavy.copy(alpha = 0.85f),
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PrimaryNavy else CardStroke
                        )
                    )
                }
            }

            val hayFiltrosActivos = state.selectedStatusFilter != null || state.selectedPriorityFilter != null
            if (hayFiltrosActivos) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = viewModel::restablecerFiltros) {
                        Text("Restablecer filtros", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Banner de Mensaje de Acción Exitosa
            state.actionMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(StatusSuccessGreenBg, RoundedCornerShape(8.dp))
                        .border(1.dp, StatusSuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusSuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Banner de Error
            state.errorMessage?.let { errorMsg ->
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TicketFlowErrorBanner(
                        message = errorMsg,
                        onDismiss = viewModel::dismissMessages
                    )
                    TextButton(
                        onClick = viewModel::cargarTickets,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reintentar consulta")
                    }
                }
            }

            when {
                state.isLoading && state.tickets.isEmpty() -> {
                    TicketFlowLoading(
                        message = "Cargando tickets de todos los usuarios...",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                state.tickets.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No se encontraron tickets",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No hay registros que coincidan con los filtros seleccionados.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.tickets, key = { it.id }) { ticket ->
                            AdminTicketCard(
                                ticket = ticket,
                                isUpdating = state.updatingTicketId == ticket.id,
                                onOpenDetail = { onNavigateToDetail(ticket.id) },
                                onChangeStatus = { ticketParaCambiarEstado = ticket },
                                onChangePriority = { ticketParaCambiarPrioridad = ticket },
                                onDelete = { viewModel.eliminarTicket(ticket.id) },
                                onEdit = { viewModel.iniciarEdicion(ticket.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal para Cambiar Estado
    ticketParaCambiarEstado?.let { ticket ->
        AlertDialog(
            onDismissRequest = { ticketParaCambiarEstado = null },
            title = { Text("Actualizar Estado · Ticket #${ticket.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selecciona el nuevo estado para esta incidencia:", style = MaterialTheme.typography.bodyMedium)
                    listOf("Pendiente", "En proceso", "Solucionado").forEach { estado ->
                        OutlinedButton(
                            onClick = {
                                viewModel.actualizarEstadoTicket(ticket.id, estado)
                                ticketParaCambiarEstado = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(estado)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { ticketParaCambiarEstado = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para Cambiar Prioridad
    ticketParaCambiarPrioridad?.let { ticket ->
        AlertDialog(
            onDismissRequest = { ticketParaCambiarPrioridad = null },
            title = { Text("Actualizar Prioridad · Ticket #${ticket.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selecciona el nuevo nivel de prioridad:", style = MaterialTheme.typography.bodyMedium)
                    listOf(
                        "Baja" to "Baja",
                        "Media" to "Media",
                        "Alta" to "Alta",
                        "Crítica" to "Critica"
                    ).forEach { (label, value) ->
                        OutlinedButton(
                            onClick = {
                                viewModel.actualizarPrioridadTicket(ticket.id, value)
                                ticketParaCambiarPrioridad = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { ticketParaCambiarPrioridad = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (ticketParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { ticketParaEliminar = null },
            title = { Text("Eliminar ticket #${ticketParaEliminar!!.id}") },
            text = { Text("¿Estás seguro de que deseas eliminar este ticket? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarTicket(ticketParaEliminar!!.id)
                        ticketParaEliminar = null
                    }
                ) {
                    Text("Eliminar", color = PriorityCritical)
                }
            },
            dismissButton = {
                TextButton(onClick = { ticketParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (state.editingTicket != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarEdicion,
            title = { Text("Editar Ticket #${state.editingTicket!!.id}") },
            text = { Text("Para editar campos completos, usa los badges de prioridad y estado en la tarjeta para cambios rápidos.") },
            confirmButton = {
                TextButton(onClick = viewModel::cancelarEdicion) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun AdminTicketCard(
    ticket: AdminTicketItem,
    isUpdating: Boolean,
    onOpenDetail: () -> Unit,
    onChangeStatus: () -> Unit,
    onChangePriority: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (pColor, pBg) = adminPrioridadColor(ticket.prioridad)
    val (eColor, eBg) = adminEstadoColor(ticket.estado)
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#${ticket.id}",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryNavy,
                    fontWeight = FontWeight.Bold
                )

                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryNavy)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BadgeBox(
                            text = if (ticket.prioridad == "Critica") "Crítica" else ticket.prioridad.ifEmpty { "Normal" },
                            textColor = pColor,
                            backgroundColor = pBg,
                            onClick = onChangePriority
                        )
                        BadgeBox(
                            text = ticket.estado.ifEmpty { "Pendiente" },
                            textColor = eColor,
                            backgroundColor = eBg,
                            onClick = onChangeStatus
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ticket.tipoIncidencia.ifEmpty { ticket.descripcion.take(40) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (ticket.descripcion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = ticket.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CardStroke)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ticket.equipo.ifEmpty { "Sin equipo" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones",
                                tint = TextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar ticket") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar ticket") },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = PriorityCritical
                                    )
                                }
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatAdminFecha(ticket.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar ticket #${ticket.id}") },
            text = { Text("¿Estás seguro de que deseas eliminar este ticket? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar", color = PriorityCritical)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BadgeBox(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = textColor,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

private fun formatAdminFecha(iso: String): String = try {
    OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
} catch (e: Exception) {
    iso.take(10)
}

private fun adminPrioridadColor(prioridad: String): Pair<Color, Color> = when (prioridad.lowercase()) {
    "baja" -> StatusSuccessGreen to StatusSuccessGreenBg
    "media" -> PriorityMedium to PriorityMediumBg
    "alta" -> PriorityHigh to PriorityHighBg
    "critica", "crítica" -> PriorityCritical to PriorityCriticalBg
    else -> TextSecondary to CardStroke
}

private fun adminEstadoColor(estado: String): Pair<Color, Color> = when (estado.lowercase()) {
    "pendiente" -> StatusPending to StatusPendingBg
    "en proceso" -> StatusInfoBlue to StatusInfoBlueBg
    "solucionado" -> StatusSuccessGreen to StatusSuccessGreenBg
    else -> TextSecondary to CardStroke
}
