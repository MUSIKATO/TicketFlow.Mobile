package com.devcore.ticketflow.ui.screens.admin

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class AdminProfileUiState(
    val userName: String = "Administrador",
    val userEmail: String = "",
    val userRole: String = "Admin",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showConfirmSaveDialog: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

class AdminProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    // Valores originales para detectar cambios
    private var originalNombre = ""
    private var originalCorreo = ""

    init {
        cargarPerfil()
    }

    fun onNombreChange(nombre: String) {
        _uiState.update { 
            it.copy(userName = nombre, hasUnsavedChanges = detectarCambios(nombre, it.userEmail))
        }
    }

    fun onCorreoChange(correo: String) {
        _uiState.update { 
            it.copy(userEmail = correo, hasUnsavedChanges = detectarCambios(it.userName, correo))
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun dismissConfirmSaveDialog() {
        _uiState.update { it.copy(showConfirmSaveDialog = false) }
    }

    fun confirmSaveDialog() {
        _uiState.update { it.copy(showConfirmSaveDialog = true) }
    }

    fun enableEditing() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun disableEditing() {
        _uiState.update { 
            it.copy(isEditing = false, userName = originalNombre, userEmail = originalCorreo, hasUnsavedChanges = false)
        }
    }

    private fun detectarCambios(nombre: String, correo: String): Boolean {
        return nombre.trim() != originalNombre || correo.trim() != originalCorreo
    }

    private fun obtenerUserId(): String? {
        return SupabaseClient.perfil?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id
    }

    fun cargarPerfil() {
        val perfil = SupabaseClient.perfil
        if (perfil != null) {
            originalNombre = perfil.nombre
            originalCorreo = perfil.correo
            _uiState.update {
                it.copy(
                    userName = perfil.nombre,
                    userEmail = perfil.correo,
                    userRole = perfil.rol
                )
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

        val nombreTrimmed = state.userName.trim()
        val correoTrimmed = state.userEmail.trim()

        // Validaciones
        if (nombreTrimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El nombre es obligatorio") }
            return
        }

        if (nombreTrimmed.length < 2) {
            _uiState.update { it.copy(errorMessage = "El nombre debe tener al menos 2 caracteres") }
            return
        }

        if (nombreTrimmed.length > 100) {
            _uiState.update { it.copy(errorMessage = "El nombre no puede tener más de 100 caracteres") }
            return
        }

        if (correoTrimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El correo electrónico es obligatorio") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoTrimmed).matches()) {
            _uiState.update { it.copy(errorMessage = "El formato del correo electrónico no es válido") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val payload = buildJsonObject {
                    put("nombre", JsonPrimitive(nombreTrimmed))
                    put("correo", JsonPrimitive(correoTrimmed))
                }

                withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("profiles")
                        .update(payload) { filter { eq("id", usuarioId) } }
                }

                // Actualizar en memoria
                SupabaseClient.perfil = SupabaseClient.perfil?.copy(
                    nombre = nombreTrimmed,
                    correo = correoTrimmed
                )

                originalNombre = nombreTrimmed
                originalCorreo = correoTrimmed

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        successMessage = "Información actualizada correctamente",
                        hasUnsavedChanges = false
                    )
                }
            } catch (e: Exception) {
                Log.e("AdminProfileVM", "Fallo al actualizar perfil", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo actualizar la información"
                    )
                }
            }
        }
    }
}
