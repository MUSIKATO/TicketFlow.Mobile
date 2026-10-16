package com.devcore.ticketflow.ui.screens.user

import android.util.Log
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TicketDetail(
    val id: Long = 0,
    val equipo: String = "",
    @SerialName("tipo_incidencia")
    val tipoIncidencia: String = "",
    val descripcion: String = "",
    val prioridad: String = "",
    val estado: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("evidencia_url")
    val evidenciaUrl: String? = null
)

data class TicketDetailUiState(
    val ticket: TicketDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TicketDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TicketDetailUiState())
    val uiState: StateFlow<TicketDetailUiState> = _uiState.asStateFlow()

    fun cargarTicket(ticketId: Long) {
        val userId = obtenerUserId()
        if (userId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Sesión no disponible, vuelve a iniciar sesión"
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val ticket = withContext(Dispatchers.IO) {
                    val json = kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                    json.decodeFromString<List<TicketDetail>>(
                        SupabaseClient.client.postgrest.from("tickets").select {
                            filter {
                                eq("id", ticketId)
                                eq("profile", userId)
                            }
                            limit(1)
                        }.data
                    ).firstOrNull()
                }

                if (ticket != null) {
                    _uiState.update {
                        it.copy(
                            ticket = ticket,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se encontró el ticket solicitado"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("TicketDetailVM", "Fallo al cargar el ticket $ticketId", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar el ticket"
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun obtenerUserId(): String? {
        return SupabaseClient.perfil?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id
    }
}
