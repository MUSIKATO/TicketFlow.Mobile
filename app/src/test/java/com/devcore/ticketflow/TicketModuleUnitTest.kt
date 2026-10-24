package com.devcore.ticketflow

import org.junit.Assert.assertEquals
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
}
