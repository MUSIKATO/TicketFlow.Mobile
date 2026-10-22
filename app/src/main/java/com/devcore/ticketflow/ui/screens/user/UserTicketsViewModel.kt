package com.devcore.ticketflow.ui.screens.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class TicketItem(
    val id: Long = 0,
    val equipo: String = "",
    val tipo_incidencia: String = "",
    val descripcion: String = "",
    val prioridad: String = "",
    val estado: String = "",
    val created_at: String = ""
)

data class UserTicketsUiState(
    val tickets: List<TicketItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedFilter: String? = null, // null = "Todos", "Pendiente", "En proceso", "Solucionado"
    val errorMessage: String? = null
)

class UserTicketsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserTicketsUiState())
    val uiState: StateFlow<UserTicketsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { cargarTickets() }
    }

    fun onFilterSelected(filter: String?) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        viewModelScope.launch { cargarTickets() }
    }

    fun refrescar() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            // Ahora cargarTickets() es suspend: esta línea espera a que termine la carga
            cargarTickets()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun obtenerUserId(): String? {
        return SupabaseClient.perfil?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id
    }

    private suspend fun cargarTickets() {
        val userId = obtenerUserId()
        if (userId == null) {
            Log.e("UserTicketsVM", "Usuario nulo, no se pudieron cargar tickets")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "No se pudo identificar el usuario"
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val filtro = _uiState.value.selectedFilter
            val tickets = withContext(Dispatchers.IO) {
                val query = SupabaseClient.client.postgrest.from("tickets").select {
                    filter {
                        eq("profile", userId)
                        if (filtro != null) eq("estado", filtro)
                    }
                    order("created_at", Order.DESCENDING)
                }
                kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = true }
                    .decodeFromString<List<TicketItem>>(query.data)
            }

            _uiState.update {
                it.copy(
                    tickets = tickets,
                    isLoading = false
                )
            }
        } catch (e: CancellationException) {
            throw e // si la pantalla se cierra, la corrutina debe cancelarse normalmente
        } catch (e: Exception) {
            Log.e("UserTicketsVM", "Fallo al cargar tickets", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "No se pudieron cargar los tickets"
                )
            }
        }
    }
}