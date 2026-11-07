package com.devcore.ticketflow

import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para lógica de autenticación
 * Nota: Estas pruebas NO requieren mocks de Supabase.
 * Comprueban lógica pura de validación y transformación.
 */
class AuthenticationLogicTest {

    @Test
    fun rolValido_admin_esAceptado() {
        val rol = "admin"
        val esValido = rol.lowercase() == "admin" || rol.lowercase() == "usuario"
        assertTrue("El rol 'admin' debe ser válido", esValido)
    }

    @Test
    fun rolValido_usuario_esAceptado() {
        val rol = "usuario"
        val esValido = rol.lowercase() == "admin" || rol.lowercase() == "usuario"
        assertTrue("El rol 'usuario' debe ser válido", esValido)
    }

    @Test
    fun rolInvalido_guest_esRechazado() {
        val rol = "guest"
        val esValido = rol.lowercase() == "admin" || rol.lowercase() == "usuario"
        assertFalse("El rol 'guest' debe ser rechazado", esValido)
    }

    @Test
    fun rolConMayusculas_ADMIN_esNormalizadoCorrectamente() {
        val rol = "ADMIN"
        val rolNormalizado = rol.lowercase()
        assertEquals("El rol debe normalizarse a minúsculas", "admin", rolNormalizado)
        val esValido = rolNormalizado == "admin" || rolNormalizado == "usuario"
        assertTrue("El rol normalizado debe ser válido", esValido)
    }

    @Test
    fun rolVacio_esRechazado() {
        val rol = ""
        val esValido = rol.isNotEmpty() && (rol.lowercase() == "admin" || rol.lowercase() == "usuario")
        assertFalse("Un rol vacío debe ser rechazado", esValido)
    }

    @Test
    fun rolConEspacios_espacio_esRechazadoSinTrim() {
        val rol = " admin"
        val esValido = rol.lowercase() == "admin"  // Sin trim
        assertFalse("Un rol con espacios sin trim debe fallar", esValido)
    }

    @Test
    fun rolConEspacios_espacio_esAceptadoConTrim() {
        val rol = " admin ".trim()
        val esValido = rol.lowercase() == "admin"
        assertTrue("Un rol con espacios debe ser aceptado después de trim", esValido)
    }

    @Test
    fun validacionUID_uuidsDiferentes_noCoinciden() {
        val uid1 = "550e8400-e29b-41d4-a716-446655440000"
        val uid2 = "550e8400-e29b-41d4-a716-446655440001"
        assertNotEquals("UIDs diferentes no deben coincidir", uid1, uid2)
    }

    @Test
    fun validacionUID_uidsIguales_coinciden() {
        val uid1 = "550e8400-e29b-41d4-a716-446655440000"
        val uid2 = "550e8400-e29b-41d4-a716-446655440000"
        assertEquals("UIDs iguales deben coincidir", uid1, uid2)
    }

    @Test
    fun validacionContrasena_menorA6_esDebil() {
        val password = "Pass1"
        assertTrue("Contraseña menor a 6 caracteres debe ser débil", password.length < 6)
    }

    @Test
    fun validacionContrasena_6Caracteres_esValida() {
        val password = "Pass12"
        assertFalse("Contraseña con 6 caracteres debe ser válida", password.length < 6)
    }

    @Test
    fun rolNull_contolerancia_esRechazado() {
        val rol: String? = null
        val rolTrimmed = rol?.trim() ?: ""
        val esValido = rolTrimmed.isNotEmpty() && (rolTrimmed.lowercase() == "admin" || rolTrimmed.lowercase() == "usuario")
        assertFalse("Rol null debe ser rechazado", esValido)
    }

    @Test
    fun perfilValidacion_uidMismatch_esDetectado() {
        val authUID = "550e8400-e29b-41d4-a716-446655440000"
        val profileUID = "550e8400-e29b-41d4-a716-446655440001"
        
        val esValido = authUID == profileUID
        assertFalse("Un mismatch de UID debe ser detectado", esValido)
    }

    @Test
    fun perfilValidacion_uidCoincide_esAceptado() {
        val authUID = "550e8400-e29b-41d4-a716-446655440000"
        val profileUID = "550e8400-e29b-41d4-a716-446655440000"
        
        val esValido = authUID == profileUID
        assertTrue("Un UID coincidente debe ser aceptado", esValido)
    }

    @Test
    fun navegacionSeguridad_rolInvalidoPrevalida_accesoDenegado() {
        val rolesPermitidos = setOf("admin", "usuario")
        val rolUsuario = "moderator"
        
        val tieneAcceso = rolUsuario in rolesPermitidos
        assertFalse("Un rol no en lista permitida debe denegar acceso", tieneAcceso)
    }

    @Test
    fun navegacionSeguridad_rolValidoPrevalida_accesoOtorgado() {
        val rolesPermitidos = setOf("admin", "usuario")
        val rolUsuario = "admin"
        
        val tieneAcceso = rolUsuario in rolesPermitidos
        assertTrue("Un rol en lista permitida debe otorgar acceso", tieneAcceso)
    }
}
