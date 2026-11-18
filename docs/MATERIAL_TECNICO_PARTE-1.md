# MATERIAL TÉCNICO — PARTE 1: ARQUITECTURA, CONFIGURACIÓN E INTEGRACIÓN DE SUPABASE
## TicketFlow Mobile — Documento académico/técnico (borrador para Word/PDF)

**Proyecto:** TicketFlow Mobile — Android nativo  
**Ruta auditada:** `TicketFlow.Mobile-main (1)\TicketFlow.Mobile-main`  
**Fecha de recopilación:** 8 de noviembre de 2026  
**Método:** inspección directa del código fuente. Todo fragmento citado fue leído del archivo indicado. Nada fue inventado ni modificado.

> **Convenciones:** las rutas son relativas a la raíz del proyecto. Los números de línea corresponden al estado del código en la fecha de auditoría. Los valores de credenciales se omiten o enmascaran (`sb_publishable_...`); el valor real está en `local.properties` (excluido de control de versiones).

---

## 3.1. ARQUITECTURA DE LA APLICACIÓN

### Descripción general

TicketFlow Mobile es una aplicación Android nativa para la gestión de tickets de soporte técnico en el contexto de Perfumería VIP. Permite a usuarios estándar reportar incidencias de equipos y servicios, y a administradores gestionarlas (estado, prioridad, edición, eliminación), con evidencias fotográficas almacenadas en Supabase Storage.

### Tecnologías utilizadas

| Capa | Tecnología | Versión / Referencia |
|------|------------|----------------------|
| Lenguaje | Kotlin | plugin Kotlin (ver `app/build.gradle.kts`) |
| UI | Jetpack Compose + Material 3 | Compose BOM `2024.12.01` |
| Arquitectura | MVVM (`ViewModel`, `StateFlow`, `SharedFlow`) | AndroidX Lifecycle |
| Navegación | Navigation Compose | `androidx.navigation.compose` |
| Backend | Supabase (Auth, PostgREST, Storage, Realtime) | BOM `3.5.0` |
| HTTP | Ktor client Android | `3.0.3` |
| Serialización | kotlinx.serialization | plugin `org.jetbrains.kotlin.plugin.serialization` |
| Imágenes | Coil (Compose) + AsyncImage | `2.6.0` |
| Cámara | `ActivityResultContracts.TakePicture` + FileProvider | AndroidX Activity |

### Arquitectura MVVM y organización de paquetes

```
app/src/main/java/com/devcore/ticketflow/
├── MainActivity.kt                 → Punto de entrada Compose + deep links OAuth
├── SupabaseClient.kt               → Singleton del cliente Supabase
├── Profile.kt                      → Modelo de la tabla profiles
├── AdminActivity.kt, UserActivity.kt, ... → Activities XML legacy (declaradas en Manifest)
├── ui/
│   ├── navigation/
│   │   ├── AppNavigation.kt        → NavHost centralizado, todas las rutas
│   │   └── NavRoutes.kt            → Constantes de rutas
│   ├── components/                 → Componentes reutilizables
│   │   ├── BottomNavigation.kt     → AdminBottomNavigation / UserBottomNavigation
│   │   ├── TicketCard.kt, Badges.kt
│   │   ├── TicketFlowButton.kt, TicketFlowTextField.kt
│   │   ├── TicketFlowLoading.kt, TicketFlowErrorBanner.kt
│   ├── screens/
│   │   ├── auth/                   → Login/Register (Screen + ViewModel)
│   │   ├── user/                   → Dashboard, Tickets, Crear, Detalle, Perfil
│   │   └── admin/                  → Dashboard, Tickets, Users, Teams, Profile, History, Reports
│   └── theme/                      → Colores y tema Material 3
└── console/                        → Utilidad de consola (task `runConsole` en Gradle)
```

**Responsabilidad de las pantallas Compose:** renderizar estado, capturar eventos del usuario y delegar toda la lógica al ViewModel. Ejemplo: `CreateTicketScreen` solo observa `viewModel.uiState` y llama a `viewModel.crearTicket()`.

**Responsabilidad de los ViewModels:** mantener el estado de UI en `MutableStateFlow`, ejecutar las operaciones contra Supabase dentro de `viewModelScope.launch` + `withContext(Dispatchers.IO)`, y emitir eventos de navegación/errores por `SharedFlow` o `StateFlow`.

**Gestión del estado:** cada pantalla tiene un `data class XxxUiState` y un `sealed interface XxxEvent`. Patrón uniforme en los 17 ViewModels del proyecto.

**Acceso a Supabase:** los ViewModels llaman directamente a `SupabaseClient.client` (`.auth`, `.postgrest`, `.storage`).

**Capa Repository: NO EXISTE.** Los ViewModels están acoplados al cliente Supabase. Esto es una decisión arquitectónica actual (documentada como deuda técnica en `docs/plan-mejoras-parte-1.md` del repositorio original) y debe mencionarse honestamente en el documento.

---

## 3.2. CONFIGURACIÓN E INICIALIZACIÓN DE SUPABASE

**Archivo:** `app/src/main/java/com/devcore/ticketflow/SupabaseClient.kt` (29 líneas, archivo completo):

```kotlin
package com.devcore.ticketflow

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = try {
        createSupabaseClient(
            supabaseUrl = "https://rsqayszvxrcgvylquzog.supabase.co",
            supabaseKey = "sb_publishable_..."
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

    // Variable global con el perfil del usuario con sesión activa (persiste por sesión en memoria)
    var perfil: Profile? = null
}
```

**Análisis por elemento:**

| Elemento | Detalle |
|----------|---------|
| Función de creación | `createSupabaseClient(supabaseUrl, supabaseKey)` del SDK Kotlin |
| `install(Auth)` | Módulo de autenticación. **Sin bloque de configuración**: no se define `scheme`/`host` en este archivo (relevante para el deep link OAuth; la URL de callback se pasa explícitamente en `signInWith`) |
| `install(Postgrest)` | Cliente REST para PostgreSQL; usado por todos los `from("tickets")` / `from("profiles")` |
| `install(Storage)` | Cliente de Supabase Storage; usado para el bucket `foto_ticket` |
| `install(Realtime)` | Módulo instalado pero **NO utilizado** en el código: no hay `channel()` ni `subscribe()` en ningún ViewModel. Las actualizaciones son manuales (pull-to-refresh) |
| `SupabaseClient.perfil` | Variable global mutable (`var`) que cachea el `Profile` del usuario autenticado. Es la fuente de identidad usada por los ViewModels junto con `auth.currentUserOrNull()` |
| Credenciales | En este archivo aparecen como literales. `build.gradle.kts` ya genera `BuildConfig.SUPABASE_URL`/`SUPABASE_KEY` desde `local.properties`, pero el cliente **no los consume** — ver §3.4 |

---

## 3.3. DEPENDENCIAS DEL PROYECTO

**Archivo:** `app/build.gradle.kts`, bloque `dependencies` (líneas 53–88).

### Dependencias de Supabase y backend

```kotlin
// líneas 74–80
implementation(platform("io.github.jan-tennert.supabase:bom:3.5.0"))
implementation("io.github.jan-tennert.supabase:auth-kt")
implementation("io.github.jan-tennert.supabase:postgrest-kt")
implementation("io.github.jan-tennert.supabase:storage-kt")
implementation("io.github.jan-tennert.supabase:realtime-kt")

implementation("io.ktor:ktor-client-android:3.0.3")
```

| Dependencia | Versión | Función en el proyecto |
|-------------|---------|------------------------|
| `bom:3.5.0` (Bill of Materials) | 3.5.0 | Gestiona versiones de todos los módulos `*-kt` de Supabase |
| `auth-kt` | vía BOM 3.5.0 | `signInWith(Email)`, `signUpWith(Email)`, `signInWith(Google)`, `signOut()`, `awaitInitialization()`, `currentUserOrNull()`, `sessionStatus`, `handleDeeplinks()` |
| `postgrest-kt` | vía BOM 3.5.0 | `postgrest.from("tickets").select/insert/update/delete`, `count(Count.EXACT)`, filtros `eq/gte/lt`, `order`, `range`, `limit` |
| `storage-kt` | vía BOM 3.5.0 | `storage.from("foto_ticket").upload(path, file)`, `bucket.publicUrl(path)` |
| `realtime-kt` | vía BOM 3.5.0 | Instalado en `SupabaseClient` pero sin uso funcional |
| `ktor-client-android` | 3.0.3 | Cliente HTTP subyacente del SDK Supabase |

### Dependencias de UI y arquitectura (extracto relevante)

```kotlin
// líneas 62–72
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.ui.graphics)
implementation(libs.androidx.compose.ui.tooling.preview)
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.compose.material.icons.extended)
implementation(libs.androidx.activity.compose)
implementation(libs.androidx.navigation.compose)
implementation(libs.androidx.lifecycle.viewmodel.compose)
```

```kotlin
// línea 83 — imágenes remotas (evidencias, avatares)
implementation("io.coil-kt:coil-compose:2.6.0")
```

**Plugins** (`app/build.gradle.kts` líneas 3–7): `android.application`, `kotlin.compose`, `org.jetbrains.kotlin.plugin.serialization`.

---

## 3.4. CONFIGURACIÓN DE BUILDCONFIG

**Archivo:** `app/build.gradle.kts`.

### Lectura de `local.properties` (líneas 15–20)

```kotlin
// Leer credenciales de local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
```

### Generación de campos BuildConfig (líneas 31–33)

```kotlin
// Definir BuildConfig fields para credenciales de Supabase
buildConfigField("String", "SUPABASE_URL", "\"${localProperties["supabase.url"]}\"")
buildConfigField("String", "SUPABASE_KEY", "\"${localProperties["supabase.key"]}\"")
```

### Activación (líneas 47–50)

```kotlin
buildFeatures {
    compose = true
    buildConfig = true
}
```

### Estado real vs configuración preparada — HALLAZGO IMPORTANTE

| Aspecto | Estado |
|---------|--------|
| `local.properties` existe en la raíz del proyecto | ✅ Sí (contiene `sdk.dir` y las claves `supabase.url`/`supabase.key`; está en `.gitignore`) |
| `local.properties.example` existe como plantilla | ✅ Sí |
| `build.gradle.kts` lee `local.properties` y genera `BuildConfig.SUPABASE_URL/KEY` | ✅ Sí |
| `SupabaseClient.kt` usa `BuildConfig.SUPABASE_URL/KEY` | ❌ **NO** — usa literales en líneas 14–15 |
| `BuildConfig` importado en `SupabaseClient.kt` | ❌ No |

**Conclusión documentable:** la infraestructura de BuildConfig está implementada en Gradle pero el cliente Supabase aún no la consume. La migración pendiente consiste en cambiar las líneas 14–15 de `SupabaseClient.kt` a:

```kotlin
supabaseUrl = BuildConfig.SUPABASE_URL,
supabaseKey = BuildConfig.SUPABASE_KEY
```

Mejora pendiente, no implementada (este documento no modifica código).

---

## 3.5. MODELOS Y ESTRUCTURA DE LA BASE DE DATOS

> **Advertencia:** el esquema real de PostgreSQL no está en el repositorio (no hay archivos `.sql`). La estructura siguiente se infiere de los modelos Kotlin `@Serializable` y de los nombres de columna usados en las consultas PostgREST. La verificación del DDL real requiere Supabase Dashboard → Table Editor.

### Tabla `profiles`

**Modelo Kotlin:** `app/src/main/java/com/devcore/ticketflow/Profile.kt` (archivo completo):

```kotlin
package com.devcore.ticketflow

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val fecha_creacion: String? = null
)
```

| Columna (inferida) | Tipo Kotlin | Uso en el código |
|--------------------|-------------|-------------------|
| `id` | `String` | UID de Supabase Auth; se consulta con `eq("id", usuario.id)` tras login (`LoginViewModel.kt:268-275`) |
| `nombre` | `String` | Editable por el usuario (`UserProfileViewModel.guardarCambios()` → `update` en `profiles`); mostrado en dashboards |
| `correo` | `String` | Solo lectura en la app (el correo es credencial de Auth) |
| `rol` | `String` | `"admin"` o `"usuario"`; decide la navegación (`LoginViewModel.kt:317-318`) y los filtros de admin |
| `fecha_creacion` | `String?` | Nullable; no se usa en lógica visible |

**Relación con Auth:** `profiles.id` = `auth.users.id` (UUID de Supabase Auth). El código asume que un trigger de Supabase crea la fila del perfil al registrarse (comentario en `RegisterViewModel.kt:186`: *"El trigger en Supabase crea automáticamente el perfil"*). El trigger en sí **no es verificable desde el código**.

### Tabla `tickets`

**Modelos Kotlin** (hay tres representaciones según la vista):

1. `TicketItem` — `ui/screens/user/UserTicketsViewModel.kt:22-31`:
```kotlin
@Serializable
data class TicketItem(
    val id: Long = 0,
    val equipo: String = "",
    val tipo_incidencia: String = "",
    val descripcion: String = "",
    val prioridad: String = "",
    val estado: String = "",
    val created_at: String = ""
)
```

2. `AdminTicketItem` — `ui/screens/admin/AdminTicketsViewModel.kt:23-37`:
```kotlin
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
```

3. `TicketDetail` — `ui/screens/user/TicketDetailViewModel.kt:20-33` (incluye `evidencia_url`).

| Columna (inferida) | Tipo Kotlin | Valores / uso |
|--------------------|-------------|----------------|
| `id` | `Long` | PK autoincremental; se obtiene del INSERT con `select()` (`CreateTicketViewModel.kt:156-160`) |
| `equipo` | `String` | Nombre del equipo; catálogo en `R.array.opciones_equipos` + opción "Otro" |
| `tipo_incidencia` | `String` | Texto libre del usuario |
| `descripcion` | `String` | Texto libre obligatorio |
| `prioridad` | `String` | `"Baja"`, `"Media"`, `"Alta"`, `"Critica"` (sin tilde en BD; la UI muestra "Crítica") |
| `estado` | `String` | `"Pendiente"`, `"En proceso"`, `"Solucionado"` |
| `created_at` | `String` | Timestamp; orden principal (`Order.DESCENDING`) |
| `profile` | `String?` | **FK lógica** al `id` de `profiles` (= UID Auth). Se inserta con `put("profile", JsonPrimitive(userId))` |
| `evidencia_url` | `String?` | URL pública del objeto en Storage; nullable |

**Relación tickets → profiles:** `tickets.profile` referencia `profiles.id`. La integridad referencial (FK constraint real en PostgreSQL) **no es verificable desde el código** — solo se observa el uso consistente del campo en INSERT y filtros.

---

## 3.6. CONSULTAS Y OPERACIONES CON SUPABASE

Todas las operaciones usan `SupabaseClient.client.postgrest.from("<tabla>")` dentro de `withContext(Dispatchers.IO)`.

### INSERT — creación de ticket
`CreateTicketViewModel.kt`, líneas 140–160:

```kotlin
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
```

*Demuestra:* inserción con asociación al UID (`profile = userId`) y retorno del ID generado.

### SELECT con filtro por usuario — "Mis tickets"
`UserTicketsViewModel.kt`, líneas 96–106:

```kotlin
val tickets = withContext(Dispatchers.IO) {
    val query = SupabaseClient.client.postgrest.from("tickets").select {
        filter {
            eq("profile", userId)
            if (filtro != null) eq("estado", filtro)
        }
        order("created_at", Order.DESCENDING)
    }
    kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = true }
        .decodeFromString<List<TicketItem>>(query.data)
}
```

*Demuestra:* filtro por UID del usuario + filtro por estado + orden.

### SELECT global con filtros (admin)
`AdminTicketsViewModel.kt`, líneas 101–115:

```kotlin
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
```

### SELECT con paginación (`range`)
`AdminHistoryViewModel.kt`, líneas 91–98:

```kotlin
private suspend fun consultarPagina(offset: Long): List<AdminTicketItem> =
    withContext(Dispatchers.IO) {
        val data = SupabaseClient.client.postgrest.from("tickets").select {
            order(column = "created_at", order = Order.DESCENDING)
            range(offset, offset + PAGE_SIZE - 1)
        }.data
        json.decodeFromString<List<AdminTicketItem>>(data)
    }
```

con `PAGE_SIZE = 50L` (línea 32) y `cargarMas()` que incrementa `offset = state.tickets.size` (línea 71).

### COUNT — métricas del dashboard admin
`AdminDashboardViewModel.kt`, líneas 65–70 (patrón repetido 6 veces: total, críticos, pendientes, en proceso, solucionados, usuarios):

```kotlin
val totalResult = SupabaseClient.client.postgrest.from("tickets")
    .select {
        count(Count.EXACT)
        limit(0)
    }
val total = totalResult.countOrNull()?.toInt() ?: 0
```

Variante con filtro (líneas 73–79):

```kotlin
val criticalResult = SupabaseClient.client.postgrest.from("tickets")
    .select {
        count(Count.EXACT)
        filter { eq("prioridad", "Critica") }
        limit(0)
    }
```

Variante sobre `profiles` para total de usuarios (líneas 109–114):

```kotlin
val usersResult = SupabaseClient.client.postgrest.from("profiles")
    .select {
        count(Count.EXACT)
        limit(0)
    }
val users = usersResult.countOrNull()?.toInt() ?: 0
```

### UPDATE — estado, prioridad y edición completa
`AdminTicketsViewModel.kt`, líneas 134–143 (estado):

```kotlin
val payload = buildJsonObject {
    put("estado", JsonPrimitive(nuevoEstado))
}

withContext(Dispatchers.IO) {
    SupabaseClient.client.postgrest.from("tickets")
        .update(payload) {
            filter { eq("id", ticketId) }
        }
}
```

Prioridad (líneas 176–184): mismo patrón con `put("prioridad", ...)`.

Edición completa `guardarEdicion()` (líneas 254–267): actualiza `equipo`, `tipo_incidencia`, `descripcion`, `prioridad`, `estado` por `id`.

### DELETE — eliminación de ticket (admin)
`AdminTicketsViewModel.kt`, líneas 298–303:

```kotlin
withContext(Dispatchers.IO) {
    SupabaseClient.client.postgrest.from("tickets")
        .delete {
            filter { eq("id", ticketId) }
        }
}
```

### UPDATE — perfil de usuario
`UserProfileViewModel.kt`, líneas 193–196:

```kotlin
SupabaseClient.client.postgrest.from("profiles")
    .update(payload) { filter { eq("id", usuarioId) } }
```

*(payload contiene `nombre`)*

### SELECT de perfil por UID
`LoginViewModel.kt`, líneas 268–275:

```kotlin
perfil = SupabaseClient.client
    .from("profiles")
    .select {
        filter {
            eq("id", usuario.id)
        }
    }
    .decodeSingle<Profile>()
```

*Nota:* usa el DSL corto `client.from("profiles")` (postgrest está en scope por el import `io.github.jan.supabase.postgrest.from`) en lugar de `client.postgrest.from(...)`; ambas formas coexisten en el código.

### Manejo de errores
Todos los ViewModels usan `try/catch` con `Log.e` y `errorMessage` en español. `UserTicketsViewModel.kt:108` distingue `CancellationException` con re-throw; varios ViewModels admin capturan `Exception` genérica (observación de calidad, no bloqueante).

---

## 3.7. SUPABASE STORAGE

### Configuración

- Módulo instalado: `install(Storage)` en `SupabaseClient.kt:19`.
- Dependencia: `storage-kt` (BOM 3.5.0) + `ktor-client-android:3.0.3`.
- **Bucket utilizado:** `foto_ticket` (constante de cadena en el código; la existencia y políticas del bucket son configurables en Dashboard → Storage y no verificables desde el repo).

### Flujo completo de evidencia fotográfica

**Paso 1 — Permiso y captura (UI):** `CreateTicketScreen.kt`

- Permiso `CAMERA` declarado en `AndroidManifest.xml:6` y solicitado en runtime (líneas 117–133).
- Archivo temporal en `cacheDir`: `File(context.cacheDir, "foto_ticket_${System.currentTimeMillis()}.jpg")` (líneas 122, 389).
- Cámara vía `ActivityResultContracts.TakePicture()` + `FileProvider` (líneas 107–114, 124–129).
- `photoPath` sobrevive rotaciones con `rememberSaveable` (línea 104).
- Vista previa con `AsyncImage` de Coil (líneas 352–364) y botón eliminar (líneas 365–377).
- El `File` resultante se guarda en el estado: `viewModel.onFotoSeleccionada(File(path))` (línea 112).

**Paso 2 — Upload a Storage (ViewModel):** `CreateTicketViewModel.kt`, líneas 131–138:

```kotlin
val evidenciaUrl = state.fotoFile?.let { file ->
    withContext(Dispatchers.IO) {
        val path = "evidencia/${System.currentTimeMillis()}.jpg"
        val bucket = SupabaseClient.client.storage.from("foto_ticket")
        bucket.upload(path, file)
        bucket.publicUrl(path).toString()
    }
}
```

| Elemento | Valor real |
|----------|------------|
| Bucket | `"foto_ticket"` |
| Ruta del objeto | `"evidencia/<timestamp>.jpg"` dentro del bucket |
| Método | `bucket.upload(path, file)` (sobrecarga con `File`) |
| URL | `bucket.publicUrl(path).toString()` → URL pública del objeto |

**Paso 3 — Asociación al ticket:** la URL se inserta como `evidencia_url` en el payload del INSERT (línea 146) y se persiste en `tickets.evidencia_url`.

**Paso 4 — Visualización:** `TicketDetailScreen.kt`, líneas 210–235:

```kotlin
if (!ticket.evidenciaUrl.isNullOrEmpty()) {
    Card(...) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "EVIDENCIA ADJUNTA", ...)
            Spacer(modifier = Modifier.height(12.dp))
            AsyncImage(
                model = ticket.evidenciaUrl,
                contentDescription = "Evidencia del ticket",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
    }
}
```

`TicketDetailViewModel.kt:31-32` mapea la columna con `@SerialName("evidencia_url")`.

### Variante legacy (Activity XML)

`UserCreateTicketActivity.kt` (líneas 126–131) contiene una implementación paralela del upload con `bucket.upload(path, file) { upsert = true }` — la única diferencia es `upsert = true`. Esta Activity sigue declarada en el Manifest pero el flujo activo es el Compose (`CreateTicketScreen`/`CreateTicketViewModel`).

### Lo que NO está implementado en Storage

- No hay eliminación de objetos del bucket al borrar un ticket (`AdminTicketsViewModel.eliminarTicket()` solo hace DELETE en PostgREST).
- No hay subida de imágenes desde galería (solo cámara).
- No hay compresión/resize de imagen antes del upload (el archivo se sube tal cual desde `cacheDir`).

---

## APÉNDICE A — OPERACIONES CRUD RESUMEN

| Operación | Tabla | Método SDK | Archivo:Líneas |
|-----------|-------|------------|----------------|
| INSERT | tickets | `postgrest.from("tickets").insert(...)` | CreateTicketViewModel.kt:151-154 |
| SELECT (usuario) | tickets | `select { filter { eq("profile", userId) } }` | UserTicketsViewModel.kt:97-106 |
| SELECT (admin) | tickets | `select { filter { eq("estado"/"prioridad") } }` | AdminTicketsViewModel.kt:102-114 |
| SELECT (detalle) | tickets | `select { filter { eq("id", id) } }` | TicketDetailViewModel.kt:69-78 |
| SELECT (paginado) | tickets | `select { order; range(...) }` | AdminHistoryViewModel.kt:93-97 |
| COUNT | tickets/profiles | `select { count(Count.EXACT); limit(0) }` | AdminDashboardViewModel.kt:65-114 |
| UPDATE | tickets | `update(payload) { filter { eq("id") } }` | AdminTicketsViewModel.kt:139-143, 181-184, 263-266 |
| DELETE | tickets | `delete { filter { eq("id") } }` | AdminTicketsViewModel.kt:299-302 |
| SELECT/UPDATE | profiles | `from("profiles").select/update` | LoginViewModel.kt:268-275; UserProfileViewModel.kt:127-129, 194-195 |
| UPLOAD | Storage | `storage.from("foto_ticket").upload` | CreateTicketViewModel.kt:134-136 |

## APÉNDICE B — LIMITACIONES Y PENDIENTES (honestidad documental)

1. **Credenciales literales** en `SupabaseClient.kt` pese a BuildConfig configurado.
2. **Sin capa Repository** — ViewModels acoplados al SDK.
3. **Realtime instalado sin uso.**
4. **Esquema PostgreSQL no verificable** desde el repo (sin DDL, sin políticas RLS visibles).
5. **Trigger de creación de perfil** asumido por comentarios, no verificable.
6. **CRUD de usuarios y equipos (admin) es solo lectura.**
7. **Google OAuth** implementado en código; verificación E2E pendiente de configuración externa.
8. **Activities/layouts XML legacy** siguen declarados en el Manifest.
9. **Tests unitarios no cubren código de producción** (lógica duplicada en los tests).

---

*Fin del material técnico — Parte 1.*
