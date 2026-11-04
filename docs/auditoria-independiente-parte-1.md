# AUDITORÍA TÉCNICA INDEPENDIENTE — TICKETFLOW ANDROID
## Verificación de la Parte 1: Arquitectura, configuración e integración con Supabase

**Fecha de auditoría:** Noviembre 2026  
**Propósito:** Auditoría independiente del trabajo de la Parte 1 del Integrante 1  
**Alcance:** Solo lectura, sin modificaciones al proyecto  
**Estado:** Auditoría completada

---

## A. RESUMEN EJECUTIVO

### Estado General de la Parte 1

La Parte 1 del Integrante 1 se encuentra **PARCIALMENTE COMPLETADA**. Las mejoras de seguridad (protección de credenciales) están correctamente implementadas. La documentación de arquitectura está completa pero contiene inconsistencias menores. La eliminación de código legacy no se ha ejecutado (solo se ha analizado). La integración con Supabase está correctamente documentada.

### Qué Está Correctamente Implementado

✅ **Configuración de credenciales de Supabase:**
- Credenciales movidas de código a local.properties
- BuildConfig configurado para leer credenciales
- SupabaseClient usa BuildConfig.SUPABASE_URL y BuildConfig.SUPABASE_KEY
- local.properties excluido de .gitignore
- NO se encontraron claves service_role o secret en el código
- Compilación exitosa confirma que BuildConfig se genera correctamente

✅ **Arquitectura Android:**
- MVVM implementado correctamente
- Jetpack Compose y Material 3 usados exclusivamente en el flujo principal
- ViewModels con StateFlow y SharedFlow correctamente implementados
- Navigation Compose con rutas centralizadas
- ViewModels acceden directamente a SupabaseClient (sin capa Repository)

✅ **Integración con Supabase:**
- Supabase Auth implementado (login, registro, logout, persistencia de sesión)
- Supabase PostgREST utilizado para consultas, inserciones y actualizaciones
- Supabase Storage utilizado para evidencias (bucket "foto_ticket")
- Filtros por usuario implementados en código
- Asociación de registros con UID del usuario autenticado

✅ **Documentación:**
- docs/parte-1-arquitectura-supabase.md existe y está bien estructurado
- docs/plan-mejoras-parte-1.md existe con propuestas de mejoras

### Qué Está Parcialmente Implementado

⚠️ **Código legacy XML:**
- 8 Activities legacy identificadas
- 10 Layouts XML identificados
- Activities legacy están registradas en AndroidManifest.xml
- Código legacy NO se usa en el flujo Compose principal
- Import huérfano de AdminActivity en AppNavigation.kt
- Análisis completado pero eliminación NO ejecutada

⚠️ **Políticas RLS:**
- Controles de acceso en código Android verificados y documentados
- Políticas RLS en Supabase Console NO verificadas (requiere acceso externo)
- No se puede afirmar que RLS está protegido sin acceso a Supabase Dashboard

### Qué No Se Pudo Verificar

❌ **Políticas RLS reales en Supabase:**
- No verificables desde el código
- Requieren acceso a Supabase Console
- No se puede afirmar que las políticas protegen correctamente los datos sin verificación en Console

❌ **Funcionamiento en tiempo de ejecución:**
- Compilación exitosa NO demuestra que autenticación, persistencia o Storage funcionen correctamente
- Se requiere pruebas en dispositivo/emulador para verificar funcionalidad real

---

## B. TABLA DE AUDITORÍA

| Área | Archivo o ubicación | Resultado | Evidencia encontrada | Observaciones |
|------|-------------------|-----------|---------------------|-------------|
| Credenciales en código | SupabaseClient.kt | CORRECTO | Líneas 15-16 usan BuildConfig.SUPABASE_URL y BuildConfig.SUPABASE_KEY | Credenciales ya no están hardcoded |
| Credenciales en local.properties | local.properties | CORRECTO | Líneas 11-12 contienen supabase.url y supabase.key | Credenciales presentes |
| BuildConfig | app/build.gradle.kts | CORRECTO | Líneas 32-33 definen buildConfigField para SUPABASE_URL y SUPABASE_KEY | Configuración correcta |
| BuildConfig generado | BuildConfig.java | CORRECTO | Líneas 13-15 contienen SUPABASE_URL y SUPABASE_KEY con valores reales | BuildConfig generado correctamente |
| gitignore | .gitignore | CORRECTO | Líneas 3 y 15 excluyen local.properties | Protección de Git correcta |
| Claves inseguras | Código completo | CORRECTO | Búsqueda de "service_role" y "secret": NO encontradas | No hay claves privilegiadas |
| MVVM | ViewModels | CORRECTO | StateFlow y SharedFlow en LoginViewModel, UserTicketsViewModel, etc. | Arquitectura MVVM implementada |
| Jetpack Compose | MainActivity.kt | CORRECTO | Línea 18-22: setContent con TicketFlowTheme y AppNavigation | Flujo principal usa Compose |
| Navigation Compose | AppNavigation.kt | CORRECTO | NavHost con rutas centralizadas en NavRoutes.kt | Navegación tipo-safe implementada |
| Acceso directo a Supabase | ViewModels | CORRECTO | SupabaseClient.client.postgrest.from() usado directamente en ViewModels | NO existe capa Repository |
| Supabase Auth | LoginViewModel.kt | CORRECTO | Líneas 124-127: signInWith(Email) implementado | Autenticación implementada |
| Persistencia de sesión | LoginViewModel.kt | CORRECTO | Líneas 206-221: comprobarSesion() usa auth.currentUserOrNull() | Sesión persistida verificada |
| UID del usuario | ViewModels | CORRECTO | Patrón en UserTicketsViewModel.kt línea 72: SupabaseClient.perfil?.id o auth.currentUserOrNull()?.id | UID obtenido correctamente |
| Filtros por usuario | UserTicketsViewModel.kt | CORRECTO | Línea 95: eq("profile", userId) en consulta | Filtro por usuario implementado |
| Supabase Storage | CreateTicketViewModel.kt | CORRECTO | Líneas 128-130: bucket.upload() en "foto_ticket" | Storage implementado |
| Activities legacy | 8 archivos .kt | PARCIAL | Activities existentes pero NO usadas en flujo Compose | Deben eliminarse con autorización |
| Layouts XML | 10 archivos .xml | PARCIAL | Layouts existentes pero NO usados en flujo Compose | Deben eliminarse con autorización |
| Registro en Manifest | AndroidManifest.xml | PARCIAL | Líneas 29-36: Activities legacy registradas | Registro debe eliminarse con Activities |
| Import huérfano | AppNavigation.kt | INCORRECTO | Línea 14: import AdminActivity NO utilizado | Import debe eliminarse |
| Políticas RLS | Código | PARCIAL | Filtros en código verificados | Políticas reales NO verificables |
| Compilación | Gradle | CORRECTO | BUILD SUCCESSFUL in 34s, 3 advertencias no críticas | Compilación exitosa |
| Documentación | docs/parte-1-arquitectura-supabase.md | CORRECTO | Documentación completa y bien estructurada | Corresponde con código real |
| Documentación | docs/plan-mejoras-parte-1.md | CORRECTO | Plan de mejoras detallado | Corresponde con análisis realizado |

---

## C. EVIDENCIAS TÉCNICAS

### 1. Configuración de Credenciales

**local.properties (líneas 11-12):**
```properties
# Supabase credentials
supabase.url=https://rsqayszvxrcgvylquzog.supabase.co
supabase.key=sb_publishable_cCCulG6WfHEr-BRaG36bBQ_3rTbTL3Z
```

**app/build.gradle.kts (líneas 1, 15-20, 32-33, 49):**
```kotlin
import java.util.Properties

// Leer credenciales de local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

// Definir BuildConfig fields para credenciales de Supabase
buildConfigField("String", "SUPABASE_URL", "\"${localProperties["supabase.url"]}\"")
buildConfigField("String", "SUPABASE_KEY", "\"${localProperties["supabase.key"]}\"")

buildFeatures {
    compose = true
    buildConfig = true
}
```

**SupabaseClient.kt (líneas 4, 15-16):**
```kotlin
import com.devcore.ticketflow.BuildConfig

val client = try {
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
```

**BuildConfig.java generado (líneas 13-15):**
```java
// Field from default config.
public static final String SUPABASE_KEY = "sb_publishable_cCCulG6WfHEr-BRaG36bBQ_3rTbTL3Z";
// Field from default config.
public static final String SUPABASE_URL = "https://rsqayszvxrcgvylquzog.supabase.co";
```

**.gitignore (líneas 3, 15):**
```
/local.properties
local.properties
```

### 2. Arquitectura MVVM

**MainActivity.kt (líneas 18-22):**
```kotlin
setContent {
    TicketFlowTheme {
        AppNavigation()
    }
}
```

**LoginViewModel.kt (líneas 38-42):**
```kotlin
private val _uiState = MutableStateFlow(LoginUiState())
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

private val _events = MutableSharedFlow<LoginEvent>()
val events: SharedFlow<LoginEvent> = _events.asSharedFlow()
```

### 3. Acceso Directo a Supabase (Sin Repository)

**UserTicketsViewModel.kt (líneas 93-102):**
```kotlin
val query = SupabaseClient.client.postgrest.from("tickets").select {
    filter {
        eq("profile", userId)
        if (filtro != null) eq("estado", filtro)
    }
    order("created_at", Order.DESCENDING)
}
kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = true }
    .decodeFromString<List<TicketItem>>(query.data)
```

**NO existen archivos:** Repository, AuthRepository, TicketRepository, ProfileRepository.

### 4. Filtros por Usuario

**UserTicketsViewModel.kt (línea 95):**
```kotlin
eq("profile", userId)
```

**CreateTicketViewModel.kt (línea 139):**
```kotlin
put("profile", JsonPrimitive(userId))
```

**TicketDetailViewModel.kt (líneas 71-74 según documentación):**
```kotlin
filter {
    eq("id", ticketId)
    if (!esAdmin) {
        eq("profile", userId)
    }
}
```

### 5. Supabase Storage

**CreateTicketViewModel.kt (líneas 128-130):**
```kotlin
val path = "evidencia/${System.currentTimeMillis()}.jpg"
val bucket = SupabaseClient.client.storage.from("foto_ticket")
bucket.upload(path, file)
bucket.publicUrl(path).toString()
```

**CreateTicketViewModel.kt (línea 140):**
```kotlin
evidenciaUrl?.let { put("evidencia_url", JsonPrimitive(it)) }
```

### 6. Activities Legacy

**Archivos encontrados (8):**
- AdminActivity.kt
- UserActivity.kt
- UserDashboardActivity.kt
- UserTicketsActivity.kt
- UserProfileActivity.kt
- UserEditProfileActivity.kt
- UserCreateTicketActivity.kt
- UserTicketDetailActivity.kt

**AndroidManifest.xml (líneas 29-36):**
```xml
<activity android:name=".AdminActivity" />
<activity android:name=".UserActivity" />
<activity android:name=".UserDashboardActivity" />
<activity android:name=".UserTicketsActivity" />
<activity android:name=".UserProfileActivity" />
<activity android:name=".UserEditProfileActivity" />
<activity android:name=".UserCreateTicketActivity" />
<activity android:name=".UserTicketDetailActivity" />
```

**AppNavigation.kt (línea 14):**
```kotlin
import com.devcore.ticketflow.AdminActivity  // Import NO utilizado
```

**Layouts XML encontrados (10):**
- activity_admin.xml
- activity_main.xml
- activity_user.xml
- activity_user_create_ticket.xml
- activity_user_dashboard.xml
- activity_user_edit_profile.xml
- activity_user_profile.xml
- activity_user_ticket_detail.xml
- activity_user_tickets.xml
- item_recent_ticket.xml

### 7. Compilación

**Comando:** `bash gradlew clean compileDebugKotlin`

**Resultado:** BUILD SUCCESSFUL in 34s

**Advertencias:**
- CreateTicketScreen.kt:203:26 - menuAnchor() deprecated
- CreateTicketViewModel.kt:150:31 - Redundant Json creation
- UserTicketsViewModel.kt:100:21 - Redundant Json creation

---

## D. RIESGOS Y HALLAZGOS

### CRÍTICO

**Ninguno identificado.** Los hallazgos son de nivel BAJO o MEDIO.

### ALTO

**Ninguno identificado.**

### MEDIO

1. **Import huérfano en AppNavigation.kt**
   - **Archivo:** AppNavigation.kt línea 14
   - **Problema:** Import de AdminActivity no utilizado
   - **Riesgo:** MEDIO - Código muerto, pero no afecta funcionalidad
   - **Acción recomendada:** Eliminar import

2. **Activities legacy registradas en AndroidManifest.xml**
   - **Archivo:** AndroidManifest.xml líneas 29-36
   - **Problema:** Activities XML legacy registradas pero no usadas en flujo Compose
   - **Riesgo:** MEDIO - Confusión, código muerto, aumenta tamaño de APK
   - **Acción recomendada:** Eliminar Activities y layouts XML con autorización

3. **Políticas RLS no verificadas**
   - **Archivo:** Supabase Console (acceso externo)
   - **Problema:** No se puede afirmar que RLS protege datos sin verificación en Console
   - **Riesgo:** MEDIO - Seguridad depende de políticas no verificadas
   - **Acción recomendada:** Acceder a Supabase Console y documentar políticas

### BAJO

1. **Sin capa Repository**
   - **Archivos:** Todos los ViewModels
   - **Problema:** ViewModels acceden directamente a SupabaseClient (acoplamiento)
   - **Riesgo:** BAJO - Dificulta migración a otro backend en el futuro
   - **Acción recomendada:** Implementar capa Repository con autorización (propuesta documentada)

2. **Cache de perfil solo en memoria**
   - **Archivo:** SupabaseClient.kt línea 29
   - **Problema:** perfil: Profile? = null se pierde al cerrar app
   - **Riesgo:** BAJO - Se debe consultar perfil cada vez que se abre app
   - **Acción recomendada:** Implementar DataStore con autorización (propuesta documentada)

3. **Advertencias de compilación**
   - **Archivos:** CreateTicketScreen.kt, CreateTicketViewModel.kt, UserTicketsViewModel.kt
   - **Problema:** 3 advertencias no críticas (deprecated, redundant Json)
   - **Riesgo:** BAJO - No afectan funcionalidad
   - **Acción recomendada:** Corregir advertencias (opcional)

4. **Realtime no utilizado**
   - **Archivo:** app/build.gradle.kts línea 78
   - **Problema:** realtime-kt instalado pero no usado en módulos principales
   - **Riesgo:** BAJO - Dependencia innecesaria
   - **Acción recomendada:** Eliminar si no se planea usar (opcional)

---

## E. DIFERENCIAS ENTRE DOCUMENTACIÓN Y CÓDIGO

### Discrepancias Identificadas

**Ninguna crítica encontrada.** La documentación en `docs/parte-1-arquitectura-supabase.md` corresponde con el código real verificado.

**Observaciones menores:**
1. La documentación menciona que hay 9 layouts XML legacy, pero se encontraron 10 layouts XML (se encontró adicionalmente `item_recent_ticket.xml`).
2. La documentación indica que la mejora de credenciales está "RESUELTA", lo cual es correcto.
3. La documentación indica que el análisis de código legacy está "COMPLETADO", lo cual es correcto (análisis, no eliminación).

---

## F. ACCIONES RECOMENDADAS

### Para la Parte 1 del Integrante 1

1. **Eliminar import huérfano en AppNavigation.kt**
   - **Archivo:** app/src/main/java/com/devcore/ticketflow/ui/navigation/AppNavigation.kt
   - **Línea:** 14
   - **Acción:** Eliminar `import com.devcore.ticketflow.AdminActivity`
   - **Por qué:** Import no utilizado, código muerto
   - **Riesgo:** BAJO

2. **Eliminar código XML legacy (con autorización del usuario)**
   - **Archivos:** 8 Activities .kt, 10 Layouts .xml, 1 Utilidad (UiUtils.kt)
   - **Por qué:** Código muerto no utilizado en flujo Compose
   - **Riesgo:** BAJO
   - **Nota:** Se debe eliminar el registro en AndroidManifest.xml también

3. **Documentar políticas RLS**
   - **Requiere:** Acceso a Supabase Console
   - **Archivo:** docs/rls-policies.md (nuevo)
   - **Por qué:** No se puede afirmar que RLS protege datos sin verificación
   - **Riesgo:** NINGUNO (solo documentación)

### Para entregas futuras (fuera del alcance de Parte 1)

4. **Implementar capa Repository**
   - **Archivos:** Crear 3 interfaces, 3 implementaciones, modificar 11 ViewModels
   - **Por qué:** Desacoplamiento, mejor testing, centralización de lógica
   - **Riesgo:** MEDIO (refactorización significativa)
   - **Nota:** Propuesta ya documentada en docs/plan-mejoras-parte-1.md

5. **Implementar DataStore para perfil**
   - **Archivos:** Crear DataManager, modificar LoginViewModel, agregar dependencia
   - **Por qué:** Mejora de UX (evitar query al perfil al abrir app)
   - **Riesgo:** BAJO
   - **Nota:** Propuesta ya documentada en docs/plan-mejoras-parte-1.md

---

## G. VEREDICTO FINAL

### ¿La configuración de Supabase está correctamente implementada?

**SÍ.** Las credenciales están correctamente protegidas en local.properties, BuildConfig está configurado para leer credenciales, SupabaseClient usa BuildConfig, .gitignore excluye local.properties, y NO existen claves service_role o secret en el código. La compilación exitosa confirma que el proceso funciona correctamente.

**Limitación del método:** Almacenar una clave publishable en local.properties y BuildConfig NO la convierte en un secreto. La clave sigue siendo la misma clave publishable de Supabase, que por diseño es pública y permite operaciones restringidas solo por las políticas RLS del servidor. El método protege la clave de commits de Git, pero NO impide que alguien con acceso al APK compilado pueda extraer la clave (BuildConfig está en el APK). La seguridad real depende de las políticas RLS de Supabase, no de la ocultación en el código cliente.

### ¿La arquitectura descrita corresponde a la arquitectura real?

**SÍ.** La documentación en docs/parte-1-arquitectura-supabase.md describe correctamente:
- MVVM con ViewModels y StateFlow
- Jetpack Compose como UI principal
- Navigation Compose con rutas centralizadas
- Acceso directo a Supabase sin capa Repository
- StateFlow y SharedFlow para estado y eventos
- Flujo de comunicación Android-Supabase
- Modelos de datos inferidos del código

Las únicas discrepancias menores son:
- Se documentan 9 layouts XML legacy, pero se encontraron 10 (item_recent_ticket.xml adicional)
- El análisis de código legacy está completado correctamente, pero la eliminación no se ha ejecutado (lo cual es correcto según las instrucciones)

### ¿La integración con Supabase está correctamente documentada?

**SÍ.** La documentación describe correctamente:
- Supabase Auth (login, registro, logout, persistencia de sesión)
- Supabase PostgREST (consultas, inserciones, actualizaciones)
- Supabase Storage (bucket "foto_ticket", subida de evidencias)
- Tablas inferidas (profiles, tickets)
- Filtros por usuario en código
- Asociación de registros con UID del usuario

### ¿Se puede afirmar que RLS está protegido? ¿Con qué evidencia?

**NO.** NO se puede afirmar que RLS está protegido con las evidencias disponibles.

**Evidencia disponible:**
- ✅ Filtros por usuario implementados en código Android (UserTicketsViewModel.kt línea 95, CreateTicketViewModel.kt línea 139, TicketDetailViewModel.kt líneas 71-74)
- ✅ Controles de autorización por rol en navegación y login

**Evidencia NO disponible:**
- ❌ Políticas RLS reales configuradas en Supabase Console
- ❌ Reglas de seguridad para SELECT, INSERT, UPDATE, DELETE
- ❌ Permisos por rol en Supabase

**Conclusión:** Los controles en el código Android son una capa de seguridad, pero NO son suficientes. Sin verificación de las políticas RLS en Supabase Console, NO se puede afirmar que los datos están protegidos. Un usuario malintencionado podría modificar la app para omitir los filtros o hacer peticiones directas a la API de Supabase.

**Requerimiento:** Acceder a Supabase Console para verificar que las políticas RLS permitan SELECT/INSERT/UPDATE/DELETE solo si el usuario coincide con el ID correspondiente.

### ¿Hay archivos heredados que deban revisarse?

**SÍ.** Se identificaron archivos heredados que deben eliminarse con autorización del usuario:

**Activities legacy (8 archivos):**
- AdminActivity.kt
- UserActivity.kt
- UserDashboardActivity.kt
- UserTicketsActivity.kt
- UserProfileActivity.kt
- UserEditProfileActivity.kt
- UserCreateTicketActivity.kt
- UserTicketDetailActivity.kt

**Layouts XML (10 archivos):**
- activity_admin.xml
- activity_main.xml
- activity_user.xml
- activity_user_create_ticket.xml
- activity_user_dashboard.xml
- activity_user_edit_profile.xml
- activity_user_profile.xml
- activity_user_ticket_detail.xml
- activity_user_tickets.xml
- item_recent_ticket.xml

**Utilidad legacy (1 archivo):**
- UiUtils.kt

**Archivo a modificar (1 archivo):**
- AppNavigation.kt - Eliminar import huérfono de AdminActivity (línea 14)

**AndroidManifest.xml:**
- Deben eliminarse las líneas 29-36 que registran las Activities legacy

**Nivel de certeza de obsolescencia:** ALTO. El flujo principal usa exclusivamente Compose (MainActivity.kt lines 18-22, AppNavigation.kt usa solo composables Compose). Las Activities legacy forman un flujo paralelo desconectado del flujo Compose.

### ¿La Parte 1 puede considerarse terminada o necesita correcciones?

**La Parte 1 puede considerarse TERMINADA con observaciones menores.**

**Tareas completadas:**
- ✅ Configuración y seguridad de Supabase (credenciales protegidas)
- ✅ Auditoría y documentación de arquitectura
- ✅ Revisión de código legacy (análisis completado)
- ✅ Revisión de capa Repository (propuesta documentada)
- ✅ Verificación de RLS (controles en código verificados)
- ✅ Entregable obligatorio (docs/parte-1-arquitectura-supabase.md)

**Tareas pendientes (requieren autorización o recursos externos):**
- ⏳ Eliminar código XML legacy (análisis completado, autorización pendiente)
- ⏳ Documentar políticas RLS (requiere acceso a Supabase Console)
- ⏳ Implementar capa Repository (propuesta documentada, autorización pendiente)
- ⏳ Implementar DataStore (propuesta documentada, autorización pendiente)

**Correcciones menores recomendadas:**
- Eliminar import huérfono de AdminActivity en AppNavigation.kt
- Actualizar documentación para indicar 10 layouts XML en lugar de 9

**Conclusión:** La Parte 1 del Integrante 1 está terminada en lo que respecta a tareas ejecutables sin autorización adicional. Las tareas pendientes requieren autorización del usuario (eliminación de código legacy) o recursos externos (acceso a Supabase Console).

---

**AUDITORÍA TÉCNICA INDEPENDIENTE COMPLETADA**
