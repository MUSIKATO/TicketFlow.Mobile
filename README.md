# TicketFlow Mobile

## Descripción del Proyecto

TicketFlow Mobile es una aplicación Android nativa para la gestión de incidencias y tickets de soporte técnico en el contexto de Perfumería VIP. Permite a los usuarios reportar problemas de equipos y servicios adjuntando evidencia fotográfica, realizar seguimiento del estado de sus tickets y administrar su perfil, mientras que los administradores gestionan, priorizan y resuelven incidencias desde un panel con métricas, historial y reportes.

La aplicación utiliza **Supabase** como backend: autenticación (correo/contraseña y Google OAuth), base de datos PostgreSQL a través de PostgREST y almacenamiento de archivos en Supabase Storage.

## Objetivo del Sistema

TicketFlow Mobile busca proporcionar una solución integral para la gestión de tickets de soporte técnico, permitiendo:

- Reporte rápido de incidencias por parte de los usuarios, con evidencia fotográfica
- Seguimiento del estado de los tickets por parte de su creador
- Gestión administrativa de incidencias: cambio de estado, prioridad, edición y eliminación
- Visualización de métricas del sistema, historial de registros y reportes
- Clasificación de tickets por prioridades y estados
- Autenticación segura con Supabase Auth (email/password y Google OAuth)

## Tecnologías Utilizadas

- **Kotlin** — Lenguaje de programación principal
- **Android** — Plataforma móvil nativa
- **Jetpack Compose + Material 3** — Toolkit de interfaz de usuario declarativa
- **Navigation Compose** — Navegación entre pantallas mediante rutas centralizadas
- **MVVM (Model-View-ViewModel)** — Arquitectura principal (`ViewModel`, `StateFlow`, `SharedFlow`, `viewModelScope`)
- **Supabase Kotlin SDK (BOM 3.5.0)** — Backend como servicio:
  - `auth-kt` — Autenticación email/password, Google OAuth, sesiones y deep links
  - `postgrest-kt` — Consultas a PostgreSQL (tablas `profiles` y `tickets`)
  - `storage-kt` — Bucket `foto_ticket` para evidencias fotográficas
  - `realtime-kt` — Soporte de tiempo real del SDK
- **Ktor Client (Android 3.0.3)** — Cliente HTTP utilizado por el SDK de Supabase
- **kotlinx.serialization** — Serialización/deserialización de modelos y payloads JSON
- **Coil (2.6.0)** — Carga de imágenes remotas (evidencias) en Compose
- **ActivityResultContracts + FileProvider** — Captura de evidencias fotográficas con la cámara
- **Gradle** — Sistema de build y gestión de dependencias
- **GitHub** — Control de versiones y colaboración

## Arquitectura de la Aplicación

La aplicación sigue el patrón **MVVM** organizado en capas:

```
UI (Pantallas Compose)
   │  observa StateFlow / emite eventos
   ▼
ViewModels (lógica de presentación y acceso a datos)
   │  viewModelScope.launch + withContext(Dispatchers.IO)
   ▼
SupabaseClient (singleton: Auth + Postgrest + Storage)
   │
   ▼
Supabase (Auth, PostgreSQL, Storage)
```

- **Pantallas Compose** (`ui/screens/`): renderizan el estado de la interfaz y delegan los eventos del usuario al ViewModel.
- **ViewModels** (`ui/screens/*/*ViewModel.kt`): mantienen el estado de UI en `StateFlow`, ejecutan las operaciones contra Supabase y exponen eventos de navegación y errores.
- **Modelos** (`@Serializable`): `Profile.kt` para la tabla `profiles` y los modelos de ticket definidos junto a cada ViewModel.
- **Navegación**: `ui/navigation/AppNavigation.kt` define el `NavHost` con las rutas de `NavRoutes.kt`, incluyendo navegación por rol (`admin` / `usuario`) y limpieza del stack al cerrar sesión.
- **Cliente Supabase**: `SupabaseClient.kt` centraliza la configuración del cliente con los módulos Auth, Postgrest, Storage y Realtime.

## Interfaz de Usuario

La interfaz de la aplicación está implementada en **Jetpack Compose**:

- `MainActivity` es la Activity lanzadora y monta la interfaz con `setContent { TicketFlowTheme { AppNavigation() } }`.
- Todas las pantallas de usuario y administrador se encuentran en `ui/screens/{auth,user,admin}/` como funciones `@Composable`, junto a sus ViewModels.
- Los componentes reutilizables (botones, tarjetas, badges, banners de error y navegación inferior) están en `ui/components/`.
- El tema Material 3 se define en `ui/theme/`.

El proyecto también conserva Activities con layouts XML de etapas anteriores del desarrollo (`AdminActivity`, `UserActivity`, `UserDashboardActivity`, `UserTicketsActivity`, `UserProfileActivity`, `UserEditProfileActivity`, `UserCreateTicketActivity`, `UserTicketDetailActivity`) junto a sus layouts en `app/src/main/res/layout/`. Estas pantallas forman parte del historial del proyecto y no interfieren con el flujo de navegación actual.

## Funcionalidades Principales

### Autenticación y usuarios

- **Registro e inicio de sesión** con correo y contraseña mediante Supabase Auth, con validaciones de campos y mensajes de error descriptivos.
- **Inicio de sesión con Google (OAuth)** mediante el proveedor `Google` del SDK de Supabase.
- **Deep links de autenticación**: callback `ticketflow://auth/callback` procesado en `MainActivity` mediante `handleDeeplinks` del SDK.
- **Persistencia de sesión**: restauración automática al reabrir la aplicación.
- **Navegación por rol**: los usuarios `admin` acceden al panel administrativo y los `usuario` al panel de usuario.
- **Cierre de sesión** con limpieza del stack de navegación.
- **Perfil de usuario**: visualización y edición del nombre; el correo es gestionado por Supabase Auth.

### Usuario

- **Dashboard**: resumen de tickets propios por estado y accesos rápidos.
- **Creación de tickets**: equipo afectado, tipo de incidencia, descripción, prioridad y evidencia fotográfica opcional.
- **Evidencia fotográfica**: captura con la cámara, almacenamiento en Supabase Storage (bucket `foto_ticket`, ruta `evidencia/<timestamp>.jpg`) y URL pública asociada al ticket.
- **Consulta de tickets propios**: listado filtrado por el UID del usuario autenticado, con filtro por estado y orden cronológico.
- **Detalle de ticket**: información completa del ticket e imagen de evidencia.
- **Gestión de perfil**: edición del nombre del usuario.

### Administrador

- **Dashboard de métricas**: totales consultados a PostgreSQL (tickets totales, críticos, pendientes, en proceso, solucionados y usuarios registrados).
- **Gestión de tickets**: listado global con filtros por estado y prioridad, cambio de estado y prioridad, edición completa y eliminación.
- **Historial de registros**: consulta cronológica paginada (`range()` de PostgREST, páginas de 50 registros).
- **Gestión de usuarios**: consulta de los perfiles registrados en el sistema.
- **Gestión de equipos**: consulta del catálogo de equipos derivada de los tickets registrados.
- **Reportes**: estadísticas y métricas del sistema.
- **Gestión de perfil**: edición del perfil de administrador.

## Integración con Supabase

### Tablas utilizadas

| Tabla | Columnas utilizadas en el código | Relación |
|-------|----------------------------------|----------|
| `profiles` | `id`, `nombre`, `correo`, `rol`, `fecha_creacion` | `id` = UID de Supabase Auth |
| `tickets` | `id`, `equipo`, `tipo_incidencia`, `descripcion`, `prioridad`, `estado`, `created_at`, `profile`, `evidencia_url` | `profile` → `profiles.id` |

Las consultas se realizan con PostgREST (`postgrest.from("tickets" | "profiles")`) e incluyen filtros `eq`, ordenamiento `order` y paginación `range`. El modelo `Profile` está en `Profile.kt`; los modelos de ticket se definen junto a cada ViewModel.

### Supabase Storage

- **Bucket**: `foto_ticket`
- **Estructura**: objetos en `evidencia/<timestamp>.jpg`
- **Flujo**: la cámara guarda la foto en el almacenamiento temporal de la app, el ViewModel la sube con `bucket.upload(path, file)`, obtiene `bucket.publicUrl(path)` y la persiste en `tickets.evidencia_url`.
- **Visualización**: `AsyncImage` de Coil en el detalle del ticket.

### Integración de Deep Links

`MainActivity` procesa los intents `ticketflow://auth/callback` (declarado en `AndroidManifest.xml` con `autoVerify`) y delega la sesión al SDK mediante `handleDeeplinks`, con deduplicación de intents para evitar procesar dos veces el mismo callback. Es el mecanismo de retorno del flujo de Google OAuth.

## Estructura del Proyecto

```
TicketFlow.Mobile-main/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/devcore/ticketflow/
│   │   │   │   ├── MainActivity.kt            # Entry point Compose + deep links OAuth
│   │   │   │   ├── SupabaseClient.kt          # Singleton: Auth + Postgrest + Storage + Realtime
│   │   │   │   ├── Profile.kt                 # Modelo de la tabla profiles
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/            # AppNavigation (NavHost) + NavRoutes
│   │   │   │   │   ├── components/            # Botones, tarjetas, badges, banners (Compose)
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── auth/              # Login/Register: Screen + ViewModel
│   │   │   │   │   │   ├── user/              # Dashboard, Tickets, Crear, Detalle, Perfil
│   │   │   │   │   │   └── admin/             # Dashboard, Tickets, Users, Teams, Profile, History, Reports
│   │   │   │   │   └── theme/                 # Tema Material 3 (Color, Type, Theme)
│   │   │   │   ├── console/                   # Consola de escritorio (POO: servicios, CRUD, modelos)
│   │   │   │   └── *Activity.kt               # Activities con layouts XML del desarrollo previo
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/layout/                    # Layouts XML de las Activities anteriores
│   │   ├── test/                              # Pruebas unitarias JUnit
│   │   └── androidTest/                       # Pruebas instrumentadas
│   ├── build.gradle.kts                        # Dependencias y configuración BuildConfig
│   └── errores.txt                             # Log de errores de la consola
├── docs/                                       # Documentación técnica del proyecto
├── build.gradle.kts
├── settings.gradle.kts
├── local.properties                            # SDK y credenciales Supabase (no versionado)
└── local.properties.example                    # Plantilla de credenciales
```

## Estados y Prioridades

### Estados de los tickets

- **Pendiente**: Ticket creado pero no atendido
- **En proceso**: Ticket siendo trabajado por el equipo de soporte
- **Solucionado**: Ticket resuelto y cerrado

### Prioridades

- **Baja**: Incidencia menor, no afecta operaciones críticas
- **Media**: Incidencia moderada, requiere atención pero no urgente
- **Alta**: Incidencia importante, afecta operaciones significativas
- **Crítica**: Incidencia crítica, requiere atención inmediata

## Validaciones

- Validación de correo con `Patterns.EMAIL_ADDRESS` y contraseña mínima de 6 caracteres en el registro.
- Confirmación de contraseña en registro y en el formulario de edición de perfil.
- Validación de campos obligatorios en la creación de tickets (equipo, tipo de incidencia, descripción y prioridad).
- Validación de UID: el `id` del perfil recuperado debe coincidir con el usuario autenticado.
- Validación de roles: los roles válidos son `admin` y `usuario`.
- Manejo de permisos de cámara para la captura de evidencias.

## Manejo de Excepciones

- `try/catch` con `Log.e` en todas las llamadas a Supabase dentro de los ViewModels.
- `CancellationException` se re-lanza donde corresponde para respetar la cancelación de corrutinas.
- Los errores de autenticación se traducen a mensajes amigables mostrados en la interfaz mediante `TicketFlowErrorBanner`.
- Los mensajes de error se muestran en español sin exponer detalles técnicos.

## Pruebas

- **Pruebas unitarias JUnit** en `app/src/test/` que cubren las reglas de autenticación, validación de perfil y lógica de tickets y módulos administrativos.
- **Prueba instrumentada** en `app/src/androidTest/`.
- La integración con Supabase se verifica en ejecución real contra el backend.

## Ejecución de la Aplicación

### Requisitos previos

- Android Studio (versión reciente con soporte para Kotlin y Jetpack Compose)
- Android SDK configurado
- Archivo `local.properties` en la raíz del proyecto con la ruta del SDK y las credenciales de Supabase:

```properties
sdk.dir=C:\\Users\\<usuario>\\AppData\\Local\\Android\\Sdk
supabase.url=https://<tu-proyecto>.supabase.co
supabase.key=<tu-clave-publicable>
```

Puede copiarse y completarse a partir de `local.properties.example`.

### Compilar la aplicación

```bash
.\gradlew.bat assembleDebug
```

### Compilar solo el código Kotlin

```bash
.\gradlew.bat compileDebugKotlin
```

### Ejecutar las pruebas unitarias

```bash
.\gradlew.bat testDebugUnitTest
```

### Ejecutar la aplicación

Conectar un dispositivo Android o iniciar un emulador y ejecutar el proyecto desde Android Studio (`Run 'app'`). La Activity lanzadora es `MainActivity`.

## Consola de Escritorio (Etapa 2)

El proyecto incluye una consola de línea de comandos en el paquete `console/` que implementa la lógica de dominio en memoria (POO) con servicios CRUD, modelos y reportes, utilizada para la evaluación académica de la arquitectura backend previa a la interfaz gráfica.

### Ejecución de la Consola

```bash
.\gradlew.bat runConsole -q --console=plain
```

### Flujo de prueba de la consola

1. Iniciar sesión como administrador o usuario en la CLI.
2. Crear un ticket con validaciones en memoria.
3. Actualizar, listar y eliminar tickets.
4. Cambiar estados (Pendiente, En Proceso, Solucionado).
5. Generar un reporte dinámico y consultar `errores.txt` en caso de fallo.

Los datos de la consola se mantienen en memoria durante la ejecución y no persisten entre ejecuciones.

## Estado del Proyecto

- **Aplicación Android (Compose)**: Interfaz implementada en Jetpack Compose con arquitectura MVVM. Compila correctamente (`compileDebugKotlin`, `assembleDebug`, `testDebugUnitTest`).
- **Autenticación Supabase**: Email/password y Google OAuth con deep links.
- **Persistencia**: PostgreSQL (PostgREST) para `profiles` y `tickets`; Supabase Storage para evidencias fotográficas.
- **Módulo Usuario**: Dashboard, Tickets, Creación con evidencia, Detalle y Perfil.
- **Módulo Administrador**: Dashboard de métricas, Gestión de Tickets, Historial, Usuarios, Equipos, Reportes y Perfil.
- **Consola Etapa 2**: Funcional, con servicios CRUD y reportes en memoria.

## Notas Importantes

- La aplicación Android utiliza **Supabase** para la persistencia real de datos.
- Las credenciales de Supabase se configuran en `local.properties` (`supabase.url`, `supabase.key`), archivo que no se versiona; consultar `local.properties.example` como plantilla.
- Las políticas RLS, los triggers y la configuración del proveedor Google OAuth se gestionan desde el panel de Supabase.
- El proyecto cuenta con documentación técnica adicional en la carpeta `docs/`.
