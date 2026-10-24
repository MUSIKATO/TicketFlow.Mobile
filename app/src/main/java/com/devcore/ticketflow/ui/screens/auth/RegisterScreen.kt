package com.devcore.ticketflow.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devcore.ticketflow.ui.components.TicketFlowButton
import com.devcore.ticketflow.ui.components.TicketFlowErrorBanner
import com.devcore.ticketflow.ui.components.TicketFlowTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("FunctionName")
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegistrationCompleted: (requiresConfirmation: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RegisterEvent.NavigateToLogin -> onNavigateToLogin()
                is RegisterEvent.RegistrationCompleted -> onRegistrationCompleted(event.requiresConfirmation)
            }
        }
    }

    // Diálogo de confirmación de registro
    if (state.registrationSuccess && state.requiresEmailConfirmation) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Registro exitoso",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "¡Cuenta creada correctamente!\n\nHemos enviado un correo de confirmación a ${state.email}. Por favor, revisa tu bandeja de entrada y sigue el enlace para confirmar tu cuenta.\n\nUna vez confirmado, podrás iniciar sesión.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.navegarALogin() }
                ) {
                    Text("Entendido")
                }
            },
            dismissButton = null
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navegarALogin() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar al inicio de sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Espacio para el logo
            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = "Registro de Usuario",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Únete a TicketFlow para gestionar tus tickets de soporte",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de Formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Banner de error general
                    state.generalError?.let { error ->
                        TicketFlowErrorBanner(message = error)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Campo de Nombre Completo
                    TicketFlowTextField(
                        value = state.nombre,
                        onValueChange = viewModel::onNombreChange,
                        label = "Nombre completo",
                        placeholder = "Tu nombre completo",
                        errorMessage = state.nombreError,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Icono nombre",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Correo Electrónico
                    TicketFlowTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Correo electrónico",
                        placeholder = "ejemplo@perfumeria.com",
                        errorMessage = state.emailError,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Icono correo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Contraseña
                    TicketFlowTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Contraseña",
                        placeholder = "••••••••",
                        errorMessage = state.passwordError,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Icono contraseña",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
                                    contentDescription = if (state.isPasswordVisible) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    }
                                )
                            }
                        },
                        visualTransformation = if (state.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Confirmación de Contraseña
                    TicketFlowTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = "Confirmar contraseña",
                        placeholder = "••••••••",
                        errorMessage = state.confirmPasswordError,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Icono confirmar contraseña",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isConfirmPasswordVisible) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
                                    contentDescription = if (state.isConfirmPasswordVisible) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    }
                                )
                            }
                        },
                        visualTransformation = if (state.isConfirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.registrarse()
                            }
                        ),
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Principal
                    TicketFlowButton(
                        text = "Crear cuenta",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.registrarse()
                        },
                        isLoading = state.isLoading,
                        enabled = !state.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje de información sobre términos
            Text(
                text = "Al registrarte, aceptas nuestros términos y condiciones de uso.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Acceso a Login
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.navegarALogin() },
                    enabled = !state.isLoading
                ) {
                    Text(
                        text = "Inicia sesión",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
