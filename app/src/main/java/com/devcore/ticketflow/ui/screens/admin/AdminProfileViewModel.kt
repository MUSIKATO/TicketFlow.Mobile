package com.devcore.ticketflow.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminProfileUiState(
    val userName: String = "Administrador",
    val userEmail: String = "",
    val userRole: String = "Admin",
    val isLoading: Boolean = false
)

class AdminProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        val perfil = SupabaseClient.perfil
        if (perfil != null) {
            _uiState.update {
                it.copy(
                    userName = perfil.nombre,
                    userEmail = perfil.correo,
                    userRole = perfil.rol
                )
            }
        }
    }
}
