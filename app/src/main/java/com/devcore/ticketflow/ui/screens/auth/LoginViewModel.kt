package com.devcore.ticketflow.ui.screens.auth

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.Profile
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isCheckingSession: Boolean = true,
    val isGoogleLoading: Boolean = false
)

sealed interface LoginEvent {
    data class NavigateToDashboard(val role: String) : LoginEvent
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    // Proteccin contra mltiples emisiones o navegaciones concurrentes
    private var isNavigating = false

    init {
        comprobarSesion()
        observarEstadoDeSesion()
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = null,
                generalError = null
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun dismissGeneralError() {
        _uiState.update { it.copy(generalError = null) }
    }

    fun iniciarSesion() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        // Validaciones
        var hasError = false
        var emailErr: String? = null
        var passwordErr: String? = null

        if (email.isEmpty()) {
            emailErr = "El correo electrnico es requerido"
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailErr = "Introduce un correo electrnico vlido"
            hasError = true
        }

        if (password.isEmpty()) {
            passwordErr = "La contrasea es requerida"
            hasError = true
        } else if (password.length < 6) {
            passwordErr = "La contrasea debe tener al menos 6 caracteres"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passwordErr,
                    generalError = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                generalError = null,
                emailError = null,
                passwordError = null
            )
        }

        viewModelScope.launch {
            try {
                // 1. Autenticacin con Supabase Auth
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                // 2. Consulta del perfil y rol
                obtenerPerfilYRedirigir()

            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e // Re-throw CancellationException to maintain proper cancellation
            } catch (e: Exception) {
                Log.e("TicketFlowAuth", "Fallo al iniciar sesin: ${e.message}")
                val userFriendlyMessage = when {
                    e.message?.contains("email_not_confirmed", ignoreCase = true) == true ||
                    e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                        "Debes confirmar tu correo electrnico antes de iniciar sesin. Revisa tu bandeja de entrada y sigue el enlace de confirmacin."
                    e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                        "Correo electrnico o contrasea incorrectos"
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("connection", ignoreCase = true) == true ->
                        "Error de conexin. Verifica tu conexin a internet"
                    else ->
                        "No se pudo iniciar sesin: ${e.message}"
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = userFriendlyMessage
                    )
                }
            }
        }
    }

    fun iniciarSesionConGoogle() {
        _uiState.update {
            it.copy(
                isGoogleLoading = true,
                generalError = null
            )
        }

        viewModelScope.launch {
            try {
                // Iniciar autenticacin con Google OAuth usando Supabase Auth SDK 3.5.0
                // Se especifica explcitamente el redirectUrl acordado
                SupabaseClient.client.auth.signInWith(
                    provider = Google,
                    redirectUrl = "ticketflow://auth/callback"
                )

                // El navegador del sistema / Custom Tabs se abrir para completar la autenticacin con Google
                // Al completarse, retornar mediante ticketflow://auth/callback hacia MainActivity
                // observarEstadoDeSesion() detectar SessionStatus.Authenticated y redirigir al dashboard
            } catch (e: kotlinx.coroutines.CancellationException) {
                _uiState.update { it.copy(isGoogleLoading = false) }
                throw e // Re-throw CancellationException para mantener cancelacin correcta
            } catch (e: Exception) {
                Log.e("TicketFlowAuth", "Fallo al iniciar sesin con Google: ${e.message}")
                val userFriendlyMessage = when {
                    e.message?.contains("provider is not enabled", ignoreCase = true) == true ||
                    e.message?.contains("provider disabled", ignoreCase = true) == true ->
                        "El inicio de sesin con Google no est configurado en Supabase. Contacte al administrador."
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("connection", ignoreCase = true) == true ->
                        "Error de conexin. Verifica tu conexin a internet"
                    e.message?.contains("cancelled", ignoreCase = true) == true ||
                    e.message?.contains("canceled", ignoreCase = true) == true ->
                        "Autenticacin con Google cancelada"
                    else ->
                        "No se pudo iniciar sesin con Google: ${e.message}"
                }
                _uiState.update {
                    it.copy(
                        isGoogleLoading = false,
                        generalError = userFriendlyMessage
                    )
                }
            }
        }
    }

    private fun observarEstadoDeSesion() {
        viewModelScope.launch {
            SupabaseClient.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        Log.d("TicketFlowAuth", "Sesin autenticada detectada (source: ${status.source})")
                        // Si estbamos esperando respuesta de login y no hay una navegacin en curso:
                        if (!isNavigating && (_uiState.value.isGoogleLoading || _uiState.value.isLoading || _uiState.value.isCheckingSession)) {
                            obtenerPerfilYRedirigir()
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        // Restablecer flag si el usuario cerr sesin para permitir un prximo login normal
                        isNavigating = false
                        if (!_uiState.value.isCheckingSession) {
                            _uiState.update { it.copy(isGoogleLoading = false, isLoading = false) }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private suspend fun obtenerPerfilYRedirigir() {
        if (isNavigating) return
        isNavigating = true

        val usuario = SupabaseClient.client.auth.currentUserOrNull()
        if (usuario == null) {
            isNavigating = false
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isGoogleLoading = false,
                    isCheckingSession = false,
                    generalError = "No se pudo recuperar la sesin del usuario"
                )
            }
            return
        }

        try {
            // Intentar consultar el perfil. Si fue creado por trigger, podra tardar unos milisegundos en el primer login
            var perfil: Profile? = null
            var intentos = 0
            val maxIntentos = 3

            while (intentos < maxIntentos && perfil == null) {
                try {
                    perfil = SupabaseClient.client
                        .from("profiles")
                        .select {
                            filter {
                                eq("id", usuario.id)
                            }
                        }
                        .decodeSingle<Profile>()
                } catch (e: Exception) {
                    intentos++
                    if (intentos < maxIntentos) {
                        Log.d("TicketFlowAuth", "Perfil no encontrado en intento $intentos, esperando trigger...")
                        delay(500)
                    } else {
                        throw e
                    }
                }
            }

            if (perfil == null) {
                throw IllegalStateException("El perfil del usuario no fue encontrado.")
            }

            SupabaseClient.perfil = perfil

            val rol = perfil.rol.lowercase()
            if (rol == "admin" || rol == "usuario") {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isGoogleLoading = false,
                        isCheckingSession = false
                    )
                }
                _events.emit(LoginEvent.NavigateToDashboard(rol))
            } else {
                isNavigating = false
                SupabaseClient.client.auth.signOut()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isGoogleLoading = false,
                        isCheckingSession = false,
                        generalError = "Rol de usuario '$rol' no reconocido en el sistema"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("TicketFlowAuth", "Fallo al obtener el perfil de usuario", e)
            isNavigating = false
            
            // Cerrar sesin limpia para no dejar al usuario en un estado incoherente
            try {
                SupabaseClient.client.auth.signOut()
            } catch (_: Exception) {}

            val errorMsg = if (e.message?.contains("No elements", ignoreCase = true) == true ||
                e.message?.contains("IllegalStateException", ignoreCase = true) == true) {
                "Tu cuenta de Google fue autenticada, pero an no tiene un perfil registrado en TicketFlow. Contacte al administrador del sistema."
            } else {
                "Error al consultar el perfil del usuario. Contacte a soporte tcnico."
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isGoogleLoading = false,
                    isCheckingSession = false,
                    generalError = errorMsg
                )
            }
        }
    }

    private fun comprobarSesion() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.awaitInitialization()
                val usuario = SupabaseClient.client.auth.currentUserOrNull()
                if (usuario != null) {
                    obtenerPerfilYRedirigir()
                } else {
                    _uiState.update { it.copy(isCheckingSession = false) }
                }
            } catch (e: Exception) {
                Log.e("TicketFlowAuth", "Error al comprobar sesin persistida", e)
                _uiState.update { it.copy(isCheckingSession = false) }
            }
        }
    }
}
