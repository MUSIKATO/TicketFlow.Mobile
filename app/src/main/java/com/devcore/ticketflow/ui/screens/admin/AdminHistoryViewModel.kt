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
import kotlinx.serialization.json.Json

data class AdminHistoryUiState(
    val tickets: List<AdminTicketItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class AdminHistoryViewModel : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val _uiState = MutableStateFlow(AdminHistoryUiState())
    val uiState: StateFlow<AdminHistoryUiState> = _uiState.asStateFlow()

    init {
        cargarHistorial()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refrescar() {
        if (_uiState.value.isRefreshing || _uiState.value.isLoading) return
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            cargarHistorialInternal()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun cargarHistorial() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            cargarHistorialInternal()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun cargarHistorialInternal() {
        try {
            val tickets = withContext(Dispatchers.IO) {
                val data = SupabaseClient.client.postgrest.from("tickets").select {
                    order(column = "created_at", order = Order.DESCENDING)
                }.data
                json.decodeFromString<List<AdminTicketItem>>(data)
            }
            _uiState.update { it.copy(tickets = tickets, errorMessage = null) }
        } catch (e: Exception) {
            Log.e("AdminHistoryViewModel", "Error al cargar historial de tickets desde PostgreSQL", e)
            _uiState.update {
                it.copy(errorMessage = "No se pudo cargar el historial desde Supabase. Verifica la conexión y los permisos.")
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
