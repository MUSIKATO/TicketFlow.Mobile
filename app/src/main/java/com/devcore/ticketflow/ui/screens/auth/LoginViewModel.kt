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

    // Proteccin contra mltiples emisiones concurrentes
    @Volatile
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
                throw e
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
                Log.d("TicketFlowAuth", "Iniciando flujo Google OAuth con redirectUrl ticketflow://auth/callback")
                SupabaseClient.client.auth.signInWith(
                    provider = Google,
                    redirectUrl = "ticketflow://auth/callback"
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                _uiState.update { it.copy(isGoogleLoading = false) }
                throw e
            } catch (e: Exception) {
                Log.e("TicketFlowAuth", "Fallo al invocar signInWith(Google): ${e.message}")
                val userFriendlyMessage = when {
                    e.message?.contains("provider is not enabled", ignoreCase = true) == true ||
                    e.message?.contains("provider disabled", ignoreCase = true) == true ||
                    e.message?.contains("missing OAuth secret", ignoreCase = true) == true ->
                        "El proveedor Google no est configurado o falta el Client Secret en Supabase. Contacte al administrador."
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
                Log.d("TicketFlowAuth", "sessionStatus recibido en LoginViewModel: $status (isNavigating=$isNavigating)")
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val uid = status.session.user?.id?.take(8) ?: "N/A"
                        Log.d("TicketFlowAuth", "Sesin Authenticated detectada. UID parcial: $uid, source: ${status.source}")
                        if (!isNavigating) {
                            obtenerPerfilYRedirigir()
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
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
            Log.e("TicketFlowAuth", "obtenerPerfilYRedirigir: currentUserOrNull() retorn null")
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

        val uidParcial = usuario.id.take(8)
        Log.d("TicketFlowAuth", "Iniciando consulta a tabla profiles para UID parcial: $uidParcial, email: ${usuario.email}")

        try {
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
                    Log.d("TicketFlowAuth", "Perfil obtenido exitosamente en intento ${intentos + 1}: ${perfil.nombre}, Rol: ${perfil.rol}")
                } catch (e: Exception) {
                    intentos++
                    Log.w("TicketFlowAuth", "Intento $intentos de $maxIntentos fall al consultar profiles: ${e.message}")
                    if (intentos < maxIntentos) {
                        delay(600)
                    } else {
                        throw e
                    }
                }
            }

            if (perfil == null) {
                throw IllegalStateException("El perfil del usuario no fue encontrado tras $maxIntentos intentos.")
            }

            // Validación defensiva: el UID del perfil debe coincidir con el usuario autenticado
            if (perfil.id != usuario.id) {
                Log.e("TicketFlowAuth", "SEGURIDAD: Mismatch entre UID de auth (${usuario.id}) e UID de perfil (${perfil.id})")
                throw SecurityException("El UID del perfil no coincide con el usuario autenticado")
            }

            SupabaseClient.perfil = perfil

            // Validación defensiva: rol no puede ser null ni vacío
            val rolRaw = perfil.rol?.trim() ?: ""
            if (rolRaw.isEmpty()) {
                Log.w("TicketFlowAuth", "Rol vacío o null detectado para usuario ${usuario.id}. Cerrando sesión preventiva.")
                isNavigating = false
                SupabaseClient.client.auth.signOut()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isGoogleLoading = false,
                        isCheckingSession = false,
                        generalError = "El rol del usuario no está configurado correctamente. Contacte al administrador."
                    )
                }
                return
            }

            val rol = rolRaw.lowercase()
            if (rol == "admin" || rol == "usuario") {
                Log.d("TicketFlowAuth", "Redirigiendo usuario al dashboard para rol: $rol")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isGoogleLoading = false,
                        isCheckingSession = false,
                        generalError = null
                    )
                }
                _events.emit(LoginEvent.NavigateToDashboard(rol))
            } else {
                Log.w("TicketFlowAuth", "Rol no reconocido: $rol. Cerrando sesión preventiva.")
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
            Log.e("TicketFlowAuth", "Error al consultar perfil del usuario: ${e.message}", e)
            isNavigating = false

            // NO cerrar sesin inmediatamente si hubo error de perfil en Google para permitir diagnstico claro
            // y que el usuario vea el mensaje exacto en pantalla
            val errorMsg = if (e.message?.contains("No elements", ignoreCase = true) == true ||
                e.message?.contains("IllegalStateException", ignoreCase = true) == true ||
                e.message?.contains("PGRST116", ignoreCase = true) == true) {
                "Tu cuenta de Google () fue autenticada, pero no tiene una fila en la tabla 'profiles'. Contacte al administrador para registrar su rol."
            } else {
                "Error al consultar el perfil del usuario (). Verifique su conexin o contacte a soporte."
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
                Log.d("TicketFlowAuth", "comprobarSesion: Esperando inicializacin de Supabase Auth...")
                SupabaseClient.client.auth.awaitInitialization()
                val usuario = SupabaseClient.client.auth.currentUserOrNull()
                if (usuario != null) {
                    val uidParcial = usuario.id.take(8)
                    Log.d("TicketFlowAuth", "Sesin activa detectada en comprobarSesion para UID parcial: $uidParcial")
                    obtenerPerfilYRedirigir()
                } else {
                    Log.d("TicketFlowAuth", "No hay sesin activa persistida.")
                    _uiState.update { it.copy(isCheckingSession = false) }
                }
            } catch (e: Exception) {
                Log.e("TicketFlowAuth", "Error al comprobar sesin persistida", e)
                _uiState.update { it.copy(isCheckingSession = false) }
            }
        }
    }
}