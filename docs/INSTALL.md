# INSTALACIÓN — TICKETFLOW MOBILE

Guía detallada para configurar, compilar y ejecutar TicketFlow Mobile en un entorno local.

## 1. Introducción

TicketFlow Mobile es una aplicación Android nativa (Kotlin + Jetpack Compose) para la gestión de incidencias y tickets de soporte técnico. Utiliza Supabase como backend exclusivo: autenticación (Auth), base de datos PostgreSQL mediante PostgREST y almacenamiento de archivos en Supabase Storage para evidencias fotográficas.

Este documento cubre:

- Requisitos previos
- Obtención del código
- Configuración de `local.properties`
- Compilación y ejecución de pruebas
- Ejecución de la aplicación
- Consola de escritorio (etapa 2)
- Solución de problemas comunes

La configuración del backend de Supabase (tablas, bucket, RLS, OAuth y deep links) se detalla en el **README principal** en las secciones "Configuración de Supabase (Backend)" y "Configuración de OAuth y Deep Links".

---

## 2. Requisitos previos

### 2.1 Software requerido

| Herramienta | Versión recomendada | Notas |
|-------------|---------------------|-------|
| Android Studio | Giraffe (2024.2.1) o superior | Con soporte para Kotlin y Jetpack Compose |
| JDK | 17 o 21 | Verificar en `gradle.properties` / `build.gradle.kts` |
| Android SDK | API nivel 24 o superior | Según `minSdk` / `compileSdk` del proyecto |
| SDK Build-tools | Última estable | Instalada por Android Studio |
| SDK Platform-tools | Última estable | Para `adb` |
| Git | 2.x o superior | Para clonar el repositorio |
| Emulador Android o dispositivo físico | API 24+ | Con depuración USB activada |

### 2.2 Cuenta y backend

- Cuenta en [Supabase](https://supabase.com/) (plan gratuito suficiente)
- Un proyecto de Supabase creado con **Project URL** y **Anon/Publishable key**
- (Opcional, para OAuth) Consola de Google Cloud y Facebook for Developers

---

## 3. Obtener el código

```bash
git clone <url-del-repositorio>
cd TicketFlow.Mobile
```

El equipo trabaja en ramas propias (`AC200739`, `AM240357`, `LM252002`, `jc252637`, `ar180405`). Cambie a la rama correspondiente si es necesario:

```bash
git checkout <rama>
```

---

## 4. Configurar `local.properties` (paso a paso)

El archivo `local.properties` define la ruta del Android SDK y las credenciales de Supabase. **No se versiona** (está en `.gitignore`).

### Paso 4.1 — Copiar la plantilla

En la raíz del proyecto, copie el archivo de ejemplo:

```bash
copy local.properties.example local.properties
```

(en PowerShell: `Copy-Item local.properties.example local.properties`)

### Paso 4.2 — Editar `sdk.dir`

Abra `local.properties` y establezca la ruta real del Android SDK en su equipo:

```properties
sdk.dir=C:\Users\<su-usuario>\AppData\Local\Android\Sdk
```

Si no sabe la ruta:
1. Abra Android Studio
2. Vaya a `Tools > SDK Manager` > `Appearance & Behavior > System Settings > Android SDK`
3. Copie la ruta mostrada en `Android SDK Location`

### Paso 4.3 — Configurar credenciales de Supabase

Obtenga los valores en Supabase Dashboard → **Settings → API**:

- `supabase.url`: **Project URL** (ej. `https://rsqayszvxrcgvylquzog.supabase.co`)
- `supabase.key`: **Anon public** o **Publishable key** (empieza por `sb_publishable_...`)

Ejemplo completo de `local.properties`:

```properties
## Ruta local al Android SDK
sdk.dir=C:\Users\juan\AppData\Local\Android\Sdk

## Credenciales de Supabase
supabase.url=https://rsqayszvxrcgvylquzog.supabase.co
supabase.key=sb_publishable_TU_CLAVE_AQUÍ
```

> **Importante:** Nunca suba `local.properties` a Git. Contiene credenciales y rutas locales. Solo se comparte la plantilla `local.properties.example`.

---

## 5. Sincronizar y compilar

### 5.1 Abrir en Android Studio

1. `File > Open` → seleccione la carpeta raíz del proyecto (`TicketFlow.Mobile`)
2. Espere a que Gradle sincronice el proyecto (`Sync Project with Gradle Files`)

### 5.2 Compilar desde la línea de comandos

Desde la raíz del proyecto:

```bash
# Compilar solo el código Kotlin
.\gradlew.bat compileDebugKotlin

# Compilar el APK de depuración
.\gradlew.bat assembleDebug
```

---

## 6. Ejecutar pruebas

```bash
# Pruebas unitarias JUnit
.\gradlew.bat testDebugUnitTest

# Pruebas instrumentadas (requiere dispositivo/emulador conectado)
.\gradlew.bat connectedAndroidTest
```

---

## 7. Ejecutar la aplicación

1. Conecte un dispositivo Android con depuración USB activada, o inicie un emulador desde Android Studio.
2. Ejecute el proyecto: `Run > Run 'app'` (o el botón verde de Play).
3. La Activity lanzadora es `MainActivity`.

---

## 8. Consola de escritorio (etapa 2)

El proyecto incluye una consola de línea de comandos con la lógica de dominio en memoria:

```bash
.\gradlew.bat runConsole -q --console=plain
```

---

## 9. Solución de problemas comunes

| Problema | Posible causa | Solución |
|----------|---------------|----------|
| `SDK location not found` | `sdk.dir` incorrecto o ausente en `local.properties` | Verificar la ruta en Android Studio > SDK Manager |
| `supabase.url` o `supabase.key` vacíos | `local.properties` incompleto | Completar con valores de Supabase Dashboard → Settings → API |
| Error de firma de debug | Keystore de debug corrupto | Eliminar `.android/debug.keystore` y reconstruir |
| Gradle no sincroniza | JDK no configurado | Configurar JDK 17 o 21 en Android Studio > Settings > Build > Build Tools > Gradle |

---

Fin de la guía de instalación.