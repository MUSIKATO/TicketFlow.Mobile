# TicketFlow Mobile

## Descripción del Proyecto

TicketFlow Mobile es una aplicación Android para la gestión de incidencias y tickets de soporte técnico. Permite a los usuarios reportar problemas de equipos y servicios, mientras que los administradores pueden gestionar, priorizar y resolver estas incidencias de manera eficiente.

## Objetivo del Sistema

El objetivo de TicketFlow Mobile es proporcionar una solución integral para la gestión de tickets de soporte técnico, permitiendo:
- Reporte rápido de incidencias por parte de los usuarios
- Seguimiento del estado de los tickets
- Gestión administrativa de incidencias, usuarios y equipos
- Generación de reportes y estadísticas
- Clasificación por prioridades y estados

## Tecnologías Utilizadas

- **Kotlin** - Lenguaje de programación principal
- **Android** - Plataforma móvil nativa
- **Jetpack Compose** - Toolkit moderno para el desarrollo de interfaces de usuario nativas (UI declarativa)
- **MVVM (Model-View-ViewModel)** - Arquitectura principal de la aplicación
- **Navigation Compose** - Gestión de navegación entre pantallas
- **Supabase** - Backend como servicio (Autenticación, Base de datos, Storage, Realtime, Deep Links)
- **Gradle** - Sistema de build y gestión de dependencias
- **GitHub** - Control de versiones y colaboración

## Funcionalidades Principales

### Usuario

- **Autenticación**: Inicio y registro de sesión (incluyendo soporte para autenticación con Deep Links).
- **Dashboard**: Vista principal con resumen y acciones rápidas.
- **Creación de tickets**: Reporte de incidencias con descripción, tipo, prioridad y equipo afectado.
- **Consulta de tickets propios**: Visualización de tickets creados por el usuario y filtrado por estados.
- **Consulta de detalle**: Visualización detallada de cada ticket con su información completa.
- **Gestión de perfil**: Edición y visualización del perfil del usuario.
- **Cierre de sesión**: Terminación segura de la sesión.

### Administrador

- **Dashboard de métricas**: Visualización de estadísticas generales del sistema.
- **Gestión de tickets**: Visualización de todos los tickets, edición, cambio de estado (Pendiente, En proceso, Solucionado) y prioridad.
- **Gestión de usuarios**: Administración de las cuentas registradas en el sistema.
- **Gestión de equipos**: Administración del catálogo de equipos y hardware.
- **Reportes**: Generación y visualización de reportes avanzados.
- **Gestión de perfil**: Edición del perfil de administrador.
- **Historial de registros**: Consulta cronológica y búsqueda de tickets obtenidos desde PostgreSQL.

## Integración de Deep Links

La aplicación implementa manejo avanzado de intenciones (Intents) en el `MainActivity` para procesar Deep Links de autenticación mediante el esquema `ticketflow://auth`. Esto permite integraciones fluidas con flujos OAuth de Supabase, validando las sesiones para evitar procesamiento duplicado.

---

## Implementación de Programación Orientada a Objetos

### Clases

- **Ticket** (data class): Modelo de ticket con validación en constructor
- **Usuario** (abstract class): Clase base para usuarios del sistema
- **UsuarioRegular**: Usuario con permisos estándar
- **Administrador**: Usuario con permisos administrativos
- **Equipo** (data class): Modelo de equipo/equipment
- **EstadoTicket** (enum): PENDIENTE, EN_PROCESO, SOLUCIONADO
- **PrioridadTicket** (enum): BAJA, MEDIA, ALTA, CRITICA
- **TicketService**: Servicio CRUD para gestión de tickets
- **UsuarioService**: Servicio CRUD para gestión de usuarios
- **EquipoService**: Servicio CRUD para gestión de equipos
- **ReporteService**: Servicio para generación de reportes estadísticos

### Objetos

- **Logger** (object): Singleton para logging de errores en archivo
- **SupabaseClient** (object): Singleton para cliente de Supabase
- Instancias globales de servicios: ticketService, usuarioService, equipoService, reporteService

### Herencia

```
Usuario (abstract class)
├── UsuarioRegular (esAdmin = false)
└── Administrador (esAdmin = true)
```

La herencia se utiliza para diferenciar tipos de usuarios con diferentes permisos en el sistema.

### Interfaces

- **CrudService<T, ID>**: Interfaz genérica para operaciones CRUD
  - `create(item: T)`
  - `readAll(): List<T>`
  - `findById(id: ID): T?`
  - `update(item: T)`
  - `delete(id: ID): Boolean`

Implementada por: TicketService, UsuarioService, EquipoService

### Encapsulamiento

- Propiedades privadas en servicios (`private val tickets`, `private val usuarios`, etc.)
- Métodos públicos para acceso controlado a datos
- Validaciones en constructores de data classes
- Separación clara entre modelos, servicios y utilidades

---

## Estructura Real del Proyecto

La aplicación utiliza Jetpack Compose, organizando la interfaz en Pantallas (Screens) y ViewModels para cada flujo.

```
TicketFlow/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/devcore/ticketflow/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/        # Configuración de AppNavigation
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── admin/         # Pantallas y ViewModels del Administrador
│   │   │   │   │   │   ├── auth/          # Pantallas y ViewModels de Login y Registro
│   │   │   │   │   │   └── user/          # Pantallas y ViewModels del Usuario Regular
│   │   │   │   │   └── theme/             # Tema, Tipografía y Colores en Compose
│   │   │   │   ├── console/               # Consola Etapa 2 (Servicios, CRUD y utilidades)
│   │   │   │   ├── MainActivity.kt        # Entry point de Compose y Deep Links
│   │   │   │   └── SupabaseClient.kt      # Configuración del cliente Supabase
│   │   │   ├── AndroidManifest.xml
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── errores.txt                        # Log de errores consola
├── build.gradle.kts
└── settings.gradle.kts
```

## Gestión de Tickets

### Operaciones CRUD

- **Crear**: Creación de nuevos tickets con tipo, descripción, prioridad y equipo afectado
- **Listar**: Visualización de tickets (todos los tickets para admin, propios para usuario)
- **Buscar**: Búsqueda de tickets por ID
- **Actualizar**: Modificación de descripción y otros campos
- **Eliminar**: Eliminación de tickets del sistema

### Operaciones Específicas

- **Cambiar estado**: Transición entre PENDIENTE → EN_PROCESO → SOLUCIONADO
- **Cambiar prioridad**: Modificación de nivel de prioridad (BAJA → MEDIA → ALTA → CRITICA)
- **Consultar detalle**: Visualización completa de información del ticket

## Estados y Prioridades

### Estados
- **PENDIENTE**: Ticket creado pero no atendido
- **EN_PROCESO**: Ticket siendo trabajado por el equipo de soporte
- **SOLUCIONADO**: Ticket resuelto y cerrado

### Prioridades
- **BAJA**: Incidencia menor, no afecta operaciones críticas
- **MEDIA**: Incidencia moderada, requiere atención pero no urgente
- **ALTA**: Incidencia importante, afecta operaciones significativas
- **CRITICA**: Incidencia crítica, requiere atención inmediata

## Validaciones

### Validación de Entradas
- `toIntOrNull()`: Validación de conversiones numéricas seguras
- `isNotBlank()`: Validación de campos no vacíos
- Validación de opciones de menú (enumeraciones)
- Validación de existencia de entidades antes de operaciones

### Validación en Modelos
- **Ticket**: Valida que `tipoIncidencia` y `descripcion` no estén vacíos en el constructor
- Validación de IDs numéricos antes de operaciones CRUD
- Validación de permisos de usuario (usuarios solo ven sus tickets)

## Manejo de Excepciones

### Excepciones Personalizadas
- **TicketValidationException**: Error de validación de datos de ticket
- **TicketNotFoundException**: Ticket no encontrado en operaciones
- **EquipoNotFoundException**: Equipo no encontrado
- **UsuarioNotFoundException**: Usuario no encontrado

### Manejo de Excepciones
- Bloques `try/catch` en todas las operaciones críticas
- Logging de errores en archivo `errores.txt`
- Mensajes de error descriptivos al usuario
- Prevención de crashes ante entradas inválidas

## Ejecución de Android

### Compilación
```bash
.\gradlew.bat assembleDebug
```

### Ejecución
Conectar dispositivo Android o emulador y ejecutar desde Android Studio.

---

## Consola y Evaluaciones Académicas (Etapa 2)

Las secciones a continuación detallan las implementaciones específicas realizadas y probadas en la línea de comando para la evaluación de la arquitectura backend, el manejo de colecciones y servicios CRUD antes de la migración a la interfaz gráfica completa.

### Reportes
Genera reportes estadísticos del sistema incluyendo totales, conteos por estado, por prioridad y los equipos con más incidencias de manera dinámica en memoria.

### Actualización Dinámica
Todos los datos se mantienen en memoria durante la ejecución de la consola. Las operaciones CRUD modifican las colecciones en memoria y los cambios se reflejan inmediatamente.

### Ejecución de la Consola (Etapa 2)
```bash
.\gradlew.bat runConsole -q --console=plain
```

### Flujo de Prueba de Etapa 2
1. Iniciar sesión como administrador / usuario en CLI.
2. Crear un ticket con validaciones en memoria.
3. Actualizar, listar, eliminar tickets.
4. Cambiar estados (Pendiente, En Proceso, Solucionado).
5. Generar un reporte dinámico y consultar el `errores.txt` en caso de fallo.

## Integrantes

Jonathan Alexander Alberto Cruz (AC200739)
Vladimir Josué Aguila Méndez (AM240357)
Bryan Otoniel Loarca Montenegro (LM252002)
Fernando Alexander Juarez Coto (JC252637)
Ricardo Ernesto Alfaro Recinos (AR180405)


## Estado del Proyecto

- **Aplicación Android (Compose)**: Compila y funciona correctamente. Interfaz 100% migrada a Jetpack Compose con arquitectura MVVM.
- **Consola Etapa 2**: Funcional y mantenida como legacy de evaluación de POO.
- **Autenticación Supabase**: Configurada con soporte a Deep Links.
- **Módulo Usuario**: Flujo completo operativo (Dashboards, Tickets, Creación y Perfil).
- **Módulo Administrador**: Flujo completo operativo (Dashboard, Gestión de Tickets, Equipos, Usuarios y Reportes).

## Notas Importantes

- Los datos de la consola (CLI) se mantienen en memoria y no persisten entre ejecuciones.
- La aplicación Android nativa utiliza **Supabase** para persistencia real de datos.
- El proyecto requiere credenciales válidas de Supabase configuradas en el entorno local (variables o cliente) para funcionamiento completo de la app.
