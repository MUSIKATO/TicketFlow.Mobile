package com.devcore.ticketflow.ui.screens.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class AdminTicketItem(
    val id: Long = 0,
    val equipo: String = "",
    @SerialName("tipo_incidencia")
    val tipoIncidencia: String = "",
    val descripcion: String = "",
    val prioridad: String = "",
    val estado: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    val profile: String? = null,
    @SerialName("evidencia_url")
    val evidenciaUrl: String? = null
)

data class AdminTicketsUiState(
    val tickets: List<AdminTicketItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedStatusFilter: String? = null, // null = Todos, "Pendiente", "En proceso", "Solucionado"
    val selectedPriorityFilter: String? = null, // null = Todas, "Baja", "Media", "Alta", "Critica"
    val updatingTicketId: Long? = null,
    val actionMessage: String? = null,
    val errorMessage: String? = null,
    val editingTicket: AdminTicketItem? = null
)

class AdminTicketsViewModel : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val _uiState = MutableStateFlow(AdminTicketsUiState())
    val uiState: StateFlow<AdminTicketsUiState> = _uiState.asStateFlow()

    init {
        cargarTickets()
    }

    fun onStatusFilterSelected(status: String?) {
        if (_uiState.value.selectedStatusFilter == status) return
        _uiState.update { it.copy(selectedStatusFilter = status) }
        cargarTickets()
    }

    fun onPriorityFilterSelected(priority: String?) {
        if (_uiState.value.selectedPriorityFilter == priority) return
        _uiState.update { it.copy(selectedPriorityFilter = priority) }
        cargarTickets()
    }

    fun restablecerFiltros() {
        if (_uiState.value.selectedStatusFilter == null && _uiState.value.selectedPriorityFilter == null) return
        _uiState.update { it.copy(selectedStatusFilter = null, selectedPriorityFilter = null) }
        cargarTickets()
    }

    fun refrescar() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null, actionMessage = null) }
        viewModelScope.launch {
            cargarTicketsInternal()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun cargarTickets() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            cargarTicketsInternal()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun cargarTicketsInternal() {
        try {
            val status = _uiState.value.selectedStatusFilter
            val priority = _uiState.value.selectedPriorityFilter

            val lista = withContext(Dispatchers.IO) {
                val data = SupabaseClient.client.postgrest.from("tickets").select {
                    filter {
                        if (status != null) {
                            eq("estado", status)
                        }
                        if (priority != null) {
                            eq("prioridad", priority)
                        }
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }.data

                json.decodeFromString<List<AdminTicketItem>>(data)
            }

            _uiState.update { it.copy(tickets = lista, errorMessage = null) }
        } catch (e: Exception) {
            Log.e("AdminTicketsViewModel", "Error al cargar tickets globales", e)
            _uiState.update {
                it.copy(errorMessage = "No se pudieron obtener los tickets. Verifica la conexión.")
            }
        }
    }

    fun actualizarEstadoTicket(ticketId: Long, nuevoEstado: String) {
        val validStates = listOf("Pendiente", "En proceso", "Solucionado")
        if (nuevoEstado !in validStates) return

        _uiState.update { it.copy(updatingTicketId = ticketId, errorMessage = null, actionMessage = null) }

        viewModelScope.launch {
            try {
                val payload = buildJsonObject {
                    put("estado", JsonPrimitive(nuevoEstado))
                }

                withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("tickets")
                        .update(payload) {
                            filter { eq("id", ticketId) }
                        }
                }

                // Actualizar estado confirmado localmente
                _uiState.update { state ->
                    val updated = state.tickets.map {
                        if (it.id == ticketId) it.copy(estado = nuevoEstado) else it
                    }
                    state.copy(
                        tickets = updated,
                        updatingTicketId = null,
                        actionMessage = "Ticket #$ticketId actualizado a '$nuevoEstado'"
                    )
                }
            } catch (e: Exception) {
                Log.e("AdminTicketsViewModel", "Error al actualizar estado de ticket #$ticketId", e)
                _uiState.update {
                    it.copy(
                        updatingTicketId = null,
                        errorMessage = "Error al actualizar el estado del ticket en Supabase."
                    )
                }
            }
        }
    }

    fun actualizarPrioridadTicket(ticketId: Long, nuevaPrioridad: String) {
        val validPriorities = listOf("Baja", "Media", "Alta", "Critica")
        if (nuevaPrioridad !in validPriorities) return

        _uiState.update { it.copy(updatingTicketId = ticketId, errorMessage = null, actionMessage = null) }

        viewModelScope.launch {
            try {
                val payload = buildJsonObject {
                    put("prioridad", JsonPrimitive(nuevaPrioridad))
                }

                withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("tickets")
                        .update(payload) {
                            filter { eq("id", ticketId) }
                        }
                }

                _uiState.update { state ->
                    val updated = state.tickets.map {
                        if (it.id == ticketId) it.copy(prioridad = nuevaPrioridad) else it
                    }
                    state.copy(
                        tickets = updated,
                        updatingTicketId = null,
                        actionMessage = "Prioridad del Ticket #$ticketId cambiada a '$nuevaPrioridad'"
                    )
                }
            } catch (e: Exception) {
                Log.e("AdminTicketsViewModel", "Error al actualizar prioridad de ticket #$ticketId", e)
                _uiState.update {
                    it.copy(
                        updatingTicketId = null,
                        errorMessage = "Error al actualizar la prioridad del ticket en Supabase."
                    )
                }
            }
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(errorMessage = null, actionMessage = null) }
    }

    fun iniciarEdicion(ticketId: Long) {
        val ticket = _uiState.value.tickets.find { it.id == ticketId }
        if (ticket != null) {
            _uiState.update { it.copy(editingTicket = ticket) }
        } else {
            viewModelScope.launch {
                cargarTicketParaEdicion(ticketId)
            }
        }
    }

    private suspend fun cargarTicketParaEdicion(ticketId: Long) {
        try {
            val ticket = withContext(Dispatchers.IO) {
                val data = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        filter { eq("id", ticketId) }
                    }.data
                json.decodeFromString<List<AdminTicketItem>>(data).firstOrNull()
            }
            if (ticket != null) {
                _uiState.update { it.copy(editingTicket = ticket) }
            } else {
                _uiState.update { it.copy(errorMessage = "No se encontró el ticket #$ticketId") }
            }
        } catch (e: Exception) {
            Log.e("AdminTicketsViewModel", "Error al cargar ticket #$ticketId para edición", e)
            _uiState.update { it.copy(errorMessage = "Error al cargar el ticket para edición") }
        }
    }

    fun cancelarEdicion() {
        _uiState.update { it.copy(editingTicket = null) }
    }

    fun guardarEdicion(ticketEditado: AdminTicketItem) {
        val editingTicket = _uiState.value.editingTicket ?: return
        _uiState.update { it.copy(updatingTicketId = editingTicket.id, errorMessage = null) }

        viewModelScope.launch {
            try {
                val payload = buildJsonObject {
                    put("equipo", JsonPrimitive(ticketEditado.equipo))
                    put("tipo_incidencia", JsonPrimitive(ticketEditado.tipoIncidencia))
                    put("descripcion", JsonPrimitive(ticketEditado.descripcion))
                    put("prioridad", JsonPrimitive(ticketEditado.prioridad))
                    put("estado", JsonPrimitive(ticketEditado.estado))
                }

                withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("tickets")
                        .update(payload) {
                            filter { eq("id", editingTicket.id) }
                        }
                }

                _uiState.update { state ->
                    val updated = state.tickets.map {
                        if (it.id == editingTicket.id) ticketEditado else it
                    }
                    state.copy(
                        tickets = updated,
                        editingTicket = null,
                        updatingTicketId = null,
                        actionMessage = "Ticket #${editingTicket.id} actualizado correctamente"
                    )
                }
                Log.d("AdminTicketsViewModel", "Ticket #${editingTicket.id} actualizado")
            } catch (e: Exception) {
                Log.e("AdminTicketsViewModel", "Error al actualizar ticket #${editingTicket.id}", e)
                _uiState.update {
                    it.copy(
                        updatingTicketId = null,
                        errorMessage = "Error al actualizar el ticket en Supabase."
                    )
                }
            }
        }
    }

    fun eliminarTicket(ticketId: Long) {
        _uiState.update { it.copy(updatingTicketId = ticketId, errorMessage = null, actionMessage = null) }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("tickets")
                        .delete {
                            filter { eq("id", ticketId) }
                        }
                }

                _uiState.update { state ->
                    val updated = state.tickets.filter { it.id != ticketId }
                    state.copy(
                        tickets = updated,
                        updatingTicketId = null,
                        actionMessage = "Ticket #$ticketId eliminado correctamente"
                    )
                }
                Log.d("AdminTicketsViewModel", "Ticket #$ticketId eliminado")
            } catch (e: Exception) {
                Log.e("AdminTicketsViewModel", "Error al eliminar ticket #$ticketId", e)
                _uiState.update {
                    it.copy(
                        updatingTicketId = null,
                        errorMessage = "Error al eliminar el ticket en Supabase."
                    )
                }
            }
        }
    }
}
