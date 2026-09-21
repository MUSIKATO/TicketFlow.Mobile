# TicketFlow Mobile

## Descripción del Proyecto

TicketFlow Mobile es una aplicación Android para la gestión de incidencias y tickets de soporte técnico. Permite a los usuarios reportar problemas de equipos y servicios, mientras que los administradores pueden gestionar, priorizar y resolver estas incidencias de manera eficiente.

## Objetivo del Sistema

El objetivo de TicketFlow Mobile es proporcionar una solución integral para la gestión de tickets de soporte técnico, permitiendo:
- Reporte rápido de incidencias por parte de los usuarios
- Seguimiento del estado de los tickets
- Gestión administrativa de incidencias
- Generación de reportes y estadísticas
- Clasificación por prioridades y estados

## Tecnologías Utilizadas

- **Kotlin** - Lenguaje de programación principal
- **Android** - Plataforma móvil nativa
- **XML** - Layouts e interfaces de usuario (View-based)
- **Supabase** - Backend como servicio (Autenticación, Base de datos, Storage, Realtime)
- **Gradle** - Sistema de build y gestión de dependencias
- **GitHub** - Control de versiones y colaboración

## Funcionalidades Principales

### Usuario

- **Inicio de sesión**: Autenticación mediante correo y contraseña con Supabase
- **Creación de tickets**: Reporte de incidencias con descripción, tipo, prioridad y equipo afectado
- **Consulta de tickets propios**: Visualización de tickets creados por el usuario
- **Consulta de detalle**: Visualización detallada de cada ticket con su información completa
- **Filtrado por estado**: Vista de tickets por estado (Pendiente, En proceso, Solucionado)
- **Cierre de sesión**: Terminación segura de la sesión de usuario

### Administrador

- **Inicio de sesión**: Autenticación de administradores con Supabase
- **Dashboard de métricas**: Visualización de estadísticas generales del sistema
- **Lista de incidencias recientes**: Vista de los tickets más recientes
- **Acciones rápidas**: Acceso rápido a funciones de gestión (en desarrollo)
- **Cierre de sesión**: Terminación segura de la sesión de administrador

*Nota: Algunas funcionalidades administrativas están en desarrollo.*

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

### Clases Abstractas

- **Usuario**: Clase base abstracta que define la estructura común de usuarios con propiedad abstracta `esAdmin`

### Data Classes

- **Ticket**: Modelo inmutable con propiedades validadas
- **Equipo**: Modelo simple de equipo
- **Profile**: Modelo de perfil de usuario de Supabase

### Enumeraciones

- **EstadoTicket**: Define los estados posibles de un ticket
- **PrioridadTicket**: Define los niveles de prioridad

## Estructura Real del Proyecto

```
TicketFlow/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/devcore/ticketflow/
│   │   │   │   ├── console/                    # Consola Etapa 2
│   │   │   │   │   ├── MainConsole.kt         # Punto de entrada consola
│   │   │   │   │   ├── interfaces/
│   │   │   │   │   │   └── CrudService.kt    # Interfaz CRUD genérica
│   │   │   │   │   ├── models/
│   │   │   │   │   │   ├── Enums.kt          # Enumeraciones
│   │   │   │   │   │   ├── Equipo.kt         # Modelo Equipo
│   │   │   │   │   │   ├── Ticket.kt         # Modelo Ticket
│   │   │   │   │   │   └── Usuario.kt        # Modelo Usuario
│   │   │   │   │   ├── services/
│   │   │   │   │   │   ├── EquipoService.kt  # Servicio CRUD equipos
│   │   │   │   │   │   ├── ReporteService.kt # Servicio reportes
│   │   │   │   │   │   ├── TicketService.kt  # Servicio CRUD tickets
│   │   │   │   │   │   └── UsuarioService.kt # Servicio CRUD usuarios
│   │   │   │   │   └── utils/
│   │   │   │   │       ├── Exceptions.kt     # Excepciones personalizadas
│   │   │   │   │       └── Logger.kt         # Logging de errores
│   │   │   │   ├── AdminActivity.kt          # Activity Administrador
│   │   │   │   ├── MainActivity.kt           # Activity Login
│   │   │   │   ├── Profile.kt                # Data class Perfil
│   │   │   │   ├── SupabaseClient.kt         # Cliente Supabase
│   │   │   │   ├── UiUtils.kt                # Utilidades UI
│   │   │   │   ├── UserActivity.kt           # Activity Usuario (legacy)
│   │   │   │   ├── UserCreateTicketActivity.kt
│   │   │   │   ├── UserDashboardActivity.kt  # Dashboard Usuario
│   │   │   │   ├── UserEditProfileActivity.kt
│   │   │   │   ├── UserProfileActivity.kt
│   │   │   │   ├── UserTicketDetailActivity.kt
│   │   │   │   └── UserTicketsActivity.kt    # Lista tickets usuario
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_admin.xml
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_user*.xml
│   │   │   │   │   └── item_*.xml
│   │   │   │   ├── menu/
│   │   │   │   │   ├── menu_bottom_nav.xml
│   │   │   │   │   └── menu_bottom_nav_user.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── dimens.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── drawable/
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── errores.txt                           # Log de errores consola
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── .gitignore
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

## Colecciones Utilizadas

- **MutableList<Ticket>**: Almacenamiento dinámico de tickets en memoria
- **MutableList<Usuario>**: Almacenamiento de usuarios en memoria
- **MutableList<Equipo>**: Almacenamiento de equipos en memoria
- **List<T>**: Retornos inmutables para consultas

### Funciones Kotlin de Colecciones

- `filter { }`: Filtrado de elementos por condición
- `find { }`: Búsqueda del primer elemento que cumple condición
- `count { }`: Conteo de elementos que cumplen condición
- `groupingBy { }.eachCount()`: Agrupación y conteo por clave
- `indexOfFirst { }`: Búsqueda de índice del primer elemento
- `removeIf { }`: Eliminación condicional de elementos
- `any { }`: Verificación de existencia de elementos

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

## Archivo errores.txt

**Ubicación**: `app/errores.txt`

**Formato de registro**:
```
[yyyy-MM-dd HH:mm] Error en <operación>: <descripción>
```

**Contenido**: Registro de todos los errores ocurridos durante la ejecución de la consola con timestamp y descripción detallada.

## Reportes

### ReporteService

Genera reportes estadísticos del sistema incluyendo:
- **Total de tickets**: Cantidad total de tickets en el sistema
- **Tickets por estado**: Pendientes, En proceso, Solucionados
- **Tickets por prioridad**: Críticos, Alta, Media, Baja
- **Equipo con más incidencias**: Identificación del equipo más problemático

### Actualización Dinámica
Los valores del reporte se recalculan en tiempo real cada vez que se genera, reflejando el estado actual de los datos.

## Actualización Dinámica de Datos

Todos los datos se mantienen en memoria durante la ejecución de la consola. Las operaciones CRUD modifican las colecciones en memoria y los cambios se reflejan inmediatamente en:
- Listas de tickets
- Reportes estadísticos
- Búsquedas y consultas

## Ejecución de Android

### Compilación
```bash
.\gradlew.bat assembleDebug
```

### Instalación
El APK generado se encuentra en: `app/build/outputs/apk/debug/app-debug.apk`

### Ejecución
Conectar dispositivo Android o emulador y ejecutar desde Android Studio.

## Ejecución de la Consola (Etapa 2)

### Comando de Ejecución
```bash
.\gradlew.bat runConsole -q --console=plain
```

### Flujo de la Consola
1. Menú principal: Iniciar como Usuario o Administrador
2. Menú de rol con opciones específicas
3. Ejecución de operaciones seleccionadas
4. Retorno al menú o salida del sistema

## Flujo de Prueba de Etapa 2

### Prueba Completa de CRUD

1. **Crear ticket**
   - Seleccionar opción "Crear ticket"
   - Ingresar equipo afectado
   - Ingresar tipo de incidencia
   - Ingresar descripción
   - Seleccionar prioridad

2. **Listar tickets**
   - Seleccionar opción "Listar tickets"
   - Visualizar lista con ID, tipo, estado y prioridad

3. **Buscar ticket**
   - Ingresar ID del ticket
   - Verificar si existe y mostrar información

4. **Consultar detalle**
   - Ingresar ID del ticket
   - Visualizar información completa del ticket

5. **Actualizar descripción**
   - Ingresar ID del ticket
   - Ingresar nueva descripción
   - Confirmar actualización

6. **Cambiar estado**
   - Ingresar ID del ticket
   - Seleccionar nuevo estado (PENDIENTE/EN_PROCESO/SOLUCIONADO)
   - Confirmar cambio

7. **Cambiar prioridad**
   - Ingresar ID del ticket
   - Seleccionar nueva prioridad (BAJA/MEDIA/ALTA/CRITICA)
   - Confirmar cambio

8. **Generar reporte**
   - Ejecutar opción de reporte
   - Visualizar estadísticas actuales

9. **Eliminar ticket**
   - Ingresar ID del ticket
   - Confirmar eliminación

10. **Generar reporte nuevamente**
    - Verificar que los valores han cambiado
    - Confirmar actualización dinámica

## Integrantes

*Sección para completar con los nombres y responsabilidades de los integrantes del equipo.*

## Etapa 2 - Implementación

### Relación con Requisitos Técnicos

- **Kotlin**: 100% del código implementado en Kotlin
- **POO**: Implementación completa con clases, objetos, herencia, interfaces, encapsulamiento
- **Colecciones**: Uso extensivo de MutableList, List y funciones de colecciones Kotlin
- **Consola**: Interfaz de línea de comandos funcional con menús interactivos
- **CRUD**: Operaciones completas de Create, Read, Update, Delete implementadas
- **Validaciones**: Validación exhaustiva de entradas y datos
- **Excepciones**: Excepciones personalizadas y manejo robusto de errores
- **Log de errores**: Sistema de logging en archivo errores.txt
- **Procesamiento**: Lógica de negocio con estados, prioridades y clasificaciones
- **Reportes**: Generación dinámica de reportes estadísticos
- **Actualización dinámica**: Datos en memoria con actualización inmediata

### Evidencia de Funcionamiento

El sistema fue probado mediante ejecución automatizada con archivo de entrada (`test_input.txt`) generando salida completa (`test_output.txt`) que demuestra:
- Funcionamiento correcto de todas las operaciones CRUD
- Actualización dinámica de datos
- Generación de reportes con valores que cambian según el estado
- Manejo correcto de validaciones y excepciones
- Interfaz de consola funcional con menús y navegación

## Estado del Proyecto

- **Aplicación Android**: Compila correctamente (BUILD SUCCESSFUL)
- **Consola Etapa 2**: Funcional y completamente implementada
- **Autenticación Supabase**: Configurada y funcional
- **Módulo Usuario**: Funcional con dashboard y gestión de tickets
- **Módulo Administrador**: Dashboard visual con métricas (funcionalidades adicionales en desarrollo)

## Notas Importantes

- La consola de Etapa 2 es una implementación separada que no afecta la aplicación Android
- Los datos de la consola se mantienen en memoria y no persisten entre ejecuciones
- La aplicación Android utiliza Supabase para persistencia de datos
- Algunas funcionalidades administrativas en Android están en desarrollo
- El proyecto requiere credenciales válidas de Supabase para funcionamiento completo de la app Android
