package com.devcore.ticketflow.ui.screens.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminDashboardUiState(
    val adminName: String = "Administrador",
    val totalTickets: Int = 0,
    val criticalTickets: Int = 0,
    val pendingTickets: Int = 0,
    val inProcessTickets: Int = 0,
    val resolvedTickets: Int = 0,
    val totalUsers: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class AdminDashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        val perfil = SupabaseClient.perfil
        if (perfil != null) {
            _uiState.update { it.copy(adminName = perfil.nombre) }
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            cargarMetricasReales()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refrescar() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            cargarMetricasReales()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun cargarMetricasReales() {
        try {
            withContext(Dispatchers.IO) {
                // Total tickets
                val totalResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        count(Count.EXACT)
                        limit(0)
                    }
                val total = totalResult.countOrNull()?.toInt() ?: 0

                // Críticos
                val criticalResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        count(Count.EXACT)
                        filter { eq("prioridad", "Critica") }
                        limit(0)
                    }
                val critical = criticalResult.countOrNull()?.toInt() ?: 0

                // Pendientes
                val pendingResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        count(Count.EXACT)
                        filter { eq("estado", "Pendiente") }
                        limit(0)
                    }
                val pending = pendingResult.countOrNull()?.toInt() ?: 0

                // En proceso
                val inProcessResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        count(Count.EXACT)
                        filter { eq("estado", "En proceso") }
                        limit(0)
                    }
                val inProcess = inProcessResult.countOrNull()?.toInt() ?: 0

                // Solucionados
                val resolvedResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        count(Count.EXACT)
                        filter { eq("estado", "Solucionado") }
                        limit(0)
                    }
                val resolved = resolvedResult.countOrNull()?.toInt() ?: 0

                // Total usuarios registrados en profiles
                val usersResult = SupabaseClient.client.postgrest.from("profiles")
                    .select {
                        count(Count.EXACT)
                        limit(0)
                    }
                val users = usersResult.countOrNull()?.toInt() ?: 0

                _uiState.update {
                    it.copy(
                        totalTickets = total,
                        criticalTickets = critical,
                        pendingTickets = pending,
                        inProcessTickets = inProcess,
                        resolvedTickets = resolved,
                        totalUsers = users,
                        errorMessage = null
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AdminDashboardVM", "Error al consultar métricas reales en Supabase", e)
            _uiState.update {
                it.copy(errorMessage = "No se pudieron obtener algunas métricas en vivo. Verifica la conexión.")
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
