package com.devcore.ticketflow.ui.screens.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.Profile
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

data class UserProfileUiState(
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed interface UserProfileEvent {
    object SignOut : UserProfileEvent
}

class UserProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<UserProfileEvent>()
    val events = _events

    init {
        cargarPerfil()
    }

    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun onCorreoChange(correo: String) {
        _uiState.update { it.copy(correo = correo) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun obtenerUserId(): String? {
        return SupabaseClient.perfil?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id
    }

    fun cargarPerfil() {
        val perfil = SupabaseClient.perfil
        if (perfil != null) {
            _uiState.update {
                it.copy(
                    nombre = perfil.nombre,
                    correo = perfil.correo,
                    rol = perfil.rol
                )
            }
            return
        }

        val userId = obtenerUserId()
        if (userId == null) {
            _uiState.update {
                it.copy(errorMessage = "Sesión no disponible")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val perfil = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("profiles").select {
                        filter { eq("id", userId) }
                    }.decodeSingle<Profile>()
                }

                SupabaseClient.perfil = perfil

                _uiState.update {
                    it.copy(
                        nombre = perfil.nombre,
                        correo = perfil.correo,
                        rol = perfil.rol,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Fallo al cargar perfil", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar el perfil"
                    )
                }
            }
        }
    }

    fun guardarCambios() {
        val state = _uiState.value
        val usuarioId = obtenerUserId()

        if (usuarioId == null) {
            _uiState.update { it.copy(errorMessage = "Sesión no disponible, vuelve a iniciar sesión") }
            return
        }

        if (state.nombre.trim().isEmpty() || state.correo.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El nombre y el correo son obligatorios") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val payload = buildJsonObject {
                    put("nombre", JsonPrimitive(state.nombre.trim()))
                    put("correo", JsonPrimitive(state.correo.trim()))
                }

                withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("profiles")
                        .update(payload) { filter { eq("id", usuarioId) } }
                }

                // Actualizar en memoria
                SupabaseClient.perfil = SupabaseClient.perfil?.copy(
                    nombre = state.nombre.trim(),
                    correo = state.correo.trim()
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Información actualizada correctamente"
                    )
                }
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Fallo al actualizar perfil", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo actualizar la información"
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                SupabaseClient.perfil = null
                _events.emit(UserProfileEvent.SignOut)
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Fallo al cerrar sesión", e)
                _uiState.update {
                    it.copy(errorMessage = "No se pudo cerrar sesión")
                }
            }
        }
    }
}
