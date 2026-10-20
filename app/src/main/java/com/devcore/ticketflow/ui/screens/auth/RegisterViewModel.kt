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

data class RegisterUiState(
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val correoError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)

sealed interface RegisterEvent {
    data object NavigateToLogin : RegisterEvent
}

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RegisterEvent>()
    val events: SharedFlow<RegisterEvent> = _events.asSharedFlow()

    fun onCorreoChange(newCorreo: String) {
        _uiState.update {
            it.copy(
                correo = newCorreo,
                correoError = null,
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

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPassword = newConfirmPassword,
                confirmPasswordError = null,
                generalError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun dismissGeneralError() {
        _uiState.update { it.copy(generalError = null) }
    }

    fun registrarUsuario() {
        val correo = _uiState.value.correo.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        var hasError = false
        var correoErr: String? = null
        var passwordErr: String? = null
        var confirmPasswordErr: String? = null

        // 1. Validar correo
        if (correo.isEmpty()) {
            correoErr = "El correo electrónico es requerido"
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            correoErr = "Introduce un correo electrónico válido"
            hasError = true
        }

        // 2. Validar contraseña
        if (password.isEmpty()) {
            passwordErr = "La contraseña es requerida"
            hasError = true
        } else if (password.length < 6) {
            passwordErr = "La contraseña debe tener al menos 6 caracteres"
            hasError = true
        }

        // 3. Validar confirmación de contraseña
        if (confirmPassword.isEmpty()) {
            confirmPasswordErr = "Confirma tu contraseña"
            hasError = true
        } else if (password != confirmPassword) {
            confirmPasswordErr = "Las contraseñas no coinciden"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    correoError = correoErr,
                    passwordError = passwordErr,
                    confirmPasswordError = confirmPasswordErr,
                    generalError = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                generalError = null,
                correoError = null,
                passwordError = null,
                confirmPasswordError = null
            )
        }

        viewModelScope.launch {
            try {
                // 1. Registro en Supabase Auth (envía email de confirmación)
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = correo
                    this.password = password
                }

                // 2. Si el registro fue exitoso (no lanzó excepción), navega al login
                // El perfil se creará en el login tras confirmar email
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(RegisterEvent.NavigateToLogin)

            } catch (e: Exception) {
                Log.e("TicketFlowRegister", "Fallo en el registro de usuario", e)
                val userFriendlyMessage = when {
                    e.message?.contains("email_not_confirmed", ignoreCase = true) == true ->
                        "Registro exitoso. Revisa tu email y confirma la cuenta para poder iniciar sesión."

                    e.message?.contains("already registered", ignoreCase = true) == true ||
                    e.message?.contains("User already registered", ignoreCase = true) == true ->
                        "El correo electrónico ya está registrado en el sistema"
                    e.message?.contains("over_email_send_rate_limit", ignoreCase = true) == true ||
                    e.message?.contains("rate limit", ignoreCase = true) == true ->
                        "Demasiados intentos. Límite de envío de correos excedido en Supabase. Espera unos minutos o desactiva 'Confirm email' en la consola de Supabase"
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                        "Error de conexión. Verifica tu conexión a internet"
                    else ->
                        "No se pudo completar el registro: ${e.localizedMessage ?: "Error desconocido"}"
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
}
