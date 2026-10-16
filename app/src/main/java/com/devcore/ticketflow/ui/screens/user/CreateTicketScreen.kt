package com.devcore.ticketflow.ui.screens.user

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.devcore.ticketflow.R
import com.devcore.ticketflow.ui.components.TicketFlowButton
import com.devcore.ticketflow.ui.components.TicketFlowErrorBanner
import com.devcore.ticketflow.ui.theme.PrimaryNavy
import com.devcore.ticketflow.ui.theme.TextPrimary
import com.devcore.ticketflow.ui.theme.TextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("FunctionName")
fun CreateTicketScreen(
    onNavigateBack: () -> Unit,
    onTicketCreated: (ticketId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateTicketViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Lista de equipos del XML
    val equipos = remember {
        context.resources.getStringArray(R.array.opciones_equipos).toList()
    }

    var expanded by remember { mutableStateOf(false) }

    // Launcher para tomar foto (desactivado por ahora)
    // TODO: Implementar captura de foto con ContentResolver cuando se active la funcionalidad
    /*
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // En Compose no usamos FileProvider de la misma forma, simplificamos sin foto por ahora
        }
    }
    */

    // Launcher de permisos (desactivado por ahora)
    /*
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Crear archivo temporal
            val photoFile = File(context.cacheDir, "foto_ticket_${System.currentTimeMillis()}.jpg")
            val photoUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        }
    }
    */

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateTicketEvent.TicketCreated -> {
                    onTicketCreated(event.ticketId)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear nuevo ticket",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = PrimaryNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Error banner
            state.errorMessage?.let { error ->
                TicketFlowErrorBanner(message = error)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Equipo
            Text(
                text = "Equipo afectado *",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.equipo,
                onValueChange = { viewModel.onEquipoChange(it) },
                label = { Text("Seleccione un equipo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                readOnly = true,
                trailingIcon = {
                    androidx.compose.material.icons.Icons.Default.ArrowDropDown.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = "Desplegar"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNavy,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (expanded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column {
                        equipos.forEach { opcion ->
                            androidx.compose.material3.Text(
                                text = opcion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onEquipoChange(opcion)
                                        expanded = false
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Campo "Otro" para equipo
            if (state.mostrarEquipoOtro) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.equipoOtro,
                    onValueChange = viewModel::onEquipoOtroChange,
                    label = { Text("Escribe el equipo afectado") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNavy,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tipo de incidencia
            OutlinedTextField(
                value = state.tipoIncidencia,
                onValueChange = viewModel::onTipoIncidenciaChange,
                label = { Text("Tipo de incidencia *") },
                placeholder = { Text("Ej. Pantalla azul, no imprime...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNavy,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción
            androidx.compose.material3.TextField(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción del problema *") },
                placeholder = { Text("Describa el problema con el mayor detalle posible...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = PrimaryNavy,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Prioridad
            Text(
                text = "Prioridad *",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prioridadChip(
                    label = "Baja",
                    selected = state.prioridad == "Baja",
                    onClick = { viewModel.onPrioridadChange("Baja") },
                    modifier = Modifier.weight(1f)
                )
                prioridadChip(
                    label = "Media",
                    selected = state.prioridad == "Media",
                    onClick = { viewModel.onPrioridadChange("Media") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prioridadChip(
                    label = "Alta",
                    selected = state.prioridad == "Alta",
                    onClick = { viewModel.onPrioridadChange("Alta") },
                    modifier = Modifier.weight(1f)
                )
                prioridadChip(
                    label = "Crítica",
                    selected = state.prioridad == "Critica",
                    onClick = { viewModel.onPrioridadChange("Critica") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Evidencia (simplificado sin funcionalidad de cámara por ahora)
            Text(
                text = "Evidencia (Opcional)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Funcionalidad de cámara en desarrollo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón enviar
            TicketFlowButton(
                text = "Enviar ticket",
                onClick = viewModel::crearTicket,
                isLoading = state.isLoading,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun prioridadChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryNavy,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = PrimaryNavy
        )
    )
}
