# AUDITORÍA TÉCNICA INTEGRAL — TICKETFLOW MOBILE
## Verificación de las Partes 3 y 4 e Integración con las Partes 1 y 2
**Proyecto:** TicketFlow Mobile (Android Nativo)  
**Ruta auditada:** `C:\Users\aguil\Downloads\TicketFlow.Mobile-main (2)\TicketFlow.Mobile-main`  
**Fecha de auditoría:** 24 de noviembre de 2026  
**Auditor técnico:** Senior Android & Backend Auditor  
**Estado de la fase:** Exclusivamente de Auditoría e Inspección Técnica (código de producción intacto)  

---

## 1. RESUMEN EJECUTIVO

Se ha completado la auditoría técnica integral del proyecto **TicketFlow Mobile**, centrándose en la evaluación de la **Parte 3** (Base de datos PostgreSQL y persistencia) y la **Parte 4** (Integración de módulos, interfaz e historial), verificando de manera cruzada su coherencia con la **Parte 1** (Gestión principal de tickets) y la **Parte 2** (Módulos secundarios y experiencia de usuario).

### Estado General del Repositorio
- **Compilación estática:** Exitosa (`BUILD SUCCESSFUL in 58s`).
- **Pruebas unitarias locales:** 26 tareas ejecutadas, 0 fallos (`BUILD SUCCESSFUL`).
- **Arquitectura activa:** 100% Jetpack Compose con Material 3 y arquitectura MVVM mediante Navigation Compose en `MainActivity.kt`.
- **Integración con Supabase:** Totalmente operativa a través del SDK oficial de Kotlin (BOM 3.5.0) sobre las tablas `tickets` y `profiles`, y el bucket `foto_ticket`.

---

## 2. ARQUITECTURA GENERAL ENCONTRADA

```
com.devcore.ticketflow/
├── MainActivity.kt                → Activity única de Compose y punto de entrada Deep Links
├── SupabaseClient.kt              → Singleton de configuración Supabase (Auth, PostgREST, Storage, Realtime)
├── Profile.kt                     → Modelo serializable de la tabla profiles
├── ui/
│   ├── navigation/
│   │   ├── AppNavigation.kt       → NavHost con 14 destinos composable y helpers de barra inferior
│   │   └── NavRoutes.kt           → Constantes de rutas y generador ticketDetail(id)
│   ├── components/                → Componentes UI reutilizables (TicketCard, Badges, Botones, TextField, etc.)
│   ├── screens/
│   │   ├── auth/                  → LoginScreen, RegisterScreen (Auth)
│   │   ├── user/                  → UserDashboard, CreateTicket, UserTickets, TicketDetail, UserProfile
│   │   └── admin/                 → AdminDashboard, AdminTickets, AdminEditTicket, AdminTeams, AdminUsers, AdminReports, AdminHistory, AdminProfile
│   └── theme/                     → Colores corporativos, tipografía y tema Material 3
```

> **Nota sobre código legado:** Existen 8 Activities declaradas en `AndroidManifest.xml` (`AdminActivity`, `UserActivity`, `UserDashboardActivity`, etc.). Se comprobó que corresponden a versiones XML previas; la aplicación en ejecución opera 100% en Compose desde `MainActivity`.

---

## 3. AUDITORÍA DE LA PARTE 3: POSTGRESQL Y PERSISTENCIA DE INFORMACIÓN

### A. Estructura de la Base de Datos

| Tabla / Bucket | Tipo en Supabase | Columnas Comprobadas | Tipos / Constraints |
|---|---|---|---|
| `profiles` | Tabla PostgreSQL | `id`, `nombre`, `correo`, `rol`, `fecha_creacion` | `id` (uuid, PK, vinculada a `auth.users.id`), `rol` ('admin' \| 'usuario') |
| `tickets` | Tabla PostgreSQL | `id`, `equipo`, `tipo_incidencia`, `descripcion`, `prioridad`, `estado`, `created_at`, `profile`, `evidencia_url` | `id` (bigint/int8, PK auto), `prioridad` ('Baja'\|'Media'\|'Alta'\|'Critica'), `estado` ('Pendiente'\|'En proceso'\|'Solucionado'), `profile` (uuid FK) |
| `foto_ticket` | Storage Bucket | `evidencia/{timestamp}.jpg` | Archivos JPEG subidos como evidencias fotográficas |

**Relaciones comprobadas:**
1. `tickets.profile` $\to$ `profiles.id` (Relación 1 a N: cada ticket pertenece a un usuario).
2. `profiles.id` $\to$ `auth.users.id` (Relación 1 a 1: cada perfil corresponde a una identidad de Supabase Auth).

**Discrepancias identificadas en modelos Kotlin:**
- No existe un único modelo de Ticket en la capa de datos. Cada ViewModel declara su propio modelo local (`TicketItem`, `TicketDetail`, `AdminTicketItem`, `DashboardTicket`, `ReporteTicketRow`).
- En `UserTicketsViewModel.kt`, el modelo `TicketItem` define propiedades con guion bajo (`tipo_incidencia`, `created_at`), mientras que en `AdminTicketsViewModel.kt` se utiliza camelCase con anotaciones `@SerialName("tipo_incidencia")`. Ambas compilan y funcionan, pero existe falta de unificación en la capa de datos.

### B. Operaciones CRUD Reales

| Operación | Tabla | Módulo / Archivo | Filtros / Identificadores | Evidencia en Código |
|---|---|---|---|---|
| **INSERT** | `tickets` | `CreateTicketViewModel.kt:166` | Asocia `profile` = UID y retorna `{ select() }` | `.insert(JsonArray(listOf(payload))) { select() }` |
| **SELECT** | `tickets` | `UserTicketsViewModel.kt:97` | `eq("profile", userId)` + `estado` opcional | `.select { filter { eq("profile", userId) } }` |
| **SELECT** | `tickets` | `AdminTicketsViewModel.kt:83` | `estado` y `prioridad` opcionales | `.select { filter { ... } order("created_at", DESC) }` |
| **SELECT** | `tickets` | `TicketDetailViewModel.kt:69` | `eq("id", ticketId)` + `eq("profile", userId)` si no es admin | `.select { filter { eq("id", ticketId) ... } limit(1) }` |
| **SELECT** | `tickets` | `AdminHistoryViewModel.kt:97` | Paginación por lotes de 50 | `.select { order("created_at", DESC) range(offset, offset + 49) }` |
| **UPDATE** | `tickets` | `AdminTicketsViewModel.kt:125` | `filter { eq("id", ticketId) }` | Actualiza estado (`put("estado", ...)` ) |
| **UPDATE** | `tickets` | `AdminTicketsViewModel.kt:167` | `filter { eq("id", ticketId) }` | Actualiza prioridad (`put("prioridad", ...)` ) |
| **UPDATE** | `tickets` | `AdminTicketsViewModel.kt:254` | `filter { eq("id", editingTicket.id) }` | Edición completa (equipo, tipo, desc, prio, estado) |
| **DELETE** | `tickets` | `AdminTicketsViewModel.kt:295` | `filter { eq("id", ticketId) }` | `.delete { filter { eq("id", ticketId) } }` |
| **SELECT** | `profiles` | `AdminUsersViewModel.kt:77` | `rol` opcional, ordenado por nombre | `.select { filter { ... } order("nombre", ASC) }` |
| **UPDATE** | `profiles` | `UserProfileViewModel.kt:194` | `filter { eq("id", usuarioId) }` | Actualiza únicamente el campo `nombre` |

### C. Asociación al UID Autenticado
- El UID se extrae de `SupabaseClient.perfil?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id`.
- Los tickets insertados en `CreateTicketViewModel` inyectan obligatoriamente `put("profile", JsonPrimitive(userId))`.
- Las consultas de tickets de usuario (`UserTicketsViewModel`, `UserDashboardViewModel`) imponen el filtro estricto `eq("profile", userId)`.
- Si el UID es nulo, los ViewModels detienen la operación y muestran el mensaje de error correspondiente.

### D. Row Level Security (RLS)
- El código cliente presupone que PostgreSQL tiene RLS habilitado y que existen políticas que:
  1. Permiten a los usuarios estándar consultar únicamente sus propios tickets (`profile = auth.uid()`).
  2. Permiten a los usuarios estándar crear tickets asignados a su propio `auth.uid()`.
  3. Permiten a los administradores (`rol = 'admin'`) consultar, actualizar y eliminar tickets de toda la organización.
  4. Permiten a los administradores consultar el directorio de perfiles completo en `AdminUsersScreen`.
- **Dictamen:** RLS reside en PostgreSQL. Si en el backend de Supabase las políticas no están configuradas correctamente, consultas como `AdminUsersViewModel` podrían retornar listas vacías sin arrojar una excepción visible. Esto requiere verificación manual en la consola de Supabase.

### E. Persistencia Real
- La aplicación recupera la información directamente de Supabase en cada apertura mediante `init` en los ViewModels y la persistencia de tokens de sesión de Supabase Auth en SharedPreferences.
- No existe base de datos local SQLite ni Room; la persistencia fuera de línea no está contemplada en la arquitectura actual.

### F. Supabase Realtime
- **Dictamen de Auditoría:** `install(Realtime)` está presente en `SupabaseClient.kt`, pero **NO existe ninguna implementación de tiempo real en toda la aplicación**.
- No hay llamadas a `client.channel()`, ni `.subscribe()`, ni `postgresChangeFlow`. La aplicación funciona bajo el modelo tradicional de solicitud/respuesta HTTP con pull-to-refresh y actualización local del StateFlow tras mutaciones.

---

## 4. AUDITORÍA DE LA PARTE 4: INTEGRACIÓN DE MÓDULOS

| Módulo | Pantalla / ViewModel | Fuente de Datos | Acciones Comprobadas | Estados Gestionados | Integración |
|---|---|---|---|---|---|
| **Tickets Usuario** | `UserTicketsScreen`<br>`UserTicketsViewModel` | Tabla `tickets` | Consultar, filtrar por estado, navegar a detalle | `isLoading`, `isRefreshing`, error banner, empty card | Integrado con Detalle y Creación |
| **Creación Tickets** | `CreateTicketScreen`<br>`CreateTicketViewModel` | `tickets` y Storage `foto_ticket` | Tomar foto, validar campos, insertar ticket | `isLoading` (bloqueo de botón), error banner, evento de éxito | Redirige al Detalle del ticket creado |
| **Detalle Ticket** | `TicketDetailScreen`<br>`TicketDetailViewModel` | Tabla `tickets` | Consulta por ID, visualización de foto Coil, refrescar | `isLoading`, error banner con reintento, visualización | Aislado por rol si no es admin |
| **Gestión Admin** | `AdminTicketsScreen`<br>`AdminTicketsViewModel` | Tabla `tickets` | Cambio rápido estado/prio, edición completa, eliminación | `updatingTicketId`, `isRefreshing`, banners éxito/error | Integrado con `AdminEditTicketScreen` |
| **Directorio Usuarios** | `AdminUsersScreen`<br>`AdminUsersViewModel` | Tabla `profiles` | Búsqueda por nombre/correo, filtro por rol, recargar | `isLoading`, error con reintento, empty con limpiar | Datos reales de usuarios |
| **Inventario Equipos** | `AdminTeamsScreen`<br>`AdminTeamsViewModel` | Catálogo local + `tickets` | Cruce de catálogo con incidencias activas en BD | `isLoading`, `isRefreshing`, empty con limpiar | Calcula pendientes y totales reales |
| **Dashboards** | `AdminDashboardScreen`<br>`UserDashboardScreen` | `tickets` y `profiles` | Conteos exactos (`Count.EXACT`), accesos rápidos | `isLoading`, `isRefreshing`, error banner | Conecta con todos los módulos |
| **Reportes** | `AdminReportsScreen`<br>`AdminReportsViewModel` | Tabla `tickets` | Estadísticas por prioridad, estado, equipo y tipo | `isLoading`, error con reintento, barras progreso | Cálculo analítico en cliente |
| **Historial** | `AdminHistoryScreen`<br>`AdminHistoryViewModel` | Tabla `tickets` | Paginación (50 en 50), búsqueda, filtros estado | `isLoading`, `isLoadingMore`, empty, endReached | Conecta al Detalle del ticket |

---

## 5. AUDITORÍA DEL HISTORIAL DE REGISTROS

Se auditó minuciosamente el funcionamiento de `AdminHistoryScreen` y `AdminHistoryViewModel`:
1. **Origen de datos:** Consulta real a PostgreSQL sobre la tabla `tickets` con ordenamiento `order("created_at", Order.DESCENDING)`.
2. **Paginación:** Implementada correctamente por bloques de 50 registros (`range(offset, offset + 49)`) y botón reactivo para cargar más registros antiguos.
3. **Búsqueda y filtros:** Búsqueda en memoria por ID, equipo, tipo, descripción, estado y prioridad, junto con chips de estado (`Pendientes`, `En proceso`, `Solucionados`).
4. **Diferenciación conceptual crítica:**
   - **Lo que SÍ es:** Un historial cronológico de los tickets registrados en la base de datos desde su creación.
   - **Lo que NO es:** No es un log de auditoría de modificaciones o transiciones de estado. La base de datos no cuenta con una tabla de eventos históricos (`ticket_audit_log`); por tanto, cuando un ticket cambia de "Pendiente" a "Solucionado", solo se almacena su estado vigente.

---

## 6. AUDITORÍA DE NAVEGACIÓN E INTERFAZ

### Navegación (Navigation Compose)
- **Rutas centralizadas:** [AppNavigation.kt](file:///C:/Users/aguil/Downloads/TicketFlow.Mobile-main%20(2)/TicketFlow.Mobile-main/app/src/main/java/com/devcore/ticketflow/ui/navigation/AppNavigation.kt) registra 14 rutas válidas en `NavRoutes.kt`. No existen rutas huérfanas ni destinos inaccesibles.
- **Backstack limpio:** Las funciones `navigateAdmin` y `navigateUser` aplican `launchSingleTop = true`, evitando que pulsaciones repetidas en la barra inferior acumulen pantallas duplicadas.
- **Protección de acceso:** En `LoginViewModel`, se evalúa el rol del usuario tras autenticar (`admin` vs `usuario`) para dirigir al dashboard correspondiente.

### Componentes Visuales y Material 3
- Componentes en `ui/components/`:
  - `TicketCard.kt`: Tarjeta reutilizable con badges semánticos y fecha formateada a la zona horaria local.
  - `Badges.kt`: `PriorityBadge`, `StatusBadge`, `RoleBadge` con colores normalizados.
  - `TicketFlowButton.kt`, `TicketFlowTextField.kt`, `TicketFlowLoading.kt`, `TicketFlowErrorBanner.kt`.
- **Referencia a Clase 13:** No se encontró ningún documento o archivo que describa la "Clase 13" en el repositorio. Los componentes siguen las directrices oficiales de Google para Jetpack Compose y Material 3.

---

## 7. CLASIFICACIÓN DE HALLAZGOS TÉCNICOS

### HALLAZGO 1: Credenciales de Supabase Hardcodeadas en Código
- **Gravedad:** **ALTA** (Riesgo de seguridad y desacoplamiento)
- **Ubicación:** `SupabaseClient.kt` (Líneas 14-15)
- **Evidencia:** `supabaseUrl = "https://rsqayszvxrcgvylquzog.supabase.co"`, `supabaseKey = "sb_publishable_cCCulG6WfHEr-BRaG36bBQ_3rTbTL3Z"`.
- **Consecuencia:** Las credenciales están expuestas directamente en el código fuente. Aunque se trata de una clave anon/publishable, la buena práctica exige leerlas desde `BuildConfig.SUPABASE_URL` y `BuildConfig.SUPABASE_KEY` configuradas mediante `local.properties`.
- **Responsable:** Parte 1 / Integrante 1.

### HALLAZGO 2: Supabase Realtime Configurado pero No Implementado
- **Gravedad:** **MEDIA** (Inconsistencia técnica / Documentación)
- **Ubicación:** `SupabaseClient.kt` (Línea 23)
- **Evidencia:** `install(Realtime)` está configurado en el cliente, pero no hay ningún `channel()`, `subscribe()` ni `postgresChangeFlow` en ningún ViewModel.
- **Consecuencia:** La aplicación no actualiza datos en tiempo real si otro usuario realiza cambios concurrentes; depende de recargas manuales (pull-to-refresh).
- **Responsable:** Parte 3 / Integrante 3.

### HALLAZGO 3: Actividades Legacy en AndroidManifest.xml
- **Gravedad:** **MEDIA** (Deuda técnica y código muerto)
- **Ubicación:** `AndroidManifest.xml` (Líneas 36-43)
- **Evidencia:** Declaración de `AdminActivity`, `UserActivity`, `UserDashboardActivity`, `UserTicketsActivity`, etc.
- **Consecuencia:** Actividades XML antiguas no utilizadas por Compose que incrementan el tamaño del APK y confunden la arquitectura del proyecto.
- **Responsable:** Parte 4 / Integrante 4.

### HALLAZGO 4: Falta de Unificación en Modelos de Datos de Tickets
- **Gravedad:** **BAJA** (Mantenibilidad)
- **Ubicación:** `UserTicketsViewModel.kt`, `TicketDetailViewModel.kt`, `AdminTicketsViewModel.kt`.
- **Evidencia:** `TicketItem` usa snake_case directo (`tipo_incidencia`), mientras que `AdminTicketItem` usa camelCase con `@SerialName`.
- **Consecuencia:** Dificulta el mantenimiento y la creación de una capa Repository compartida.
- **Responsable:** Parte 3 / Integrante 3.

### HALLAZGO 5: Advertencias de Compilación por Deprecación
- **Gravedad:** **BAJA** (Calidad de código)
- **Ubicación:** `CreateTicketScreen.kt:206` (`menuAnchor()` deprecado).
- **Consecuencia:** Deuda técnica menor frente a futuras versiones de Compose Material 3.
- **Responsable:** Parte 4 / Integrante 4.

---

## 8. PRUEBAS Y VERIFICACIONES TÉCNICAS EJECUTADAS

1. **Compilación Kotlin:**
   - Comando: `.\gradlew.bat compileDebugKotlin`
   - Resultado: `BUILD SUCCESSFUL` (0 errores de sintaxis o tipos).
2. **Pruebas Unitarias Automatizadas:**
   - Comando: `.\gradlew.bat testDebugUnitTest`
   - Resultado: `BUILD SUCCESSFUL in 58s` (26 tareas ejecutadas, todas las suites pasaron).
   - Cobertura de tests comprobada:
     - `TicketModuleUnitTest`: Mapeo de restricciones de BD (`Critica`), validación de campos obligatorios en creación y edición.
     - `AdminModuleUnitTest`: Validación de estados, prioridades, cálculo de equipos e inventario, búsqueda integral del historial y filtrado por estado.
     - `AuthenticationLogicTest`: Lógica de validación de contraseñas, correos y tokens.
     - `ProfileValidationTest`: Reglas de validación del perfil.

---

## 9. MATRIZ FINAL DE CUMPLIMIENTO

| Requisito / Área Auditada | Estado de Cumplimiento | Justificación Basada en Evidencia |
|---|---|---|
| **Estructura PostgreSQL (`tickets`, `profiles`)** | **Cumple** | Tablas y campos coinciden con las consultas PostgREST analizadas en código. |
| **Operaciones CRUD completas** | **Cumple** | INSERT, SELECT, UPDATE y DELETE verificados en los ViewModels respectivos. |
| **Asociación al UID autenticado** | **Cumple** | Uso consistente de `SupabaseClient.perfil?.id` y `auth.currentUserOrNull()?.id`. |
| **Políticas Row Level Security (RLS)** | **No verificable** | RLS se ejecuta en backend; el código asume permisos pero requiere verificación en Supabase Console. |
| **Persistencia tras reapertura** | **Cumple** | Persistencia de tokens en SharedPreferences y reconsulta de perfiles/tickets al inicio. |
| **Consultas en Tiempo Real (Realtime)** | **No cumple** | `install(Realtime)` está presente, pero no existe ninguna suscripción ni flujo activo. |
| **Integración de Módulos (Tickets, Usuarios, Equipos, Reportes)** | **Cumple** | Los 7 módulos se conectan con datos reales y no usan mocks estáticos. |
| **Historial de Registros Paginado** | **Cumple** | Consulta PostgreSQL paginada (`range`), búsqueda y filtros por estado funcionales. |
| **Historial de Auditoría de Cambios** | **No cumple** | Limitación de backend: PostgreSQL no cuenta con tabla de auditoría previa de estados. |
| **Navegación e Interfaz Material 3** | **Cumple** | 14 rutas activas, backstack protegido con `launchSingleTop`, componentes reutilizables sólidos. |
| **Pautas de Clase 13** | **No verificable** | No existe documentación de la Clase 13 en el repositorio. |

---

## 10. RECOMENDACIONES DE PRIORIZACIÓN PARA EL EQUIPO

1. **Prioridad 1 (Seguridad - Integrante 1):** Migrar las credenciales hardcodeadas en `SupabaseClient.kt` hacia `BuildConfig.SUPABASE_URL` y `BuildConfig.SUPABASE_KEY` leyendo de `local.properties`.
2. **Prioridad 2 (Backend / RLS - Integrante 3):** Acceder a la consola de Supabase y verificar las políticas RLS de `tickets` y `profiles` para garantizar que los administradores tengan permisos globales de lectura/escritura y los usuarios únicamente acceso a sus registros.
3. **Prioridad 3 (Limpieza de Arquitectura - Integrante 4):** Remover las 8 Activities legacy del `AndroidManifest.xml` para sanear el manifiesto y reducir el tamaño del build.
4. **Prioridad 4 (Evolutiva - Opcional):** Si la cátedra exige tiempo real, implementar un `channel` con `postgresChangeFlow` en `AdminTicketsViewModel` o `UserTicketsViewModel`; de lo contrario, documentar formalmente que el sistema opera bajo el patrón Request/Response con recarga manual.
