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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devcore.ticketflow.ui.components.RoleBadge
import com.devcore.ticketflow.ui.theme.CorporateNavy
import com.devcore.ticketflow.ui.theme.PriorityCritical
import com.devcore.ticketflow.ui.theme.TextPrimary
import com.devcore.ticketflow.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = PriorityCritical
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
            // Error banner
            state.errorMessage?.let { error ->
                com.devcore.ticketflow.ui.components.TicketFlowErrorBanner(message = error)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Success message
            state.successMessage?.let { success ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = success,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Información del perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(CorporateNavy.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CorporateNavy,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.userName,
                            onValueChange = viewModel::onNombreChange,
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSaving,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CorporateNavy,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    } else {
                        Text(
                            text = state.userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.userEmail,
                            onValueChange = viewModel::onCorreoChange,
                            label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSaving,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CorporateNavy,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    } else {
                        Text(
                            text = state.userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    RoleBadge(
                        role = state.userRole,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // Botones de edición
                    if (state.isEditing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::disableEditing,
                                enabled = !state.isSaving,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancelar")
                            }

                            com.devcore.ticketflow.ui.components.TicketFlowButton(
                                text = "Guardar",
                                onClick = viewModel::confirmSaveDialog,
                                isLoading = state.isSaving,
                                enabled = !state.isSaving,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Opciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileOption(
                        icon = Icons.Default.Person,
                        title = "Editar información",
                        subtitle = "Nombre y correo electrónico",
                        onClick = viewModel::enableEditing,
                        enabled = !state.isEditing
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileOption(
                        icon = Icons.Default.Person,
                        title = "Cambiar contraseña",
                        subtitle = "Actualizar credenciales de acceso",
                        onClick = { /* Pendiente de implementación */ },
                        enabled = false
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileOption(
                        icon = Icons.Default.Person,
                        title = "Configuración de notificaciones",
                        subtitle = "Preferencias de alertas",
                        onClick = { /* Pendiente de implementación */ },
                        enabled = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo de confirmación de guardado
    if (state.showConfirmSaveDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissConfirmSaveDialog,
            title = { Text("Guardar cambios") },
            text = { Text("¿Estás seguro de que deseas guardar los cambios en tu perfil?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissConfirmSaveDialog()
                    viewModel.guardarCambios()
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissConfirmSaveDialog) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) CorporateNavy else TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.5f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}
