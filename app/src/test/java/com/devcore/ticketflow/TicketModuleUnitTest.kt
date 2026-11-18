package com.devcore.ticketflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketModuleUnitTest {

    @Test
    fun prioridadValueMapping_criticaDbConstraint_matchesCriticaWithoutAccent() {
        // En la base de datos PostgreSQL, el enum o constraint check es 'Critica'
        val chipLabel = "Crítica"
        val dbValue = if (chipLabel == "Crítica") "Critica" else chipLabel
        assertEquals("Critica", dbValue)
    }

    @Test
    fun prioridadSeleccionada_mapping_matchesExpectedDbValues() {
        val validPriorities = listOf("Baja", "Media", "Alta", "Critica")
        validPriorities.forEach { prioridad ->
            val mapped = when (prioridad.lowercase()) {
                "baja" -> "Baja"
                "media" -> "Media"
                "alta" -> "Alta"
                "critica", "crítica" -> "Critica"
                else -> null
            }
            assertEquals(prioridad, mapped)
        }
    }

    @Test
    fun ticketCreationFormValidation_requiresAllMandatoryFields() {
        fun isFormValid(equipo: String, tipoIncidencia: String, descripcion: String, prioridad: String?): Boolean {
            val equipoFinal = equipo.trim()
            return equipoFinal.isNotEmpty() &&
                    equipoFinal != "Seleccione un equipo" &&
                    tipoIncidencia.trim().isNotEmpty() &&
                    descripcion.trim().length >= 5 &&
                    prioridad != null
        }

        // Vacío no es válido
        assertFalse(isFormValid("", "", "", null))
        assertFalse(isFormValid("Seleccione un equipo", "Hardware", "Pantalla rota", "Alta"))
        assertFalse(isFormValid("Laptop", "", "Pantalla rota", "Alta"))
        assertFalse(isFormValid("Laptop", "Hardware", "abc", "Alta")) // menos de 5 caracteres
        assertFalse(isFormValid("Laptop", "Hardware", "Pantalla rota", null)) // sin prioridad

        // Caso válido completo
        assertTrue(isFormValid("Laptop Dell Inspiron", "Hardware", "No enciende tras actualización", "Critica"))
    }

    @Test
    fun adminEditTicketFormValidation_validatesRequiredFieldsAndDomainConstraints() {
        val validPriorities = listOf("Baja", "Media", "Alta", "Critica")
        val validStates = listOf("Pendiente", "En proceso", "Solucionado")

        fun isEditValid(equipo: String, tipoIncidencia: String, descripcion: String, prioridad: String, estado: String): Boolean {
            return equipo.trim().isNotBlank() &&
                    tipoIncidencia.trim().isNotBlank() &&
                    descripcion.trim().length >= 5 &&
                    prioridad in validPriorities &&
                    estado in validStates
        }

        // Caso válido
        assertTrue(isEditValid("Impresora HP", "Hardware", "Atasco constante de papel", "Media", "En proceso"))

        // Estado inválido
        assertFalse(isEditValid("Impresora HP", "Hardware", "Atasco constante de papel", "Media", "CanceladoInvalido"))

        // Prioridad inválida
        assertFalse(isEditValid("Impresora HP", "Hardware", "Atasco constante de papel", "UrgenteInvalido", "Pendiente"))

        // Descripción con sólo espacios
        assertFalse(isEditValid("Impresora HP", "Hardware", "     ", "Baja", "Solucionado"))
    }
}
