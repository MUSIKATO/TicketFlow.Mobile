package com.devcore.ticketflow.ui.screens.admin

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.R
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class AdminEquipoItem(
    val nombre: String,
    val totalTicketsAsociados: Int = 0,
    val ticketsPendientes: Int = 0,
    val esPersonalizado: Boolean = false
)

data class AdminTeamsUiState(
    val equipos: List<AdminEquipoItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class AdminTeamsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminTeamsUiState())
    val uiState: StateFlow<AdminTeamsUiState> = _uiState.asStateFlow()

    fun cargarEquipos(context: Context) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            cargarEquiposInternal(context)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refrescar(context: Context) {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            cargarEquiposInternal(context)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private suspend fun cargarEquiposInternal(context: Context) {
        try {
            // 1. Obtener catálogo base del sistema (definido en recursos)
            val opcionesCatalogo = context.resources.getStringArray(R.array.opciones_equipos)
                .filter { it != "Otro" && it != context.getString(R.string.opcion_equipo_otro) }

            // 2. Consultar equipos que realmente tienen incidencias en Supabase
            val ticketsData = withContext(Dispatchers.IO) {
                SupabaseClient.client.postgrest.from("tickets")
                    .select {
                        // Traer columna equipo y estado para estadísticas por hardware
                    }.data
            }

            val ticketsJson = Json.parseToJsonElement(ticketsData).jsonArray

            val conteoPorEquipo = mutableMapOf<String, Pair<Int, Int>>() // total to pendientes

            ticketsJson.forEach { elem ->
                val obj = elem.jsonObject
                val eq = obj["equipo"]?.jsonPrimitive?.content.orEmpty().trim()
                val est = obj["estado"]?.jsonPrimitive?.content.orEmpty().trim()

                if (eq.isNotEmpty()) {
                    val current = conteoPorEquipo.getOrDefault(eq, Pair(0, 0))
                    val isPending = est.equals("Pendiente", ignoreCase = true)
                    conteoPorEquipo[eq] = Pair(
                        current.first + 1,
                        if (isPending) current.second + 1 else current.second
                    )
                }
            }

            // 3. Fusionar catálogo base y dispositivos adicionales registrados
            val lista = mutableListOf<AdminEquipoItem>()

            opcionesCatalogo.forEach { nombre ->
                val stats = conteoPorEquipo[nombre] ?: Pair(0, 0)
                lista.add(
                    AdminEquipoItem(
                        nombre = nombre,
                        totalTicketsAsociados = stats.first,
                        ticketsPendientes = stats.second,
                        esPersonalizado = false
                    )
                )
            }

            conteoPorEquipo.forEach { (nombre, stats) ->
                if (lista.none { it.nombre.equals(nombre, ignoreCase = true) }) {
                    lista.add(
                        AdminEquipoItem(
                            nombre = nombre,
                            totalTicketsAsociados = stats.first,
                            ticketsPendientes = stats.second,
                            esPersonalizado = true
                        )
                    )
                }
            }

            _uiState.update { it.copy(equipos = lista, errorMessage = null) }
        } catch (e: Exception) {
            Log.e("AdminTeamsViewModel", "Error al cargar equipos de inventario", e)
            _uiState.update {
                it.copy(errorMessage = "No se pudieron calcular las métricas de equipos. Verifica la conexión.")
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
