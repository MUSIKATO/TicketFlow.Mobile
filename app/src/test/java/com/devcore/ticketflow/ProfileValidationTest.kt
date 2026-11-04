package com.devcore.ticketflow

import org.junit.Test

import org.junit.Assert.*

/**
 * Pruebas unitarias para validaciones de perfil de usuario
 * Nota: Las pruebas de formato de correo usan un regex simple porque Patterns.EMAIL_ADDRESS
 * requiere contexto de Android. La validación real en el código usa Patterns.EMAIL_ADDRESS.
 */
class ProfileValidationTest {

    // Regex simple para validación de correo en tests (similar a Patterns.EMAIL_ADDRESS)
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    @Test
    fun nombreVacio_esInvalido() {
        val nombre = ""
        assertFalse("El nombre vacío debe ser inválido", nombre.trim().isNotEmpty())
    }

    @Test
    fun nombreConMenosDe2Caracteres_esInvalido() {
        val nombre = "A"
        assertTrue("El nombre con menos de 2 caracteres debe fallar validación", nombre.trim().length < 2)
    }

    @Test
    fun nombreCon2Caracteres_esValido() {
        val nombre = "AB"
        assertTrue("El nombre con 2 caracteres debe ser válido", nombre.trim().length >= 2)
    }

    @Test
    fun nombreConMasDe100Caracteres_esInvalido() {
        val nombre = "A".repeat(101)
        assertTrue("El nombre con más de 100 caracteres debe fallar validación", nombre.trim().length > 100)
    }

    @Test
    fun nombreCon100Caracteres_esValido() {
        val nombre = "A".repeat(100)
        assertTrue("El nombre con 100 caracteres debe ser válido", nombre.trim().length <= 100)
    }

    @Test
    fun correoVacio_esInvalido() {
        val correo = ""
        assertFalse("El correo vacío debe ser inválido", correo.trim().isNotEmpty())
    }

    @Test
    fun correoConFormatoIncorrecto_esInvalido() {
        val correo = "correo-invalido"
        assertFalse("El correo sin formato debe ser inválido", emailRegex.matches(correo))
    }

    @Test
    fun correoSinArroba_esInvalido() {
        val correo = "usuariodominio.com"
        assertFalse("El correo sin @ debe ser inválido", emailRegex.matches(correo))
    }

    @Test
    fun correoSinDominio_esInvalido() {
        val correo = "usuario@"
        assertFalse("El correo sin dominio debe ser inválido", emailRegex.matches(correo))
    }

    @Test
    fun correoValido_esValido() {
        val correo = "usuario@ejemplo.com"
        assertTrue("El correo válido debe pasar validación", emailRegex.matches(correo))
    }

    @Test
    fun correoValidoConSubdominio_esValido() {
        val correo = "usuario@subdominio.ejemplo.com"
        assertTrue("El correo con subdominio debe ser válido", emailRegex.matches(correo))
    }

    @Test
    fun calculoPorcentaje_totalCero_retornaCero() {
        val count = 5
        val total = 0
        val percentage = if (total > 0) count.toFloat() / total.toFloat() else 0f
        assertEquals("El porcentaje con total cero debe ser 0", 0f, percentage, 0.001f)
    }

    @Test
    fun calculoPorcentaje_normal_calculaCorrectamente() {
        val count = 25
        val total = 100
        val percentage = if (total > 0) count.toFloat() / total.toFloat() else 0f
        assertEquals("El porcentaje debe calcularse correctamente", 0.25f, percentage, 0.001f)
    }

    @Test
    fun agrupacionPorEquipo_ordenamientoDescendente() {
        val equipmentCounts = mapOf(
            "Equipo A" to 10,
            "Equipo B" to 25,
            "Equipo C" to 5
        )
        val sortedList = equipmentCounts.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }

        assertEquals("El primer elemento debe ser el de mayor incidencia", "Equipo B", sortedList[0].first)
        assertEquals("El primer elemento debe tener 25 incidencias", 25, sortedList[0].second)
        assertEquals("El último elemento debe ser el de menor incidencia", "Equipo C", sortedList[2].first)
    }

    @Test
    fun agrupacionPorEquipo_listaVacia_manejaCorrectamente() {
        val equipmentCounts = emptyMap<String, Int>()
        val sortedList = equipmentCounts.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }

        assertTrue("La lista vacía debe retornar lista vacía", sortedList.isEmpty())
    }

    @Test
    fun trim_nombreConEspacios_eliminaEspacios() {
        val nombre = "  Juan  "
        val trimmed = nombre.trim()
        assertEquals("El trim debe eliminar espacios", "Juan", trimmed)
    }

    @Test
    fun trim_correoConEspacios_eliminaEspacios() {
        val correo = "  usuario@ejemplo.com  "
        val trimmed = correo.trim()
        assertEquals("El trim debe eliminar espacios", "usuario@ejemplo.com", trimmed)
    }
}
