# PARTE 2 — AUTENTICACIÓN Y GESTIÓN DE USUARIOS CON SUPABASE AUTH
## TicketFlow Mobile — Integrante 2

**Fecha:** Noviembre 2026  
**Responsable:** Integrante 2  
**Estado:** Inspección inicial completada

---

## A. RESUMEN DE LO ENCONTRADO ANTES DE MODIFICAR

### Funcionalidades YA IMPLEMENTADAS

**✅ Registro de usuarios con correo y contraseña:**
- Implementado en `RegisterViewModel.kt` y `RegisterScreen.kt`
- Registro mediante Supabase Auth (`signUpWith(Email)`)
- Validaciones de campos:
  - Nombre: obligatorio, mínimo 2 caracteres
  - Email: obligatorio, formato válido con `Patterns.EMAIL_ADDRESS`
  - Contraseña: obligatorio, mínimo 6 caracteres
  - Confirmación de contraseña: obligatoria, debe coincidir con contraseña
- Manejo de errores:
  - Cuenta ya registrada ("User already registered")
  - Contraseña débil ("weak password")
  - Errores de red
- Confirmación de correo: UI maneja estado `requiresEmailConfirmation`
- Post-registro: Cierra sesión temporal para forzar confirmación de correo

**✅ Inicio de sesión:**
- Implementado en `LoginViewModel.kt` y `LoginScreen.kt`
- Login mediante Supabase Auth (`signInWith(Email)`)
- Validaciones:
  - Email: obligatorio, formato válido
  - Contraseña: obligatorio, mínimo 6 caracteres
- Manejo de errores:
  - Correo no confirmado ("email_not_confirmed")
  - Credenciales incorrectas ("Invalid login credentials")
  - Errores de red
- Post-login: Consulta perfil en tabla `profiles`, almacena en `SupabaseClient.perfil`
- Navegación según rol (admin/usuario)

**✅ Cierre de sesión:**
- Implementado en `AppNavigation.kt` función `performSignOut()`
- Llama a `SupabaseClient.client.auth.signOut()`
- Limpia `SupabaseClient.perfil = null`
- Navega a login con `popUpTo(0) { inclusive = true }` (limpia stack de navegación)

**✅ Persistencia de sesiones:**
- Implementado en `LoginViewModel.kt` función `comprobarSesion()`
- Se ejecuta en `init` del ViewModel
- Usa `SupabaseClient.client.auth.awaitInitialization()`
- Usa `SupabaseClient.client.auth.currentUserOrNull()`
- Si hay sesión, llama a `obtenerPerfilYRedirigir()`
- Si no hay sesión, permanece en login

**✅ Validaciones de formularios:**
- Login: Email válido, contraseña mínimo 6 caracteres
- Registro: Nombre mínimo 2 caracteres, email válido, contraseña mínimo 6 caracteres, confirmación coincide
- Mensajes de error claros en cada campo
- Estado de carga (`isLoading`) durante operaciones
- Prevención de envíos repetidos (botón deshabilitado durante `isLoading`)

**✅ Gestión de errores:**
- Mensajes amigables para el usuario
- Validación de errores específicos de Supabase
- Registro de errores técnicos en Log sin exponer credenciales
- Banner de error general en UI

**✅ Navegación según estado de autenticación:**
- `AppNavigation.kt` inicia en `NavRoutes.LOGIN`
- Login comprueba sesión automáticamente
- Login navega a dashboard según rol
- SignOut navega a login y limpia stack de navegación
- No se puede volver a pantallas privadas después de logout (stack limpiado)

**✅ Identidad del usuario mediante UID de Supabase:**
- UID obtenido de `SupabaseClient.client.auth.currentUserOrNull()?.id`
- UID usado para consultar perfil en tabla `profiles`
- UID usado para filtrar tickets (`eq("profile", userId)`)
- UID consistente al cerrar y recuperar sesión

### Funcionalidades INCOMPLETAS O NO IMPLEMENTADAS

**❌ Inicio de sesión con Google OAuth:**
- NO existe integración con Google OAuth
- NO hay botón de Google en LoginScreen
- NO hay configuración de Google en Supabase verificada
- Requiere:
  - Configuración de proveedor Google en Supabase Dashboard
  - Configuración de OAuth en Google Cloud Console
  - Implementación de flujo OAuth en Android
  - Deep links o callback URI
  - Procesamiento de resultado de autenticación

---

## B. ARCHIVOS INVOLUCRADOS EN AUTENTICACIÓN

### Archivos Existentes (ya implementados)

| Archivo | Función | Estado |
|---------|---------|--------|
| `ui/screens/auth/LoginViewModel.kt` | Lógica de inicio de sesión | ✅ COMPLETO |
| `ui/screens/auth/LoginScreen.kt` | UI de inicio de sesión | ✅ COMPLETO |
| `ui/screens/auth/RegisterViewModel.kt` | Lógica de registro | ✅ COMPLETO |
| `ui/screens/auth/RegisterScreen.kt` | UI de registro | ✅ COMPLETO |
| `ui/navigation/AppNavigation.kt` | Navegación y cierre de sesión | ✅ COMPLETO |
| `SupabaseClient.kt` | Cliente Supabase + cache de perfil | ✅ COMPLETO |
| `Profile.kt` | Modelo de datos de perfil | ✅ COMPLETO |

### Archivos por Crear (Google OAuth)

| Archivo | Función | Estado |
|---------|---------|--------|
| `ui/screens/auth/GoogleAuthViewModel.kt` | Lógica de Google OAuth | ❌ NO EXISTE |
| `ui/screens/auth/GoogleAuthComponent.kt` | Componente UI de botón Google | ❌ NO EXISTE |

---

## C. CONFIGURACIONES EXTERNAS PENDIENTES (GOOGLE OAUTH)

Para implementar Google OAuth, el responsable del proyecto debe realizar las siguientes configuraciones:

### 1. Supabase Dashboard - Autenticación

1. Acceder a: https://app.supabase.com/project/[PROJECT_ID]/auth/providers
2. Habilitar el proveedor **Google**
3. Configurar:
   - **Client ID**: Debe obtenerse de Google Cloud Console
   - **Client Secret**: Debe obtenerse de Google Cloud Console
   - **Redirect URL**: Proporcionada por Supabase (ej: `https://[PROJECT_ID].supabase.co/auth/v1/callback`)
4. Guardar configuración

### 2. Google Cloud Console - OAuth 2.0

1. Acceder a: https://console.cloud.google.com/apis/credentials
2. Crear nuevo proyecto o seleccionar existente
3. Habilitar **Google+ API** (o People API según versión)
4. Crear credenciales OAuth 2.0:
   - Tipo de aplicación: **Web application** o **Android**
   - Nombre: "TicketFlow Auth"
   - Authorized redirect URIs: Agregar URL de callback de Supabase
5. Copiar **Client ID** y **Client Secret**
6. Pegarlos en Supabase Dashboard

### 3. Android - Gradle (si se usa biblioteca de Google)

Si se requiere agregar dependencia de Google Identity Services, agregar a `app/build.gradle.kts`:
```kotlin
implementation("com.google.android.gms:play-services-auth:21.0.0")
```

**NOTA:** Supabase Kotlin SDK puede manejar OAuth sin dependencias adicionales de Google. Verificar documentación oficial.

### 4. Android - AndroidManifest.xml

Configurar deep link si es necesario:
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="ticketflow" />
</intent-filter>
```

---

## D. ARQUITECTURA DE AUTENTICACIÓN ACTUAL

### Flujo de Registro

```
Usuario completa formulario en RegisterScreen
  → RegisterViewModel.registrarse()
  → Validaciones (nombre, email, contraseña, confirmación)
  → SupabaseClient.client.auth.signUpWith(Email)
  → Supabase Auth crea usuario
  → RegisterViewModel cierra sesión temporal
  → UI muestra diálogo de confirmación
  → Usuario confirma en diálogo
  → Navegación a LoginScreen
```

### Flujo de Inicio de Sesión

```
Usuario introduce credenciales en LoginScreen
  → LoginViewModel.iniciarSesion()
  → Validaciones (email, contraseña)
  → SupabaseClient.client.auth.signInWith(Email)
  → Supabase Auth valida credenciales
  → LoginViewModel.obtenerPerfilYRedirigir()
  → SupabaseClient.client.from("profiles").select()
  → Consulta tabla profiles por usuario.id
  → Almacena en SupabaseClient.perfil
  → Valida rol (admin/usuario)
  → Emite evento NavigateToDashboard
  → Navegación a dashboard según rol
```

### Flujo de Persistencia de Sesión

```
App se inicia
  → LoginViewModel.init()
  → LoginViewModel.comprobarSesion()
  → SupabaseClient.client.auth.awaitInitialization()
  → SupabaseClient.client.auth.currentUserOrNull()
  → Si usuario existe: obtenerPerfilYRedirigir()
  → Si usuario es null: permanecer en login
```

### Flujo de Cierre de Sesión

```
Usuario hace logout
  → performSignOut() en AppNavigation
  → SupabaseClient.client.auth.signOut()
  → SupabaseClient.perfil = null
  → Navegación a NavRoutes.LOGIN
  → popUpTo(0) { inclusive = true } (limpia stack)
```

---

## E. MEDIDAS DE SEGURIDAD EXISTENTES

- ✅ Credenciales protegidas en local.properties/BuildConfig (Parte 1)
- ✅ NO se usan claves service_role o secret en Android
- ✅ UID obtenido de Supabase Auth, no de UI
- ✅ Registros filtrados por UID en consultas
- ✅ Sesión gestionada por Supabase SDK
- ✅ Logout limpia sesión y caché de perfil
- ✅ Validaciones de formularios en ViewModel (separación MVVM)
- ✅ Errores no exponen credenciales ni detalles técnicos sensibles

---

## F. PRUEBAS PENDIENTES

Las siguientes pruebas NO se han ejecutado en esta fase de inspección:

- ❌ Registro con correo y contraseña (requiere emulador/dispositivo + Supabase real)
- ❌ Registro con correo inválido (validaciones verificadas en código, pero no probadas en runtime)
- ❌ Registro con campos vacíos (validaciones verificadas en código, pero no probadas en runtime)
- ❌ Inicio de sesión correcto (requiere emulador/dispositivo + Supabase real)
- ❌ Inicio de sesión con contraseña incorrecta (requiere emulador/dispositivo + Supabase real)
- ❌ Cierre de sesión (requiere emulador/dispositivo + Supabase real)
- ❌ Recuperación de sesión al abrir app (requiere emulador/dispositivo + Supabase real)
- ❌ Inicio de sesión con Google (NO IMPLEMENTADO)
- ❌ Cancelación de inicio de sesión con Google (NO IMPLEMENTADO)
- ❌ Verificación del UID de Supabase (requiere emulador/dispositivo + Supabase real)
- ❌ Navegación según estado de autenticación (requiere emulador/dispositivo + Supabase real)

---

## G. PLAN DE TRABAJO PARA PARTE 2

### Fase 1: Documentar autenticación existente (Esta fase)
- ✅ Inspeccionar código actual
- ✅ Identificar funcionalidades implementadas
- ✅ Identificar funcionalidades faltantes
- ✅ Documentar arquitectura y flujos
- ✅ Listar configuraciones externas requeridas

### Fase 2: Implementar Google OAuth (PENDIENTE DE AUTORIZACIÓN)
- Agregar dependencias si es necesario
- Implementar flujo OAuth en LoginViewModel
- Agregar botón de Google en LoginScreen
- Configurar deep links en AndroidManifest
- Manejar resultado de autenticación
- Integrar con perfil de usuario
- Manejar errores de OAuth
- Documentar configuraciones externas

### Fase 3: Pruebas (PENDIENTE DE AUTORIZACIÓN)
- Pruebas de registro en emulador/dispositivo
- Pruebas de login en emulador/dispositivo
- Pruebas de logout en emulador/dispositivo
- Pruebas de persistencia de sesión
- Pruebas de Google OAuth (si se implementa)
- Actualizar documentación con resultados

---

## H. ESTADO ACTUAL

**Parte 2 del Integrante 2:**
- ✅ Inspección inicial completada
- ✅ Autenticación con correo/contraseña YA IMPLEMENTADA y completa
- ✅ Registro YA IMPLEMENTADO y completo
- ✅ Persistencia de sesión YA IMPLEMENTADA y completa
- ✅ Validaciones YA IMPLEMENTADAS y completas
- ✅ Manejo de errores YA IMPLEMENTADO y completo
- ✅ Navegación según autenticación YA IMPLEMENTADA y completa
- ✅ UID de Supabase YA IMPLEMENTADO y completo
- ❌ Google OAuth NO IMPLEMENTADO (requiere configuración externa)

**Conclusión:** La Parte 2 está **PARCIALMENTE COMPLETADA**. La autenticación con correo/contraseña está completamente implementada. Solo falta Google OAuth, que requiere configuración externa (Supabase Dashboard + Google Cloud Console) antes de poder implementar el código Android.

---

**INFORME INICIAL COMPLETADO - FASE DE INSPECCIÓN FINALIZADA**
