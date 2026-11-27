# PRUEBAS DE INTEGRACIÓN CON SUPABASE — TICKETFLOW MOBILE

**Proyecto:** TicketFlow Mobile — Android con Kotlin y Jetpack Compose  
**Backend:** Supabase (Auth, PostgreSQL/PostgREST, Storage)  
**Documento:** Parte 5 — Pruebas integrales e integración con Supabase  
**Fecha:** 7 de noviembre de 2026  
**Alcance:** Documentación técnica basada exclusivamente en el código fuente y los tests existentes en el repositorio.

> **Advertencia metodológica:** Este documento distingue entre (a) pruebas automatizadas que existen y ejecutan, (b) integraciones implementadas en código cuya ejecución real contra Supabase requiere dispositivo/emulador, y (c) comprobaciones manuales pendientes en el panel de Supabase. Ninguna integración se declara "probada" solo por existir el código que la invoca.

---

## 1. INTRODUCCIÓN Y OBJETIVO

TicketFlow Mobile utiliza Supabase como backend exclusivo para autenticación, persistencia de datos relacionales y almacenamiento de archivos. Este documento registra qué pruebas de integración existen realmente en el proyecto, qué evidencias las respaldan y cuáles permanecen pendientes de ejecución manual.

El objetivo es dar trazabilidad al equipo sobre el estado real de las pruebas: qué está automatizado, qué está implementado pero sin prueba E2E, y qué depende de configuración externa (Supabase Dashboard, Google Cloud Console).

---

## 2. TECNOLOGÍAS UTILIZADAS

| Capa | Tecnología | Versión / Referencia |
|------|------------|----------------------|
| Lenguaje | Kotlin | 2.2.x (plugin Kotlin en `app/build.gradle.kts`) |
| UI | Jetpack Compose + Material 3 | Compose BOM `2024.12.01` |
| Arquitectura | MVVM con `ViewModel`, `StateFlow`, `SharedFlow` | `androidx.lifecycle` |
| Navegación | Navigation Compose | `2.8.x` |
| Backend SDK | Supabase Kotlin (BOM) | `io.github.jan-tennert.supabase:bom:3.5.0` |
| Módulos Supabase | `auth-kt`, `postgrest-kt`, `storage-kt`, `realtime-kt` | BOM 3.5.0 |
| HTTP | Ktor client Android | `3.0.3` |
| Serialización | kotlinx.serialization | plugin `org.jetbrains.kotlin.plugin.serialization` |
| Imágenes | Coil | `2.6.0` |
| Pruebas unitarias | JUnit 4 | `testImplementation(libs.junit)` |
| Pruebas instrumentadas | AndroidX Test + Espresso | `androidTestImplementation` |

---

## 3. ARQUITECTURA GENERAL DE LA INTEGRACIÓN

```
┌─────────────────────────────────────────────────────────┐
│  UI (Compose Screens)                                   │
│  LoginScreen, RegisterScreen, *Screen.kt                │
└──────────────────────────┬──────────────────────────────┘
                           │ eventos / StateFlow
┌──────────────────────────▼──────────────────────────────┐
│  ViewModels (MVVM)                                      │
│  LoginViewModel, RegisterViewModel, CreateTicketViewModel│
│  UserTicketsViewModel, AdminTicketsViewModel, etc.      │
└──────────────────────────┬──────────────────────────────┘
                           │ llamadas directas (sin capa Repository)
┌──────────────────────────▼──────────────────────────────┐
│  SupabaseClient (objeto singleton)                      │
│  app/.../SupabaseClient.kt                              │
│  createSupabaseClient(url, key) {                       │
│      install(Auth); install(Postgrest);                 │
│      install(Storage); install(Realtime)                │
│  }                                                      │
└───────┬──────────────┬───────────────┬─────────────────┘
        │              │               │
   ┌────▼────┐   ┌─────▼─────┐   ┌─────▼──────┐
   │ Supabase│   │ PostgREST │   │  Storage   │
   │   Auth  │   │(PostgreSQL│   │ foto_ticket│
   │         │   │  tickets, │   │   bucket   │
   │         │   │ profiles) │   │            │
   └─────────┘   └───────────┘   └────────────┘
```

**Puntos clave de la arquitectura:**

- `SupabaseClient.kt` define un objeto singleton `SupabaseClient.client` con los cuatro módulos instalados y una variable global `perfil: Profile?` usada como caché de sesión en memoria.
- **No existe capa Repository**: los ViewModels llaman directamente a `SupabaseClient.client.auth`, `.postgrest` y `.storage`.
- Las credenciales se leen en `app/build.gradle.kts` desde `local.properties` y se exponen como `BuildConfig.SUPABASE_URL` y `BuildConfig.SUPABASE_KEY` (`buildConfig = true` está activado). Nota: en el código auditado, `SupabaseClient.kt` contenía los valores literales en lugar de referenciar `BuildConfig`; la configuración de build existe pero debe verificarse que el cliente los consuma.
- La sesión la gestiona el propio SDK de Supabase Auth (persistencia interna del token); la app no implementa almacenamiento paralelo de credenciales.

---

## 4. AUTENTICACIÓN DE USUARIOS

### 4.1 Registro con correo y contraseña

**Implementación:** `ui/screens/auth/RegisterViewModel.kt` → `registrarse()` (líneas 105–240).

- Llamada real: `SupabaseClient.client.auth.signUpWith(Email) { email; password }` (línea 179).
- Validaciones previas: nombre obligatorio (≥2 caracteres), email con `Patterns.EMAIL_ADDRESS`, contraseña ≥6 caracteres, confirmación coincidente.
- Tras el registro cierra la sesión temporal (`auth.signOut()`, línea 192) para forzar confirmación de correo.
- Estado `requiresEmailConfirmation = true` en `RegisterUiState`.
- **Dependencia externa:** se asume un trigger en Supabase que crea la fila en `public.profiles` al registrarse en Auth. Este trigger **no es verificable desde el código** y debe confirmarse en el Dashboard (Authentication → Hooks/Triggers o Database → Triggers).

### 4.2 Inicio de sesión con correo y contraseña

**Implementación:** `ui/screens/auth/LoginViewModel.kt` → `iniciarSesion()` (líneas 85–167).

- Llamada real: `SupabaseClient.client.auth.signInWith(Email) { email; password }` (líneas 133–136).
- Tras autenticar llama a `obtenerPerfilYRedirigir()` que consulta `profiles` por `id = usuario.id` con hasta 3 reintentos y delay de 600 ms (líneas 261–286).
- Validaciones defensivas: el `perfil.id` debe coincidir con `usuario.id` (líneas 293–296); rol vacío o no reconocido provoca `signOut()` preventivo (líneas 301–341).
- Mapeo de errores: `email_not_confirmed`, `Invalid login credentials`, errores de red/timeout → mensajes en español.

### 4.3 Inicio de sesión con Google (OAuth)

**Implementación:**

- `LoginViewModel.iniciarSesionConGoogle()` (líneas 169–213): `auth.signInWith(provider = Google, redirectUrl = "ticketflow://auth/callback")` usando `io.github.jan.supabase.auth.providers.Google` (proveedor correcto del SDK 3.5.0).
- `MainActivity.handleAuthIntent()` (líneas 46–72): procesa el deep link `ticketflow://auth/callback`, con deduplicación mediante `lastHandledIntentData`, e invoca `SupabaseClient.client.handleDeeplinks(intent, onSessionSuccess, onError)` tanto en `onCreate` (arranque en frío) como en `onNewIntent`.
- `AndroidManifest.xml` (líneas 28–36): intent-filter `VIEW` + `DEFAULT` + `BROWSABLE` con `scheme="ticketflow"`, `host="auth"`, `path="/callback"`.
- `LoginViewModel.observarEstadoDeSesion()` (líneas 215–237): colecta `auth.sessionStatus` y dispara `obtenerPerfilYRedirigir()` cuando llega `SessionStatus.Authenticated`.

**Estado:** código implementado; **no probado E2E**. Requiere configuración externa pendiente (ver sección 11).

### 4.4 Cierre de sesión

**Implementación:** `ui/navigation/AppNavigation.kt` → `performSignOut()` (líneas 43–57).

- `SupabaseClient.client.auth.signOut()` + `SupabaseClient.perfil = null`.
- Navegación a `NavRoutes.LOGIN` con `popUpTo(0) { inclusive = true }` (limpia todo el back stack).
- También invocado desde `UserProfileScreen`/`AdminProfileScreen` vía callback `onSignOut`.

### 4.5 Persistencia y recuperación de sesión

**Implementación:** `LoginViewModel.init` → `comprobarSesion()` (líneas 367–386).

- `auth.awaitInitialization()` espera a que el SDK restaure la sesión persistida.
- `auth.currentUserOrNull()` devuelve el usuario; si existe, `obtenerPerfilYRedirigir()` carga el perfil y emite `LoginEvent.NavigateToDashboard(rol)`.
- `startDestination = NavRoutes.LOGIN` en `AppNavigation` (línea 61): la app siempre arranca en login y la sesión decide la redirección.

---

## 5. ASOCIACIÓN DE REGISTROS CON EL UID

Patrón repetido en los ViewModels:

```kotlin
private fun obtenerUserId(): String? {
    return SupabaseClient.perfil?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id
}
```

**Archivos que lo usan:** `CreateTicketViewModel.kt:99-101`, `UserTicketsViewModel.kt:72-74`, `TicketDetailViewModel.kt:112-114`, `UserDashboardViewModel.kt`, `UserProfileViewModel.kt`.

**Asociación en escritura:** `CreateTicketViewModel.crearTicket()` incluye `put("profile", JsonPrimitive(userId))` en el payload del INSERT (línea 145), donde `userId` proviene del UID de Supabase Auth.

**Asociación en lectura:**

- `UserTicketsViewModel` filtra `eq("profile", userId)` (línea 99).
- `TicketDetailViewModel` aplica `eq("profile", userId)` solo cuando el usuario no es admin (líneas 72–74).
- `UserDashboardViewModel.contarEstado()` filtra métricas por `eq("profile", userId)` (línea 175).
- Las consultas de admin (`AdminTicketsViewModel`, `AdminHistoryViewModel`, `AdminUsersViewModel`, `AdminDashboardViewModel`, `AdminReportsViewModel`) son globales sin filtro de usuario.

**Verificación de identidad:** `LoginViewModel` valida que `perfil.id == usuario.id` antes de navegar, mitigando el riesgo de caché de perfil desactualizada.

---

## 6. CREACIÓN, CONSULTA Y ACTUALIZACIÓN DE TICKETS

Todas las operaciones usan PostgREST sobre la tabla `tickets`.

| Operación | ViewModel / Método | Evidencia |
|-----------|--------------------|-----------|
| INSERT | `CreateTicketViewModel.crearTicket()` | `postgrest.from("tickets").insert(JsonArray(listOf(payload))) { select() }` (líneas 151–154); campos: `equipo`, `tipo_incidencia`, `descripcion`, `prioridad`, `profile`, `evidencia_url` |
| SELECT (usuario) | `UserTicketsViewModel.cargarTicketsInternal()` | `select` con `filter { eq("profile", userId); eq("estado", filtro)? }`, `order created_at DESC` (líneas 97–106) |
| SELECT (admin) | `AdminTicketsViewModel.cargarTicketsInternal()` | `select` global con filtros `estado`/`prioridad`, orden DESC (líneas 102–114) |
| SELECT (detalle) | `TicketDetailViewModel.cargarTicket()` | `filter { eq("id", ticketId); if (!esAdmin) eq("profile", userId) }`, `limit(1)` (líneas 69–78) |
| SELECT (historial) | `AdminHistoryViewModel.consultarPagina()` | `order created_at DESC` + `range(offset, offset + PAGE_SIZE - 1)` con `PAGE_SIZE = 50` (líneas 91–98) |
| UPDATE estado | `AdminTicketsViewModel.actualizarEstadoTicket()` | `update(payload){ estado }` con `filter { eq("id", ticketId) }` (líneas 134–143) |
| UPDATE prioridad | `AdminTicketsViewModel.actualizarPrioridadTicket()` | `update(payload){ prioridad }` por ID (líneas 176–184) |
| UPDATE completo | `AdminTicketsViewModel.guardarEdicion()` | `update` de equipo/tipo/descripcion/prioridad/estado (líneas 254–267) |
| DELETE | `AdminTicketsViewModel.eliminarTicket()` | `delete { filter { eq("id", ticketId) } }` (líneas 298–303) |
| COUNT métricas | `UserDashboardViewModel.contarEstado()`, `AdminDashboardViewModel`, `AdminReportsViewModel` | `select { count(Count.EXACT); filter {...}; limit(0) }` |

**Campos del modelo `tickets` (inferidos del código):** `id` (Long), `equipo`, `tipo_incidencia`, `descripcion`, `prioridad` (`Baja|Media|Alta|Critica`), `estado` (`Pendiente|En proceso|Solucionado`), `created_at`, `profile` (FK lógica a `profiles.id`), `evidencia_url` (nullable).

**Manejo de errores:** todos los ViewModels usan `try/catch` con `Log.e` y mensajes `errorMessage` en español; `UserTicketsViewModel` distingue `CancellationException` (línea 108).

---

## 7. PERSISTENCIA DE INFORMACIÓN

- **Datos relacionales:** PostgreSQL vía PostgREST (Supabase). No existe base de datos local (Room/SQLite).
- **Sesión:** el SDK de Supabase Auth persiste el token de sesión; `comprobarSesion()` la restaura al abrir la app.
- **Perfil en memoria:** `SupabaseClient.perfil` es solo caché de sesión; se pierde al cerrar la app y se recarga desde `profiles` tras el login. `UserProfileViewModel.cargarPerfil()` vuelve a consultar PostgreSQL si la caché está vacía (líneas 99–154).
- **Evidencias:** archivos JPEG subidos a Storage y URL pública guardada en `tickets.evidencia_url`.
- **Verificación pendiente:** que los datos persistan realmente tras cerrar/reabrir la app debe probarse en dispositivo (las consultas son siempre remotas, por lo que la persistencia depende del backend, no del cliente).

---

## 8. INTEGRACIÓN CON SUPABASE STORAGE

**Implementación:** `CreateTicketViewModel.crearTicket()` (líneas 131–138).

```kotlin
val path = "evidencia/${System.currentTimeMillis()}.jpg"
val bucket = SupabaseClient.client.storage.from("foto_ticket")
bucket.upload(path, file)
bucket.publicUrl(path).toString()
```

- Bucket utilizado: `foto_ticket`.
- La foto es opcional; si existe se sube antes del INSERT y su URL se incluye en `evidencia_url`.
- `TicketDetailScreen`/`TicketDetail` exponen `evidenciaUrl` para mostrar la evidencia.
- **Pendiente de verificación manual:** que el bucket exista, sus políticas de escritura/lectura y que la URL pública sea accesible.

---

## 9. SEGURIDAD Y POLÍTICAS RLS

**Lo que el código evidencia:**

- Solo se usa la publishable key (`sb_publishable_...`); no se encontraron referencias a `service_role` ni claves secretas en el código fuente (las coincidencias de "service_role" aparecen únicamente en documentación).
- Las consultas de usuario filtran por `profile = userId` del lado cliente.
- `TicketDetailViewModel` exime a admin del filtro por usuario basándose en `SupabaseClient.perfil?.rol`.
- `AdminUsersViewModel` registra en logs que una respuesta vacía puede deberse a "posible denegación RLS" (líneas 85–87, 98–99), lo que indica conciencia del tema pero no verificación.
- LoginViewModel valida coincidencia UID auth↔perfil y rol antes de conceder navegación.

**Lo que NO puede verificarse desde el código:**

- Las políticas Row Level Security reales de las tablas `profiles` y `tickets` en Supabase.
- Los triggers que crean perfiles.
- La configuración del proveedor Google en Supabase Auth.
- El bucket `foto_ticket` y sus permisos.

**Conclusión de seguridad:** la integridad de los datos depende de que existan políticas RLS del tipo `USING (auth.uid() = profile)` (y equivalentes para admin). **Esto requiere verificación manual en Supabase Dashboard → Authentication/Database → Policies.** No debe afirmarse que RLS está configurado sin evidencia externa.

---

## 10. TESTS AUTOMATIZADOS EXISTENTES

### 10.1 Pruebas unitarias (`app/src/test/`)

| Archivo | Contenido | Qué prueba realmente |
|---------|-----------|----------------------|
| `ExampleUnitTest.kt` | `addition_isCorrect()` | Prueba trivial de JUnit (`2+2=4`); no toca código del proyecto |
| `AuthenticationLogicTest.kt` | 14 tests | Reimplementa en el propio test la lógica de validación de roles (`rol == "admin" || "usuario"`), longitud de contraseña y comparación de UIDs; **no invoca `LoginViewModel` ni `RegisterViewModel`** |
| `ProfileValidationTest.kt` | ~16 tests | Valida reglas de nombre/correo usando un regex propio y `trim()`; **no usa `Patterns.EMAIL_ADDRESS` ni el código real de `UserProfileViewModel`** |
| `TicketModuleUnitTest.kt` | 2 tests | Verifica el mapeo `"Crítica" → "Critica"` y la lista de prioridades; lógica duplicada dentro del test |
| `AdminModuleUnitTest.kt` | 4 tests | Instancia `AdminTicketItem`/`AdminEquipoItem` y verifica valores por defecto y constantes de filtros |

**Resultado de ejecución:** `./gradlew.bat testDebugUnitTest` → `BUILD SUCCESSFUL` (ejecutado el 7-nov-2026, 23 s). Todas las pruebas pasan.

**Limitación importante:** estas pruebas verifican lógica autocontenida en el propio archivo de test (condiciones reescritas), no el comportamiento de las clases de producción. No hay tests de ViewModels, ni mocks de Supabase, ni pruebas de integración.

### 10.2 Pruebas instrumentadas (`app/src/androidTest/`)

| Archivo | Contenido |
|---------|-----------|
| `ExampleInstrumentedTest.kt` | `useAppContext()` — solo verifica que `packageName == "com.devcore.ticketflow"` |

No existen pruebas instrumentadas de flujos de autenticación, navegación ni Supabase.

---

## 11. PRUEBAS DE INTEGRACIÓN VERIFICADAS

**No existen pruebas de integración automatizadas** que ejecuten llamadas reales contra Supabase. No hay tests que invoquen `signInWith`, `signUpWith`, PostgREST o Storage.

Las siguientes integraciones están **implementadas en código** pero su verificación real requiere ejecución en dispositivo/emulador con credenciales configuradas:

| Integración | Estado del código | Verificación real |
|-------------|-------------------|-------------------|
| Auth email/password (signUp/signIn) | Implementado | Pendiente E2E |
| Google OAuth + deep link | Implementado | Pendiente E2E + config externa |
| SELECT/INSERT/UPDATE/DELETE tickets | Implementado | Pendiente E2E |
| SELECT/UPDATE profiles | Implementado | Pendiente E2E |
| Storage upload `foto_ticket` | Implementado | Pendiente E2E |
| Restauración de sesión | Implementado | Pendiente E2E |

---

## 12. PRUEBAS MANUALES PENDIENTES DE EJECUCIÓN

### En Supabase Dashboard

1. **Authentication → Providers:** verificar que el proveedor Google esté habilitado y con Client ID/Secret configurados.
2. **Authentication → URL Configuration:** confirmar que `ticketflow://auth/callback` esté en Redirect URLs.
3. **Database → Tables:** confirmar estructura de `profiles` (id, nombre, correo, rol, fecha_creacion) y `tickets` (id, equipo, tipo_incidencia, descripcion, prioridad, estado, created_at, profile, evidencia_url).
4. **Database → Policies (RLS):** verificar políticas de SELECT/INSERT/UPDATE/DELETE para ambas tablas.
5. **Database → Triggers:** confirmar trigger que crea `profiles` al registrarse en Auth (necesario tanto para email como para Google OAuth).
6. **Storage:** confirmar bucket `foto_ticket` y sus políticas.

### En Google Cloud Console

7. **OAuth 2.0 Client ID:** verificar Authorized redirect URI `https://rsqayszvxrcgvylquzog.supabase.co/auth/v1/callback` (única URI requerida para este flujo).

### En el dispositivo/emulador Android

8. Registro con email/contraseña válidos → verificar fila en `auth.users` y `profiles`.
9. Registro con email inválido, campos vacíos, contraseñas que no coinciden → verificar mensajes de error.
10. Login correcto → verificar navegación al dashboard según rol.
11. Login con credenciales incorrectas y correo no confirmado → verificar mensajes.
12. Cerrar sesión → verificar retorno a login sin acceso a pantallas protegidas.
13. Cerrar y reabrir la app con sesión activa → verificar restauración automática.
14. Google OAuth completo: botón "Continuar con Google" → navegador → consentimiento → retorno por deep link → sesión → perfil → dashboard.
15. Cancelación del flujo OAuth → verificar mensaje y estado de carga.
16. Crear ticket con y sin foto → verificar fila en `tickets`, objeto en Storage y `evidencia_url`.
17. Listar tickets como usuario → verificar que solo aparecen los propios.
18. Como admin: cambiar estado/prioridad, editar y eliminar tickets → verificar persistencia en PostgreSQL.
19. Editar nombre de perfil → verificar UPDATE en `profiles`.
20. Dashboard/reportes/historial → verificar que las métricas corresponden a datos reales.

---

## 13. TABLA RESUMEN DE PRUEBAS

| Prueba | Tipo | Evidencia encontrada | Resultado |
|--------|------|----------------------|-----------|
| Registro email/password | Implementación en código | `RegisterViewModel.kt:179` `signUpWith(Email)` | Implementado; E2E pendiente |
| Login email/password | Implementación en código | `LoginViewModel.kt:133` `signInWith(Email)` | Implementado; E2E pendiente |
| Google OAuth | Implementación en código | `LoginViewModel.kt:180-183`, `MainActivity.kt:57`, Manifest intent-filter | Implementado; E2E y config externa pendientes |
| Cierre de sesión | Implementación en código | `AppNavigation.kt:46` `auth.signOut()` | Implementado; E2E pendiente |
| Persistencia de sesión | Implementación en código | `LoginViewModel.kt:371-372` `awaitInitialization`/`currentUserOrNull` | Implementado; E2E pendiente |
| Asociación UID | Implementación en código | `obtenerUserId()` en 5 ViewModels; `eq("profile", userId)` | Implementado; E2E pendiente |
| INSERT tickets | Implementación en código | `CreateTicketViewModel.kt:152-154` | Implementado; E2E pendiente |
| SELECT tickets | Implementación en código | `UserTicketsViewModel.kt:97`, `AdminTicketsViewModel.kt:102`, `AdminHistoryViewModel.kt:93` | Implementado; E2E pendiente |
| UPDATE tickets | Implementación en código | `AdminTicketsViewModel.kt:139,181,263` | Implementado; E2E pendiente |
| DELETE tickets | Implementación en código | `AdminTicketsViewModel.kt:299-300` | Implementado; E2E pendiente |
| UPDATE profiles | Implementación en código | `UserProfileViewModel.kt:194-195`, `AdminProfileViewModel.kt:146-147` | Implementado; E2E pendiente |
| Storage upload | Implementación en código | `CreateTicketViewModel.kt:134-136` bucket `foto_ticket` | Implementado; E2E pendiente |
| RLS policies | No verificable en código | Solo comentarios en `AdminUsersViewModel.kt:86,99` | NO VERIFICADO — requiere Dashboard |
| Trigger creación de perfil | No verificable en código | Comentario en `RegisterViewModel.kt:186` | NO VERIFICADO — requiere Dashboard |
| Tests unitarios | Automatizado | 5 archivos en `app/src/test/` | Ejecutan y pasan; no prueban código de producción |
| Tests instrumentados | Automatizado | `ExampleInstrumentedTest.kt` | Solo verifica packageName |
| Tests de integración Supabase | No existen | — | NO IMPLEMENTADO |
| Compilación | Automatizado | `gradlew clean compileDebugKotlin` → BUILD SUCCESSFUL | Verificado (52 s, 3 warnings menores) |

---

## 14. RESULTADOS Y LIMITACIONES

**Resultados comprobados:**

- `./gradlew.bat clean compileDebugKotlin` → BUILD SUCCESSFUL (3 warnings: `menuAnchor()` deprecado, dos `Json` redundantes).
- `./gradlew.bat testDebugUnitTest` → BUILD SUCCESSFUL; los tests unitarios existentes pasan.
- Búsqueda de `service_role`/`secret` en código fuente: sin coincidencias (solo documentación).

**Limitaciones:**

1. No hay pruebas de integración automatizadas contra Supabase.
2. Los tests unitarios existentes verifican lógica duplicada en el propio test, no el código de producción.
3. Las políticas RLS, triggers, configuración del proveedor Google y permisos del bucket no son verificables desde el repositorio.
4. El flujo OAuth completo (navegador → Google → Supabase callback → deep link → sesión) no ha sido ejecutado.
5. La persistencia real de datos requiere ejecución en dispositivo con backend activo.
6. No existe capa Repository ni mocks del cliente Supabase, lo que dificulta pruebas unitarias reales de los ViewModels.

---

## 15. CONCLUSIÓN

TicketFlow Mobile tiene una **integración Supabase completa a nivel de código**: autenticación email/password, Google OAuth con deep link, CRUD de tickets con asociación por UID, gestión de perfiles, Storage para evidencias y consultas de métricas. Sin embargo, **la cobertura de pruebas es débil**: los tests unitarios existentes son autocontenidos y no ejercitan el código de producción, y no existen pruebas de integración ni instrumentadas de los flujos reales.

Para considerar la Parte 5 validada integralmente se requiere:

1. Ejecutar el checklist manual de la sección 12 (Dashboard, Google Cloud, dispositivo).
2. Verificar RLS y triggers en Supabase.
3. Probar Google OAuth E2E una vez configurado el proveedor.
4. A futuro, añadir tests que invoquen la lógica real (idealmente extrayendo validaciones a funciones testeables e introduciendo una capa Repository con fake para tests).

**Estado: implementación completa en código; verificación integral pendiente de ejecución manual.**
