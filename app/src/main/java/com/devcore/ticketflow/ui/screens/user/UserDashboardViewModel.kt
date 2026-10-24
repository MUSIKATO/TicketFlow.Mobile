package com.devcore.ticketflow.ui.screens.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.Profile
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class DashboardTicket(
    val id: Long = 0,
    val descripcion: String = "",
    val prioridad: String = "",
    val estado: String = ""
)

data class UserDashboardUiState(
    val userName: String = "Usuario",
    val pendingCount: Int = 0,
    val inProcessCount: Int = 0,
    val resolvedCount: Int = 0,
    val isMetricsLoading: Boolean = false,
    val tickets: List<DashboardTicket> = emptyList(),
    val isTicketsLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedFilter: String? = null, // null = "Todos", "Pendiente", "En proceso", "Solucionado"
    val errorMessage: String? = null
)

class UserDashboardViewModel : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val _uiState = MutableStateFlow(UserDashboardUiState())
    val uiState: StateFlow<UserDashboardUiState> = _uiState.asStateFlow()

    init {
        try {
            cargarDatos()
        } catch (e: Exception) {
            Log.e("UserDashboardVM", "Error en init del ViewModel", e)
            _uiState.update { it.copy(errorMessage = "Error al inicializar el dashboard") }
        }
    }

    fun cargarDatos() {
        try {
            val perfil = SupabaseClient.perfil
            if (perfil != null) {
                _uiState.update { it.copy(userName = perfil.nombre) }
            }

            cargarMetricas()
            cargarTicketsRecientes()
        } catch (e: Exception) {
            Log.e("UserDashboardVM", "Error al cargar datos iniciales", e)
            _uiState.update { it.copy(errorMessage = "Error al cargar datos iniciales") }
        }
    }

    fun onFilterSelected(filter: String?) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        cargarTicketsRecientes()
    }

    fun refrescar() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            cargarMetricas()
            cargarTicketsRecientes()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun obtenerUserId(): String? {
        return SupabaseClient.perfil?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id
    }

    private fun cargarMetricas() {
        viewModelScope.launch {
            val userId = obtenerUserId()
            if (userId == null) {
                Log.e("UserDashboardVM", "Perfil o usuario nulo, no se pudieron cargar métricas")
                return@launch
            }

            _uiState.update { it.copy(isMetricsLoading = true) }
            try {
                // Si el perfil no estaba en memoria, recuperarlo
                if (SupabaseClient.perfil == null) {
                    val perfilRecuperado = withContext(Dispatchers.IO) {
                        SupabaseClient.client.from("profiles").select {
                            filter { eq("id", userId) }
                        }.decodeSingle<Profile>()
                    }
                    SupabaseClient.perfil = perfilRecuperado
                    _uiState.update { it.copy(userName = perfilRecuperado.nombre) }
                }

                val (pendientes, enProceso, solucionados) = withContext(Dispatchers.IO) {
                    val inicioMes = LocalDate.now().withDayOfMonth(1)
                        .atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    val inicioSiguienteMes = LocalDate.now().plusMonths(1).withDayOfMonth(1)
                        .atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    Triple(
                        contarEstado(userId, "Pendiente", null, null),
                        contarEstado(userId, "En proceso", null, null),
                        contarEstado(userId, "Solucionado", inicioMes, inicioSiguienteMes)
                    )
                }

                _uiState.update {
                    it.copy(
                        pendingCount = pendientes,
                        inProcessCount = enProceso,
                        resolvedCount = solucionados,
                        isMetricsLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("UserDashboardVM", "Fallo al cargar métricas", e)
                _uiState.update {
                    it.copy(
                        isMetricsLoading = false,
                        errorMessage = "No se pudieron actualizar las métricas"
                    )
                }
            }
        }
    }

    private suspend fun contarEstado(userId: String, estado: String, desde: String?, hasta: String?): Int {
        val result = SupabaseClient.client.postgrest.from("tickets").select {
            count(Count.EXACT)
            filter {
                eq("profile", userId)
                eq("estado", estado)
                if (desde != null) gte("created_at", desde)
                if (hasta != null) lt("created_at", hasta)
            }
        }
        return result.countOrNull()?.toInt() ?: 0
    }

    private fun cargarTicketsRecientes() {
        viewModelScope.launch {
            val userId = obtenerUserId()
            if (userId == null) {
                Log.e("UserDashboardVM", "Usuario nulo, no se pudieron cargar tickets recientes")
                return@launch
            }

            _uiState.update { it.copy(isTicketsLoading = true) }
            try {
                val filtro = _uiState.value.selectedFilter
                val tickets = withContext(Dispatchers.IO) {
                    json.decodeFromString<List<DashboardTicket>>(
                        SupabaseClient.client.postgrest.from("tickets").select {
                            filter {
                                eq("profile", userId)
                                if (filtro != null) eq("estado", filtro)
                            }
                            order("created_at", Order.DESCENDING)
                            limit(3)
                        }.data
                    )
                }

                _uiState.update {
                    it.copy(
                        tickets = tickets,
                        isTicketsLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("UserDashboardVM", "Fallo al cargar tickets recientes", e)
                _uiState.update {
                    it.copy(
                        isTicketsLoading = false,
                        errorMessage = "No se pudieron cargar los tickets recientes"
                    )
                }
            }
        }
    }
}
