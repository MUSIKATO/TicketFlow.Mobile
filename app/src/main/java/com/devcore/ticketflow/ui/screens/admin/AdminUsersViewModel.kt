package com.devcore.ticketflow.ui.screens.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devcore.ticketflow.Profile
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
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

data class AdminUsersUiState(
    val users: List<Profile> = emptyList(),
    val searchQuery: String = "",
    val selectedRoleFilter: String? = null, // null = Todos, "admin", "usuario"
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class AdminUsersViewModel : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onRoleFilterSelected(role: String?) {
        if (_uiState.value.selectedRoleFilter == role) return
        _uiState.update { it.copy(selectedRoleFilter = role) }
        cargarUsuarios()
    }

    fun refrescar() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            cargarUsuariosInternal()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun cargarUsuarios() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            cargarUsuariosInternal()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun cargarUsuariosInternal() {
        try {
            val roleFilter = _uiState.value.selectedRoleFilter
            Log.d("AdminUsersViewModel", "Cargando usuarios con filtro de rol: $roleFilter")

            val lista = withContext(Dispatchers.IO) {
                Log.d("AdminUsersViewModel", "Consultando tabla profiles...")
                Log.d("AdminUsersViewModel", "Sesión activa: ${SupabaseClient.client.auth.currentUserOrNull() != null}")
                Log.d("AdminUsersViewModel", "Perfil cargado: ${SupabaseClient.perfil?.nombre}")
                
                val data = SupabaseClient.client.postgrest.from("profiles").select {
                    filter {
                        if (roleFilter != null) {
                            eq("rol", roleFilter)
                        }
                    }
                    order(column = "nombre", order = Order.ASCENDING)
                }.data

                Log.d("AdminUsersViewModel", "Datos recibidos de Supabase: ${data.take(200)}...")
                
                if (data == "null" || data.isEmpty()) {
                    Log.d("AdminUsersViewModel", "Respuesta vacía de Supabase - posible denegación RLS o tabla vacía")
                    emptyList<Profile>()
                } else {
                    val decoded = json.decodeFromString<List<Profile>>(data)
                    Log.d("AdminUsersViewModel", "Usuarios decodados: ${decoded.size} registros")
                    decoded
                }
            }

            _uiState.update { it.copy(users = lista, errorMessage = null) }
            Log.d("AdminUsersViewModel", "Carga exitosa: ${lista.size} usuarios")
            
            if (lista.isEmpty()) {
                Log.w("AdminUsersViewModel", "Lista vacía - verificar RLS y existencia de perfiles")
            }
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e("AdminUsersViewModel", "Error de deserialización", e)
            _uiState.update {
                it.copy(errorMessage = "Error al decodificar datos. El esquema de la tabla no coincide con el modelo Profile. Error: ${e.message}")
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e("AdminUsersViewModel", "Error de red - sin conexión", e)
            _uiState.update {
                it.copy(errorMessage = "Sin conexión a internet. Verifica tu conexión.")
            }
        } catch (e: Exception) {
            Log.e("AdminUsersViewModel", "Error al cargar profiles de Supabase", e)
            _uiState.update {
                it.copy(errorMessage = "No se pudieron obtener los usuarios registrados. Error: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
