package com.devcore.ticketflow.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devcore.ticketflow.ui.components.TicketFlowButton
import com.devcore.ticketflow.ui.components.TicketFlowErrorBanner
import com.devcore.ticketflow.ui.components.TicketFlowTextField
import com.devcore.ticketflow.ui.theme.TextPrimary
import com.devcore.ticketflow.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditTicketScreen(
    ticket: AdminTicketItem,
    onCancel: () -> Unit,
    onSave: (AdminTicketItem) -> Unit,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var equipo by remember { mutableStateOf(ticket.equipo) }
    var tipoIncidencia by remember { mutableStateOf(ticket.tipoIncidencia) }
    var descripcion by remember { mutableStateOf(ticket.descripcion) }
    var prioridad by remember { mutableStateOf(ticket.prioridad) }
    var estado by remember { mutableStateOf(ticket.estado) }

    val validPriorities = listOf("Baja", "Media", "Alta", "Critica")
    val validStates = listOf("Pendiente", "En proceso", "Solucionado")

    val isFormValid = equipo.trim().isNotBlank() &&
            tipoIncidencia.trim().isNotBlank() &&
            descripcion.trim().length >= 5 &&
            prioridad in validPriorities &&
            estado in validStates

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Ticket #${ticket.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, enabled = !isSaving) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancelar"
                        )
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
            Text(
                text = "Edita la información del ticket para mantener los registros actualizados.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            errorMessage?.let { msg ->
                TicketFlowErrorBanner(
                    message = msg,
                    onDismiss = onDismissError
                )
            }

            TicketFlowTextField(
                label = "Equipo *",
                value = equipo,
                onValueChange = { equipo = it },
                placeholder = "Laptop, Monitor, Impresora...",
                enabled = !isSaving
            )

            TicketFlowTextField(
                label = "Tipo de incidencia *",
                value = tipoIncidencia,
                onValueChange = { tipoIncidencia = it },
                placeholder = "Hardware, Software, Red...",
                enabled = !isSaving
            )

            TicketFlowTextField(
                label = "Descripción * (mínimo 5 caracteres)",
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = "Describe el problema detalladamente",
                minLines = 4,
                maxLines = 6,
                enabled = !isSaving
            )

            Text(
                text = "Nivel de Prioridad *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                validPriorities.forEach { priority ->
                    val isSelected = prioridad == priority
                    val label = if (priority == "Critica") "Crítica" else priority
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (!isSaving) prioridad = priority },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            Text(
                text = "Estado del Ticket *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                validStates.forEach { state ->
                    val isSelected = estado == state
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (!isSaving) estado = state },
                        label = { Text(state, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }

                TicketFlowButton(
                    text = "Guardar cambios",
                    onClick = {
                        onSave(
                            ticket.copy(
                                equipo = equipo.trim(),
                                tipoIncidencia = tipoIncidencia.trim(),
                                descripcion = descripcion.trim(),
                                prioridad = prioridad,
                                estado = estado
                            )
                        )
                    },
                    isLoading = isSaving,
                    enabled = isFormValid && !isSaving,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        shape = RoundedCornerShape(8.dp)
    )
}
