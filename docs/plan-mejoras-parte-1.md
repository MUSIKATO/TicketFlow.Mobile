# PLAN DE MEJORAS PARTE 1 — IMPLEMENTACIÓN CONTROLADA
## TicketFlow Mobile — Protección de credenciales, limpieza y arquitectura

**Fecha:** Noviembre 2026  
**Estado:** Plan propuesto, pendiente de autorización para ejecución  
**Rama propuesta:** `feature/parte-1-arquitectura`  
**Prioridad:** Mejoras seguras que no rompen funcionalidad existente

---

## INTRODUCCIÓN

Este plan detalla las mejoras propuestas tras la auditoría de arquitectura y configuración de Supabase. Todas las mejoras están diseñadas para:
- Proteger credenciales de Supabase
- Limpiar código legacy no utilizado
- Mejorar arquitectura con capa de repositorio
- Optimizar persistencia de perfil

**Principio:** NO romper funcionalidad existente. Todas las mejoras son backwards compatible.

---

## MEJORA 1: PROTEGER CREDENCIALES DE SUPABASE

### Problema Actual

**Archivo:** `app/src/main/java/com/devcore/ticketflow/SupabaseClient.kt` (líneas 14-15)

```kotlin
supabaseUrl = "https://rsqayszvxrcgvylquzog.supabase.co",
supabaseKey = "sb_publishable_cCCulG6WfHEr-BRaG36bBQ_3rTbTL3Z"
```

**Riesgo:** La clave publishable está visible en código fuente. Aunque no es service_role, no debe exponerse en repositorios públicos.

### Solución Propuesta

#### Paso 1: Agregar credenciales a local.properties

**Archivo:** `local.properties` (ya existe, contiene solo sdk.dir)

**Agregar:**
```properties
supabase.url=https://rsqayszvxrcgvylquzog.supabase.co
supabase.key=sb_publishable_cCCulG6WfHEr-BRaG36bBQ_3rTbTL3Z
```

**Estado actual de .gitignore:** ✅ `local.properties` ya está en .gitignore (líneas 3 y 15)

#### Paso 2: Configurar build.gradle.kts para leer de local.properties

**Archivo:** `app/build.gradle.kts`

**Agregar en bloque android (después de línea 36):**
```kotlin
android {
    // ... configuración existente ...
    
    // Leer credenciales de local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }
    
    defaultConfig {
        // ... configuración existente ...
        
        // Definir BuildConfig fields
        buildConfigField("String", "SUPABASE_URL", "\"${localProperties["supabase.url"]}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProperties["supabase.key"]}\"")
    }
    
    buildFeatures {
        compose = true
        buildConfig = true  // Agregar esta línea
    }
}
```

**Nota:** Es necesario agregar `buildConfig = true` para que BuildConfig genere los campos.

#### Paso 3: Modificar SupabaseClient.kt para usar BuildConfig

**Archivo:** `app/src/main/java/com/devcore/ticketflow/SupabaseClient.kt`

**Reemplazar líneas 12-16:**
```kotlin
val client = try {
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
} catch (e: Exception) {
    Log.e("SupabaseClient", "Error al inicializar Supabase client", e)
    throw e
}
```

**Agregar import:**
```kotlin
import com.devcore.ticketflow.BuildConfig
```

### Archivos a Modificar

1. `local.properties` - Agregar credenciales
2. `app/build.gradle.kts` - Configurar lectura de local.properties y BuildConfig
3. `app/src/main/java/com/devcore/ticketflow/SupabaseClient.kt` - Usar BuildConfig

### Riesgos

- **Riesgo:** BAJO
- **Impacto:** Solo cambia de dónde se leen las credenciales
- **Backwards compatible:** Sí
- **Si local.properties no tiene credenciales:** BuildConfig estará vacío y la app fallará al iniciar. Mitigación: documentar requerimiento en README

### Pruebas Necesarias

1. Compilar proyecto: `./gradlew clean assembleDebug`
2. Verificar que BuildConfig.SUPABASE_URL y BuildConfig.SUPABASE_KEY se generan correctamente
3. Ejecutar app en emulador
4. Verificar que login funcione correctamente
5. Verificar que registro funcione correctamente
6. Verificar que operaciones de tickets funcionen

---

## MEJORA 2: ELIMINAR CÓDIGO XML LEGACY

### Problema Actual

El proyecto contiene Activities y layouts XML heredados de una implementación anterior que NO se utilizan en el flujo principal de Compose.

**Activities legacy:**
- AdminActivity.kt
- UserActivity.kt
- UserDashboardActivity.kt
- UserTicketsActivity.kt
- UserProfileActivity.kt
- UserEditProfileActivity.kt
- UserCreateTicketActivity.kt
- UserTicketDetailActivity.kt

**Layouts XML legacy:**
- activity_admin.xml
- activity_main.xml
- activity_user.xml
- activity_user_create_ticket.xml
- activity_user_dashboard.xml
- activity_user_edit_profile.xml
- activity_user_profile.xml
- activity_user_ticket_detail.xml
- activity_user_tickets.xml

**UiUtils.kt:** Contiene utilidades para XML (LayoutInsets, setupUserBottomNav, funciones de badges para TextView)

### Verificación de No Uso

**Flujo principal:**
- MainActivity.kt (línea 18-22): Usa `setContent { TicketFlowTheme { AppNavigation() } }`
- AppNavigation.kt: Usa exclusivamente composables Compose (LoginScreen, RegisterScreen, UserDashboardScreen, etc.)
- NO hay llamadas a `startActivity` con Activities legacy en AppNavigation.kt

**Import de AdminActivity en AppNavigation.kt (línea 14):**
- Está importado pero NO se usa en ninguna parte del código
- Puede eliminarse el import

**Conclusión:** El código XML legacy es código muerto que puede eliminarse sin afectar funcionalidad.

### Solución Propuesta

#### Paso 1: Eliminar Activities legacy

**Archivos a eliminar:**
```
app/src/main/java/com/devcore/ticketflow/AdminActivity.kt
app/src/main/java/com/devcore/ticketflow/UserActivity.kt
app/src/main/java/com/devcore/ticketflow/UserDashboardActivity.kt
app/src/main/java/com/devcore/ticketflow/UserTicketsActivity.kt
app/src/main/java/com/devcore/ticketflow/UserProfileActivity.kt
app/src/main/java/com/devcore/ticketflow/UserEditProfileActivity.kt
app/src/main/java/com/devcore/ticketflow/UserCreateTicketActivity.kt
app/src/main/java/com/devcore/ticketflow/UserTicketDetailActivity.kt
```

#### Paso 2: Eliminar layouts XML legacy

**Archivos a eliminar:**
```
app/src/main/res/layout/activity_admin.xml
app/src/main/res/layout/activity_main.xml
app/src/main/res/layout/activity_user.xml
app/src/main/res/layout/activity_user_create_ticket.xml
app/src/main/res/layout/activity_user_dashboard.xml
app/src/main/res/layout/activity_user_edit_profile.xml
app/src/main/res/layout/activity_user_profile.xml
app/src/main/res/layout/activity_user_ticket_detail.xml
app/src/main/res/layout/activity_user_tickets.xml
```

#### Paso 3: Eliminar import no usado en AppNavigation.kt

**Archivo:** `app/src/main/java/com/devcore/ticketflow/ui/navigation/AppNavigation.kt`

**Eliminar línea 14:**
```kotlin
import com.devcore.ticketflow.AdminActivity
```

#### Paso 4: Evaluar UiUtils.kt

**Archivo:** `app/src/main/java/com/devcore/ticketflow/UiUtils.kt`

**Contenido:**
- LayoutInsets (funciones para insets de sistema en XML)
- setupUserBottomNav (configuración de navegación XML)
- aplicarBadge, prioridadBadge, estadoBadge (badges para TextView)

**Uso actual:** Los badges en Compose están en `ui/components/Badges.kt`. UiUtils.kt NO se usa en Compose.

**Decisión:** ELIMINAR UiUtils.kt también (código muerto).

### Archivos a Modificar/Eliminar

**Eliminar (16 archivos):**
- 8 Activities legacy (.kt)
- 9 layouts XML (.xml)
- 1 utilidad legacy (UiUtils.kt)

**Modificar (1 archivo):**
- AppNavigation.kt - Eliminar import no usado

### Riesgos

- **Riesgo:** BAJO
- **Impacto:** Limpieza de código muerto
- **Backwards compatible:** Sí (el código no se usa)
- **Verificación:** Buscar referencias a Activities legacy en grep antes de eliminar

### Pruebas Necesarias

1. Buscar referencias a Activities legacy en todo el proyecto
2. Eliminar archivos
3. Compilar proyecto: `./gradlew clean assembleDebug`
4. Ejecutar app en emulador
5. Verificar que navegación funcione correctamente
6. Verificar que login funcione
7. Verificar que tickets funcionen

---

## MEJORA 3: DOCUMENTAR POLÍTICAS RLS

### Problema Actual

Las políticas Row Level Security (RLS) de Supabase NO están documentadas en el código. Solo se pueden verificar accediendo al panel de Supabase Console.

**Inferencias del código:**
- **Usuario estándar:** Solo puede ver sus propios tickets (filtro por `profile` en UserTicketsViewModel.kt)
- **Administrador:** Puede ver todos los tickets (sin filtro en AdminTicketsViewModel.kt)
- **Perfiles:** Los usuarios solo pueden editar su propio perfil

### Información que se puede verificar desde el repositorio

**NO verificable desde código:**
- Políticas RLS reales configuradas en Supabase Console
- Reglas de seguridad en tablas profiles y tickets
- Permisos de SELECT, INSERT, UPDATE, DELETE por rol

**Verificable desde código:**
- Cómo los ViewModels filtran por usuario (líneas de código específicas)
- Lógica de autorización por rol en AppNavigation.kt
- Verificación de sesión en LoginViewModel.kt

### Solución Propuesta

#### Paso 1: Acceder a Supabase Console

**Requiere:** Acceso al panel de Supabase Console del proyecto `rsqayszvxrcgvylquzog`

**Acciones:**
1. Navegar a Authentication → Policies
2. Navegar a Database → Tables → profiles → Policies
3. Navegar a Database → Tables → tickets → Policies
4. Documentar cada política:
   - Nombre
   - Tipo (SELECT, INSERT, UPDATE, DELETE)
   - Condiciones (USING, WITH CHECK)
   - Roles afectados

#### Paso 2: Agregar documentación al proyecto

**Archivo nuevo:** `docs/rls-policies.md`

**Contenido:**
- Lista de políticas RLS configuradas
- Diagrama de permisos por rol
- Cómo se relacionan con el código de los ViewModels
- Verificación de que las políticas protegen correctamente los datos

### Archivos a Crear

1. `docs/rls-policies.md` - Documentación de políticas RLS

### Riesgos

- **Riesgo:** NINGUNO (solo documentación)
- **Impacto:** Ninguno
- **Backwards compatible:** Sí

### Pruebas Necesarias

1. Revisar documentación de RLS en Supabase Console
2. Verificar que las políticas documentadas coinciden con el código
3. Identificar posibles vulnerabilidades

---

## MEJORA 4: CREAR CAPA DE REPOSITORY

### Problema Actual

Los ViewModels acceden directamente a SupabaseClient, lo que:
- Acopla los ViewModels a Supabase
- Dificulta migrar a otro backend en el futuro
- Duplica lógica de consultas en múltiples ViewModels
- Dificulta testing unitario sin mockear SupabaseClient

### Solución Propuesta

#### Arquitectura Propuesta

```
UI (Screens Compose)
  → ViewModels (StateFlow)
  → Repository Interface
  → SupabaseRepository (implementación)
  → SupabaseClient
  → Supabase (Auth, PostgREST, Storage)
```

#### Paso 1: Crear interfaces de repositorio

**Archivo nuevo:** `app/src/main/java/com/devcore/ticketflow/data/repository/AuthRepository.kt`

```kotlin
package com.devcore.ticketflow.data.repository

import com.devcore.ticketflow.Profile

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Profile>
    suspend fun signUp(email: String, password: String, nombre: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<Profile?>
    suspend fun getCurrentSession(): Result<Boolean>
}
```

**Archivo nuevo:** `app/src/main/java/com/devcore/ticketflow/data/repository/TicketRepository.kt`

```kotlin
package com.devcore.ticketflow.data.repository

import com.devcore.ticketflow.ui.screens.user.TicketItem
import com.devcore.ticketflow.ui.screens.user.CreateTicketUiState

interface TicketRepository {
    suspend fun getTickets(userId: String, statusFilter: String?): Result<List<TicketItem>>
    suspend fun getTicket(ticketId: Long, userId: String): Result<TicketItem>
    suspend fun createTicket(userId: String, ticketData: CreateTicketUiState): Result<Long>
    suspend fun updateTicketStatus(ticketId: Long, newStatus: String): Result<Unit>
    suspend fun updateTicketPriority(ticketId: Long, newPriority: String): Result<Unit>
}
```

**Archivo nuevo:** `app/src/main/java/com/devcore/ticketflow/data/repository/ProfileRepository.kt`

```kotlin
package com.devcore.ticketflow.data.repository

import com.devcore.ticketflow.Profile

interface ProfileRepository {
    suspend fun getProfile(userId: String): Result<Profile>
    suspend fun updateProfile(userId: String, nombre: String, correo: String): Result<Unit>
    suspend fun getAllUsers(): Result<List<Profile>>
}
```

#### Paso 2: Crear implementaciones Supabase

**Archivo nuevo:** `app/src/main/java/com/devcore/ticketflow/data/repository/SupabaseAuthRepository.kt`

```kotlin
package com.devcore.ticketflow.data.repository

import com.devcore.ticketflow.Profile
import com.devcore.ticketflow.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<Profile> {
        return try {
            withContext(Dispatchers.IO) {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                val usuario = SupabaseClient.client.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Usuario no encontrado"))
                
                val perfil = SupabaseClient.client.from("profiles").select {
                    filter { eq("id", usuario.id) }
                }.decodeSingle<Profile>()
                
                SupabaseClient.perfil = perfil
                Result.success(perfil)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ... implementar otros métodos
}
```

#### Paso 3: Inyectar repositorios en ViewModels

**Modificar ViewModels para recibir repositorios en constructor**

**Ejemplo LoginViewModel.kt:**
```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository = SupabaseAuthRepository()
) : ViewModel() {
    // ... usar authRepository en lugar de SupabaseClient directamente
}
```

### Archivos a Crear

1. `data/repository/AuthRepository.kt` - Interface
2. `data/repository/TicketRepository.kt` - Interface
3. `data/repository/ProfileRepository.kt` - Interface
4. `data/repository/SupabaseAuthRepository.kt` - Implementación
5. `data/repository/SupabaseTicketRepository.kt` - Implementación
6. `data/repository/SupabaseProfileRepository.kt` - Implementación

### Archivos a Modificar

1. Todos los ViewModels (11 ViewModels) - Inyectar repositorios
2. ViewModels que usan SupabaseClient directamente - Cambiar a usar repositorios

### Riesgos

- **Riesgo:** MEDIO
- **Impacto:** Refactorización significativa de la capa de acceso a datos
- **Backwards compatible:** Sí (funcionalidad idéntica, solo cambia la capa)
- **Complejidad:** Alta - requiere modificar todos los ViewModels

### Pruebas Necesarias

1. Crear interfaces e implementaciones
2. Modificar un ViewModel primero (ej: LoginViewModel)
3. Probar que login funcione
4. Modificar ViewModels gradualmente
5. Probar cada módulo después de modificación
6. Ejecutar pruebas unitarias si existen
7. Pruebas de integración manuales

---

## MEJORA 5: DATASTORE PARA PERSISTIR PERFIL

### Problema Actual

**Archivo:** `SupabaseClient.kt` (línea 28)

```kotlin
var perfil: Profile? = null
```

**Problema:** El perfil se almacena solo en memoria. Se pierde al cerrar la aplicación. Cada vez que se abre la app, se debe consultar el perfil nuevamente.

### ¿Es DataStore Necesario?

**Análisis:**
- El perfil se consulta al iniciar sesión (LoginViewModel.obtenerPerfilYRedirigir)
- La sesión de Supabase Auth persiste automáticamente
- El perfil NO cambia frecuentemente
- Consultar el perfil cada vez que se abre la app es rápido (una sola query)

**Conclusión:** DataStore NO es crítico para la funcionalidad actual. Es una mejora de UX (evitar query al perfil al abrir app).

### Solución Propuesta (Opcional - Prioridad BAJA)

#### Paso 1: Agregar dependencia DataStore

**Archivo:** `app/build.gradle.kts`

```kotlin
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

#### Paso 2: Crear DataManager

**Archivo nuevo:** `app/src/main/java/com/devcore/ticketflow/data/manager/DataManager.kt`

```kotlin
package com.devcore.ticketflow.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devcore.ticketflow.Profile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

class DataManager(private val context: Context) {
    private val PROFILE_KEY = stringPreferencesKey("profile")
    
    suspend fun saveProfile(profile: Profile) {
        context.dataStore.edit { preferences ->
            preferences[PROFILE_KEY] = Json.encodeToString(profile)
        }
    }
    
    suspend fun getProfile(): Profile? {
        val jsonString = context.dataStore.data.map { preferences ->
            preferences[PROFILE_KEY]
        }.firstOrNull()
        
        return jsonString?.let { Json.decodeFromString<Profile>(it) }
    }
    
    suspend fun clearProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(PROFILE_KEY)
        }
    }
}
```

#### Paso 3: Integrar en LoginViewModel

**Modificar LoginViewModel.kt:**
- Guardar perfil en DataStore después de obtenerlo
- Recuperar perfil de DataStore al comprobar sesión

### Archivos a Crear

1. `data/manager/DataManager.kt` - Gestión de DataStore

### Archivos a Modificar

1. `app/build.gradle.kts` - Agregar dependencia DataStore
2. `LoginViewModel.kt` - Integrar DataStore
3. `SupabaseClient.kt` - Mantener var perfil para compatibilidad, pero usar DataStore

### Riesgos

- **Riesgo:** BAJO
- **Impacto:** Mejora de UX, no crítico
- **Backwards compatible:** Sí
- **Complejidad:** Baja

### Pruebas Necesarias

1. Agregar dependencia DataStore
2. Crear DataManager
3. Integrar en LoginViewModel
4. Probar que perfil se persiste entre cierres de app
5. Probar que perfil se actualiza al modificar datos

---

## CRONOGRAMA DE IMPLEMENTACIÓN

### Fase 1: Mejoras Seguras (Críticas)

**Duración estimada:** 30 minutos

1. Mejora 1: Proteger credenciales con local.properties
   - Modificar local.properties
   - Modificar build.gradle.kts
   - Modificar SupabaseClient.kt
   - Probar compilación y funcionalidad

2. Mejora 2: Eliminar código XML legacy
   - Eliminar Activities legacy
   - Eliminar layouts XML
   - Eliminar UiUtils.kt
   - Eliminar import no usado en AppNavigation.kt
   - Probar compilación y funcionalidad

### Fase 2: Documentación (Crítica)

**Duración estimada:** 20 minutos

3. Mejora 3: Documentar políticas RLS
   - Acceder a Supabase Console
   - Documentar políticas
   - Crear docs/rls-policies.md

### Fase 3: Mejoras Arquitectura (Opcionales)

**Duración estimada:** 2-3 horas

4. Mejora 4: Crear capa de repository
   - Crear interfaces
   - Crear implementaciones
   - Modificar ViewModels gradualmente
   - Probar cada módulo

5. Mejora 5: DataStore para perfil (opcional)
   - Agregar dependencia
   - Crear DataManager
   - Integrar en LoginViewModel
   - Probar persistencia

---

## VERIFICACIÓN ANTES DE EJECUTAR

### Pre-requisitos

1. ✅ Comprobar estado de Git
2. ✅ Crear rama `feature/parte-1-arquitectura`
3. ✅ Documentar cambios planificados
4. ✅ Verificar que local.properties existe
5. ✅ Backup del proyecto actual

### Verificación de Mejora 1 (Credenciales)

- [ ] local.properties tiene credenciales
- [ ] build.gradle.kts lee local.properties
- [ ] BuildConfig se genera correctamente
- [ ] SupabaseClient usa BuildConfig
- [ ] App compila
- [ ] Login funciona
- [ ] Registro funciona

### Verificación de Mejora 2 (XML Legacy)

- [ ] No hay referencias a Activities legacy
- [ ] No hay referencias a layouts XML legacy
- [ ] App compila
- [ ] Navegación funciona
- [ ] Todos los módulos funcionan

### Verificación de Mejora 3 (RLS)

- [ ] Acceso a Supabase Console
- [ ] Políticas documentadas
- [ ] Documentación creada

### Verificación de Mejora 4 (Repository)

- [ ] Interfaces creadas
- [ ] Implementaciones creadas
- [ ] LoginViewModel migrado
- [ ] Login funciona
- [ ] Otros ViewModels migrados
- [ ] Todos los módulos funcionan

### Verificación de Mejora 5 (DataStore)

- [ ] Dependencia agregada
- [ ] DataManager creado
- [ ] LoginViewModel integrado
- [ ] Perfil persiste entre cierres
- [ ] Perfil se actualiza correctamente

---

## CONCLUSIÓN

Este plan propone mejoras graduales y backwards compatible para TicketFlow Mobile:

**Fase 1 (Prioridad ALTA - Seguridad y Limpieza):**
- Proteger credenciales con local.properties
- Eliminar código XML legacy

**Fase 2 (Prioridad ALTA - Documentación):**
- Documentar políticas RLS de Supabase

**Fase 3 (Prioridad MEDIA - Arquitectura):**
- Crear capa de repository (requiere refactorización significativa)
- DataStore para perfil (opcional, mejora de UX)

**NO modificar:** Firebase, Supabase Auth, PostgreSQL, Storage, RLS configuradas, datos existentes.

**Principio:** Mejoras graduales, probadas en cada paso, sin romper funcionalidad existente.

---

**PLAN COMPLETADO - PENDIENTE DE AUTORIZACIÓN PARA EJECUCIÓN**
