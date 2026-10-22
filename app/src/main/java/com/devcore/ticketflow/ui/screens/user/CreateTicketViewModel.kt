package com.devcore.ticketflow.ui.screens.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.io.File

data class CreateTicketUiState(
    val equipo: String = "",
    val equipoOtro: String = "",
    val mostrarEquipoOtro: Boolean = false,
    val tipoIncidencia: String = "",
    val descripcion: String = "",
    val prioridad: String? = null, // "Baja", "Media", "Alta", "Critica"
    val fotoFile: File? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val equipoError: String? = null,
    val tipoIncidenciaError: String? = null,
    val descripcionError: String? = null,
    val prioridadError: String? = null
)

sealed interface CreateTicketEvent {
    data class TicketCreated(val ticketId: Long) : CreateTicketEvent
}

class CreateTicketViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTicketUiState())
    val uiState: StateFlow<CreateTicketUiState> = _uiState.asStateFlow()

    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<CreateTicketEvent>()
    val events = _events

    fun onEquipoChange(equipo: String) {
        _uiState.update {
            it.copy(
                equipo = equipo,
                mostrarEquipoOtro = equipo == "Otro",
                equipoError = null
            )
        }
    }

    fun onEquipoOtroChange(text: String) {
        _uiState.update { it.copy(equipoOtro = text, equipoError = null) }
    }

    fun onTipoIncidenciaChange(text: String) {
        _uiState.update { it.copy(tipoIncidencia = text.take(60), tipoIncidenciaError = null) }
    }

    fun onDescripcionChange(text: String) {
        _uiState.update { it.copy(descripcion = text.take(500), descripcionError = null) }
    }

    fun onPrioridadChange(prioridad: String) {
        _uiState.update { it.copy(prioridad = prioridad, prioridadError = null) }
    }

    fun onFotoSeleccionada(file: File?) {
        _uiState.update { it.copy(fotoFile = file) }
    }

    fun onEliminarFoto() {
        _uiState.update { it.copy(fotoFile = null) }
    }

    fun onError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun obtenerUserId(): String? {
        return SupabaseClient.perfil?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id
    }

    fun crearTicket() {
        val state = _uiState.value
        val userId = obtenerUserId()

        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "Sesión no disponible, vuelve a iniciar sesión") }
            return
        }

        val equipoError = when {
            state.equipo.isBlank() -> "Selecciona un equipo"
            state.mostrarEquipoOtro && state.equipoOtro.isBlank() -> "Escribe el equipo afectado"
            else -> null
        }
        val tipoError = when {
            state.tipoIncidencia.isBlank() -> "Indica el tipo de incidencia"
            state.tipoIncidencia.trim().length < 3 -> "Escribe al menos 3 caracteres"
            else -> null
        }
        val descripcionError = when {
            state.descripcion.isBlank() -> "Describe el problema"
            state.descripcion.trim().length < 10 -> "Escribe al menos 10 caracteres"
            else -> null
        }
        val prioridadError = if (state.prioridad == null) "Selecciona una prioridad" else null

        if (equipoError != null || tipoError != null ||
            descripcionError != null || prioridadError != null
        ) {
            _uiState.update {
                it.copy(
                    equipoError = equipoError,
                    tipoIncidenciaError = tipoError,
                    descripcionError = descripcionError,
                    prioridadError = prioridadError,
                    errorMessage = "Revisa los campos marcados en rojo"
                )
            }
            return
        }

        val equipoFinal = if (state.mostrarEquipoOtro) state.equipoOtro.trim() else state.equipo.trim()

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val evidenciaUrl = state.fotoFile?.let { file ->
                    withContext(Dispatchers.IO) {
                        val path = "evidencia/${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("foto_ticket")
                        bucket.upload(path, file) { upsert = true }
                        bucket.publicUrl(path).toString()
                    }
                }

                val payload = buildJsonObject {
                    put("equipo", JsonPrimitive(equipoFinal))
                    put("tipo_incidencia", JsonPrimitive(state.tipoIncidencia.trim()))
                    put("descripcion", JsonPrimitive(state.descripcion.trim()))
                    put("prioridad", JsonPrimitive(state.prioridad!!))
                    put("profile", JsonPrimitive(userId))
                    evidenciaUrl?.let { put("evidencia_url", JsonPrimitive(it)) }
                }

                Log.d("CreateTicketVM", "Enviando ticket: $payload")

                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest.from("tickets")
                        .insert(JsonArray(listOf(payload))) { select() }
                }

                val nuevoId = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = true }
                    .parseToJsonElement(result.data).jsonArray
                    .firstOrNull()?.let { it as kotlinx.serialization.json.JsonObject }
                    ?.get("id")?.let { it as kotlinx.serialization.json.JsonPrimitive }
                    ?.longOrNull

                if (nuevoId != null) {
                    _events.emit(CreateTicketEvent.TicketCreated(nuevoId))
                    _uiState.update {
                        CreateTicketUiState() // Reset form
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo obtener el ID del ticket creado"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateTicketVM", "Fallo al crear el ticket", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo crear el ticket. Inténtalo de nuevo."
                    )
                }
            }
        }
    }
}