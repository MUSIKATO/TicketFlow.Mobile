package com.devcore.ticketflow.ui.screens.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.OffsetDateTime

enum class PeriodoFiltro(val label: String) {
    HOY("Hoy"),
    ULTIMOS_7_DIAS("Últimos 7 días"),
    ULTIMOS_30_DIAS("Últimos 30 días"),
    TODO("Todo el historial")
}

@Serializable
data class ReportTicketItem(
    val id: Long = 0,
    val equipo: String? = null,
    @SerialName("tipo_incidencia")
    val tipoIncidencia: String? = null,
    val prioridad: String? = null,
    val estado: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

data class EquipoCount(
    val nombre: String,
    val count: Int,
    val frecuenciaRelativa: Float // 0f a 1f respecto al equipo líder
)

data class TipoEquipoCount(
    val tipo: String,
    val count: Int,
    val porcentaje: Float
)

data class EstadoCount(
    val estado: String,
    val count: Int,
    val porcentaje: Float
)

data class PrioridadCount(
    val prioridad: String,
    val count: Int,
    val porcentaje: Float
)

data class AdminReportsUiState(
    val periodoSeleccionado: PeriodoFiltro = PeriodoFiltro.ULTIMOS_30_DIAS,
    val totalTickets: Int = 0,
    val criticalTickets: Int = 0,
    val criticalPercentage: Float = 0f,
    val resolvedTickets: Int = 0,
    val resolvedPercentage: Float = 0f,
    val pendingTickets: Int = 0,
    val pendingPercentage: Float = 0f,
    val inProcessTickets: Int = 0,
    val inProcessPercentage: Float = 0f,
    val incidenciasPorTipoEquipo: List<TipoEquipoCount> = emptyList(),
    val distribucionPorEstado: List<EstadoCount> = emptyList(),
    val equiposConMasIncidencias: List<EquipoCount> = emptyList(),
    val incidenciasPorPrioridad: List<PrioridadCount> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class AdminReportsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReportsUiState())
    val uiState: StateFlow<AdminReportsUiState> = _uiState.asStateFlow()

    // Caché en memoria de todos los tickets para filtrar rápidamente por periodo sin llamadas de red innecesarias
    private var ticketsCache: List<ReportTicketItem> = emptyList()

    init {
        cargarDatos()
    }

    fun onPeriodoSelected(periodo: PeriodoFiltro) {
        if (_uiState.value.periodoSeleccionado == periodo) return
        _uiState.update { it.copy(periodoSeleccionado = periodo) }
        recalcularEstadisticas()
    }

    fun refrescar() {
        if (_uiState.value.isRefreshing || _uiState.value.isLoading) return
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            consultarTicketsSupabase()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun cargarDatos() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            consultarTicketsSupabase()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun consultarTicketsSupabase() {
        try {
            val lista = withContext(Dispatchers.IO) {
                SupabaseClient.client.postgrest.from("tickets")
                    .select(columns = Columns.list("id", "equipo", "tipo_incidencia", "prioridad", "estado", "created_at"))
                    .decodeList<ReportTicketItem>()
            }
            ticketsCache = lista
            recalcularEstadisticas()
        } catch (e: Exception) {
            Log.e("AdminReportsVM", "Error al consultar tickets para reportes", e)
            _uiState.update {
                it.copy(
                    errorMessage = "No se pudieron obtener las estadísticas desde Supabase. Verifica tu conexión."
                )
            }
        }
    }

    private fun recalcularEstadisticas() {
        val periodo = _uiState.value.periodoSeleccionado
        val filtrados = ticketsCache.filter { cumpleFiltroPeriodo(it.createdAt, periodo) }

        val total = filtrados.size

        // Resumen
        val critical = filtrados.count {
            val p = it.prioridad?.lowercase() ?: ""
            p == "critica" || p == "crítica"
        }
        val criticalPct = if (total > 0) (critical.toFloat() / total * 100f) else 0f

        val resolved = filtrados.count { it.estado.equals("Solucionado", ignoreCase = true) }
        val resolvedPct = if (total > 0) (resolved.toFloat() / total * 100f) else 0f

        val pending = filtrados.count { it.estado.equals("Pendiente", ignoreCase = true) }
        val pendingPct = if (total > 0) (pending.toFloat() / total * 100f) else 0f

        val inProcess = filtrados.count { it.estado.equals("En proceso", ignoreCase = true) }
        val inProcessPct = if (total > 0) (inProcess.toFloat() / total * 100f) else 0f

        // B. Incidencias por tipo de equipo (agrupación inteligente por tipo)
        val tipoEquipoList = agruparPorTipoEquipo(filtrados, total)

        // C. Distribución por estado (para gráfico de dona)
        val estadosList = listOf(
            EstadoCount("Pendiente", pending, pendingPct),
            EstadoCount("En proceso", inProcess, inProcessPct),
            EstadoCount("Solucionado", resolved, resolvedPct)
        )

        // D. Equipos con más incidencias (Top equipos)
        val equiposGrouped = filtrados
            .groupingBy { it.equipo?.trim().takeUnless { n -> n.isNullOrEmpty() } ?: "Sin especificar" }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(6)

        val maxEquipoCount = equiposGrouped.firstOrNull()?.value?.toFloat() ?: 1f
        val equiposList = equiposGrouped.map { (nombre, count) ->
            EquipoCount(
                nombre = nombre,
                count = count,
                frecuenciaRelativa = if (maxEquipoCount > 0) (count / maxEquipoCount) else 0f
            )
        }

        // E. Incidencias por prioridad
        val baja = filtrados.count { it.prioridad.equals("Baja", ignoreCase = true) }
        val media = filtrados.count { it.prioridad.equals("Media", ignoreCase = true) }
        val alta = filtrados.count { it.prioridad.equals("Alta", ignoreCase = true) }

        val prioridadesList = listOf(
            PrioridadCount("Crítica", critical, criticalPct),
            PrioridadCount("Alta", alta, if (total > 0) (alta.toFloat() / total * 100f) else 0f),
            PrioridadCount("Media", media, if (total > 0) (media.toFloat() / total * 100f) else 0f),
            PrioridadCount("Baja", baja, if (total > 0) (baja.toFloat() / total * 100f) else 0f)
        )

        _uiState.update {
            it.copy(
                totalTickets = total,
                criticalTickets = critical,
                criticalPercentage = criticalPct,
                resolvedTickets = resolved,
                resolvedPercentage = resolvedPct,
                pendingTickets = pending,
                pendingPercentage = pendingPct,
                inProcessTickets = inProcess,
                inProcessPercentage = inProcessPct,
                incidenciasPorTipoEquipo = tipoEquipoList,
                distribucionPorEstado = estadosList,
                equiposConMasIncidencias = equiposList,
                incidenciasPorPrioridad = prioridadesList,
                errorMessage = null
            )
        }
    }

    private fun agruparPorTipoEquipo(tickets: List<ReportTicketItem>, total: Int): List<TipoEquipoCount> {
        val conteo = mutableMapOf<String, Int>()

        tickets.forEach { ticket ->
            val eq = ticket.equipo?.trim().orEmpty()
            val tipo = clasificarTipoEquipo(eq)
            conteo[tipo] = conteo.getOrDefault(tipo, 0) + 1
        }

        return conteo.entries
            .sortedByDescending { it.value }
            .take(6)
            .map { (tipo, count) ->
                TipoEquipoCount(
                    tipo = tipo,
                    count = count,
                    porcentaje = if (total > 0) (count.toFloat() / total * 100f) else 0f
                )
            }
    }

    private fun clasificarTipoEquipo(nombre: String): String {
        val lower = nombre.lowercase()
        return when {
            lower.contains("pc") || lower.contains("computadora") || lower.contains("escritorio") -> "PC"
            lower.contains("laptop") || lower.contains("portátil") || lower.contains("notebook") -> "Laptop"
            lower.contains("impresora") || lower.contains("scanner") || lower.contains("térmica") -> "Impresora"
            lower.contains("monitor") || lower.contains("pantalla") -> "Monitor"
            lower.contains("teclado") -> "Teclado"
            lower.contains("mouse") || lower.contains("ratón") -> "Mouse"
            lower.contains("pos") || lower.contains("terminal") || lower.contains("lector") -> "Terminal POS"
            lower.contains("red") || lower.contains("router") || lower.contains("switch") -> "Red"
            nombre.isNotBlank() -> nombre.take(18)
            else -> "Otros"
        }
    }

    private fun cumpleFiltroPeriodo(createdAtStr: String?, periodo: PeriodoFiltro): Boolean {
        if (periodo == PeriodoFiltro.TODO) return true
        if (createdAtStr.isNullOrBlank()) return false

        return try {
            val dateTime = OffsetDateTime.parse(createdAtStr)
            val now = OffsetDateTime.now()
            when (periodo) {
                PeriodoFiltro.HOY -> {
                    dateTime.toLocalDate() == LocalDate.now()
                }
                PeriodoFiltro.ULTIMOS_7_DIAS -> {
                    dateTime.isAfter(now.minusDays(7))
                }
                PeriodoFiltro.ULTIMOS_30_DIAS -> {
                    dateTime.isAfter(now.minusDays(30))
                }
                PeriodoFiltro.TODO -> true
            }
        } catch (e: Exception) {
            true
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
