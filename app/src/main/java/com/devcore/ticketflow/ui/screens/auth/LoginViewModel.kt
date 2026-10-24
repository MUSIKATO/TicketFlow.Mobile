package com.devcore.ticketflow.ui.screens.auth

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.Profile
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
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
    val isCheckingSession: Boolean = true
)

sealed interface LoginEvent {
    data class NavigateToDashboard(val role: String) : LoginEvent
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        comprobarSesion()
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
            emailErr = "El correo electrónico es requerido"
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailErr = "Introduce un correo electrónico válido"
            hasError = true
        }

        if (password.isEmpty()) {
            passwordErr = "La contraseña es requerida"
            hasError = true
        } else if (password.length < 6) {
            passwordErr = "La contraseña debe tener al menos 6 caracteres"
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
                // 1. Autenticación con Supabase Auth
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                // 2. Consulta del perfil y rol
                obtenerPerfilYRedirigir()

            } catch (e: Exception) {
                Log.e("TicketFlowAuth", "Fallo al iniciar sesión: ${e.message}")
                val userFriendlyMessage = when {
                    e.message?.contains("email_not_confirmed", ignoreCase = true) == true ||
                    e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                        "Debes confirmar tu correo electrónico antes de iniciar sesión. Revisa tu bandeja de entrada y sigue el enlace de confirmación."
                    e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                        "Correo electrónico o contraseña incorrectos"
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("connection", ignoreCase = true) == true ->
                        "Error de conexión. Verifica tu conexión a internet"
                    else ->
                        "No se pudo iniciar sesión: ${e.message}"
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

    private suspend fun obtenerPerfilYRedirigir() {
        val usuario = SupabaseClient.client.auth.currentUserOrNull()
        if (usuario == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    generalError = "No se pudo recuperar la sesión del usuario"
                )
            }
            return
        }

        try {
            val perfil = SupabaseClient.client
                .from("profiles")
                .select {
                    filter {
                        eq("id", usuario.id)
                    }
                }
                .decodeSingle<Profile>()

            SupabaseClient.perfil = perfil

            val rol = perfil.rol.lowercase()
            if (rol == "admin" || rol == "usuario") {
                _uiState.update { it.copy(isLoading = false, isCheckingSession = false) }
                _events.emit(LoginEvent.NavigateToDashboard(rol))
            } else {
                SupabaseClient.client.auth.signOut()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Rol de usuario '$rol' no reconocido en el sistema"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("TicketFlowAuth", "Fallo al obtener el perfil de usuario", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    generalError = "Error al consultar el perfil del usuario. Contacte a soporte"
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
                Log.e("TicketFlowAuth", "Error al comprobar sesión persistida", e)
                _uiState.update { it.copy(isCheckingSession = false) }
            }
        }
    }
}
