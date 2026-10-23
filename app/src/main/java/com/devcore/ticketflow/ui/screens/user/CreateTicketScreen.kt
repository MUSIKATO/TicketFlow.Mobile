package com.devcore.ticketflow.ui.screens.user

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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

    // ---------------------------------------------------------------
    // CÁMARA
    // ---------------------------------------------------------------

    // Ruta del archivo en curso. rememberSaveable la conserva si la pantalla
    // se rota mientras la app de cámara está abierta.
    var pendingPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingPhotoPath?.let { File(it) }
        if (success && file != null) {
            viewModel.onFotoSeleccionada(file)
        } else {
            file?.delete() // el usuario canceló: no dejamos archivos vacíos
        }
        pendingPhotoPath = null
    }

    // Debe declararse DESPUÉS de cameraLauncher, porque lo usa.
    fun launchCamera() {
        val file = File(context.cacheDir, "foto_ticket_${System.currentTimeMillis()}.jpg")
        file.createNewFile()
        pendingPhotoPath = file.absolutePath
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        cameraLauncher.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            viewModel.onError("Se necesita el permiso de cámara para adjuntar una foto")
        }
    }

    fun onTomarFotoClick() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            launchCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ---------------------------------------------------------------

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

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = state.equipo,
                    onValueChange = {},
                    readOnly = true,
                    isError = state.equipoError != null && !state.mostrarEquipoOtro,
                    supportingText = {
                        if (!state.mostrarEquipoOtro) state.equipoError?.let { Text(it) }
                    },
                    label = { Text("Seleccione un equipo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNavy,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    equipos.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                viewModel.onEquipoChange(opcion)
                                expanded = false
                            }
                        )
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
                    isError = state.equipoError != null,
                    supportingText = { state.equipoError?.let { Text(it) } },
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
                isError = state.tipoIncidenciaError != null,
                supportingText = { state.tipoIncidenciaError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNavy,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción
            TextField(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción del problema *") },
                placeholder = { Text("Describa el problema con el mayor detalle posible...") },
                isError = state.descripcionError != null,
                supportingText = { state.descripcionError?.let { Text(it) } },
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

            state.prioridadError?.let { error ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Evidencia
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
                    brush = SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.fotoFile != null) {
                        // Vista previa de la foto tomada
                        Box {
                            AsyncImage(
                                model = state.fotoFile,
                                contentDescription = "Foto de evidencia",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            IconButton(
                                onClick = viewModel::onEliminarFoto,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar foto",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { onTomarFotoClick() }) {
                            Text("Tomar otra foto")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { onTomarFotoClick() }) {
                            Text("Tomar foto")
                        }
                    }
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