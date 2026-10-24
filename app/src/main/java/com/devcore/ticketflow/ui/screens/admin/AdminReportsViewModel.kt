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

data class AdminReportsUiState(
    val totalTickets: Int = 0,
    val criticalTickets: Int = 0,
    val pendingTickets: Int = 0,
    val inProcessTickets: Int = 0,
    val resolvedTickets: Int = 0,
    val lowPriorityTickets: Int = 0,
    val mediumPriorityTickets: Int = 0,
    val highPriorityTickets: Int = 0,
    val topEquipment: String? = null,
    val topEquipmentCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminReportsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReportsUiState())
    val uiState: StateFlow<AdminReportsUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            cargarEstadisticasReales()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun cargarEstadisticasReales() {
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

                // Por prioridad
                val lowResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        count(Count.EXACT)
                        filter { eq("prioridad", "Baja") }
                        limit(0)
                    }
                val low = lowResult.countOrNull()?.toInt() ?: 0

                val mediumResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        count(Count.EXACT)
                        filter { eq("prioridad", "Media") }
                        limit(0)
                    }
                val medium = mediumResult.countOrNull()?.toInt() ?: 0

                val highResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        count(Count.EXACT)
                        filter { eq("prioridad", "Alta") }
                        limit(0)
                    }
                val high = highResult.countOrNull()?.toInt() ?: 0

                // Equipo con más incidencias
                val equipmentResult = SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        "equipo"
                    }
                val equipmentTickets = equipmentResult.decodeList<MutableMap<String, String>>()
                val equipmentCounts = equipmentTickets.groupingBy { it["equipo"] ?: "Desconocido" }.eachCount()
                val topEquipment = equipmentCounts.maxByOrNull { it.value }
                val topEquipmentName = topEquipment?.key
                val topEquipmentCount = topEquipment?.value ?: 0

                _uiState.update {
                    it.copy(
                        totalTickets = total,
                        criticalTickets = critical,
                        pendingTickets = pending,
                        inProcessTickets = inProcess,
                        resolvedTickets = resolved,
                        lowPriorityTickets = low,
                        mediumPriorityTickets = medium,
                        highPriorityTickets = high,
                        topEquipment = topEquipmentName,
                        topEquipmentCount = topEquipmentCount,
                        errorMessage = null
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AdminReportsVM", "Error al cargar estadísticas", e)
            _uiState.update {
                it.copy(errorMessage = "No se pudieron cargar las estadísticas. Verifica la conexión.")
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
