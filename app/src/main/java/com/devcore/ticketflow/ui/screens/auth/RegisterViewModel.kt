package com.devcore.ticketflow.ui.screens.auth

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.Profile
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
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
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false,
    val requiresEmailConfirmation: Boolean = false
)

sealed interface RegisterEvent {
    data object NavigateToLogin : RegisterEvent
    data class RegistrationCompleted(val requiresConfirmation: Boolean) : RegisterEvent
}

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RegisterEvent>()
    val events: SharedFlow<RegisterEvent> = _events.asSharedFlow()

    fun onNombreChange(newNombre: String) {
        _uiState.update {
            it.copy(
                nombre = newNombre.trim(),
                nombreError = null,
                generalError = null
            )
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail.trim(),
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
                confirmPasswordError = null,
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

    fun registrarse() {
        val nombre = _uiState.value.nombre.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        // Validaciones
        var hasError = false
        var nombreErr: String? = null
        var emailErr: String? = null
        var passwordErr: String? = null
        var confirmErr: String? = null

        if (nombre.isEmpty()) {
            nombreErr = "El nombre es requerido"
            hasError = true
        } else if (nombre.length < 2) {
            nombreErr = "El nombre debe tener al menos 2 caracteres"
            hasError = true
        }

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

        if (confirmPassword.isEmpty()) {
            confirmErr = "La confirmación de contraseña es requerida"
            hasError = true
        } else if (password != confirmPassword) {
            confirmErr = "Las contraseñas no coinciden"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    nombreError = nombreErr,
                    emailError = emailErr,
                    passwordError = passwordErr,
                    confirmPasswordError = confirmErr,
                    generalError = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                generalError = null,
                nombreError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null
            )
        }

        viewModelScope.launch {
            try {
                Log.d("TicketFlowRegister", "Iniciando registro para: $email")

                // 1. Registro en Supabase Auth
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                Log.d("TicketFlowRegister", "Registro completado exitosamente para: $email")

                // 2. El trigger en Supabase crea automáticamente el perfil
                // El usuario debe confirmar su correo antes de poder iniciar sesión

                // 3. Cerrar cualquier sesión temporal que pueda haberse creado
                // Esto asegura que el usuario no pueda iniciar sesión sin confirmar correo
                try {
                    SupabaseClient.client.auth.signOut()
                    Log.d("TicketFlowRegister", "Sesión temporal cerrada después del registro")
                } catch (e: Exception) {
                    // Ignorar error al cerrar sesión
                    Log.d("TicketFlowRegister", "No se pudo cerrar sesión temporal: ${e.message}")
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        registrationSuccess = true,
                        requiresEmailConfirmation = true
                    )
                }
                _events.emit(RegisterEvent.RegistrationCompleted(true))

            } catch (e: Exception) {
                Log.e("TicketFlowRegister", "Fallo al registrar usuario: ${e.message}", e)
                Log.e("TicketFlowRegister", "Stack trace:", e)
                val userFriendlyMessage = when {
                    e.message?.contains("User already registered", ignoreCase = true) == true ||
                    e.message?.contains("duplicate", ignoreCase = true) == true ||
                    e.message?.contains("duplicate key", ignoreCase = true) == true ->
                        "Este correo electrónico ya está registrado"
                    e.message?.contains("weak password", ignoreCase = true) == true ||
                    e.message?.contains("password", ignoreCase = true) == true ->
                        "La contraseña es demasiado débil. Usa al menos 6 caracteres"
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("connection", ignoreCase = true) == true ->
                        "Error de conexión. Verifica tu conexión a internet"
                    e.message?.contains("email_not_confirmed", ignoreCase = true) == true ||
                    e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                        "Error inesperado: la librería intentó iniciar sesión. Por favor intenta de nuevo."
                    else ->
                        "No se pudo completar el registro: ${e.message}"
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

    fun navegarALogin() {
        viewModelScope.launch {
            _events.emit(RegisterEvent.NavigateToLogin)
        }
    }
}
