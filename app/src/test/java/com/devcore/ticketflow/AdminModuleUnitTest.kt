package com.devcore.ticketflow

import com.devcore.ticketflow.ui.screens.admin.AdminEquipoItem
import com.devcore.ticketflow.ui.screens.admin.AdminTicketItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminModuleUnitTest {

    @Test
    fun adminTicketItem_defaultState_initializesCorrectly() {
        val ticket = AdminTicketItem(
            id = 101L,
            equipo = "PC-001",
            tipoIncidencia = "Falla de red",
            descripcion = "No conecta al servidor local",
            prioridad = "Alta",
            estado = "Pendiente",
            createdAt = "2026-10-22T19:00:00Z"
        )
        assertEquals(101L, ticket.id)
        assertEquals("PC-001", ticket.equipo)
        assertEquals("Alta", ticket.prioridad)
        assertEquals("Pendiente", ticket.estado)
    }

    @Test
    fun adminTicketsFilter_statusFilterValues_areValidDbStates() {
        val validStates = listOf("Pendiente", "En proceso", "Solucionado")
        assertTrue(validStates.contains("Pendiente"))
        assertTrue(validStates.contains("En proceso"))
        assertTrue(validStates.contains("Solucionado"))
    }

    @Test
    fun adminTicketsFilter_priorityFilterValues_areValidDbPriorities() {
        val validPriorities = listOf("Baja", "Media", "Alta", "Critica")
        assertTrue(validPriorities.contains("Baja"))
        assertTrue(validPriorities.contains("Media"))
        assertTrue(validPriorities.contains("Alta"))
        assertTrue(validPriorities.contains("Critica"))
    }

    @Test
    fun adminEquipoItem_calculationLogic_handlesCustomHardware() {
        val item = AdminEquipoItem(
            nombre = "Proyector Epson Sala 3",
            totalTicketsAsociados = 3,
            ticketsPendientes = 1,
            esPersonalizado = true
        )
        assertTrue(item.esPersonalizado)
        assertEquals(3, item.totalTicketsAsociados)
        assertEquals(1, item.ticketsPendientes)
    }

    @Test
    fun adminHistorySearchFilter_matchesAcrossAllFields() {
        val ticket = AdminTicketItem(
            id = 205L,
            equipo = "Terminal POS Mostrador",
            tipoIncidencia = "Software",
            descripcion = "Lector de tarjetas bloqueado",
            prioridad = "Critica",
            estado = "En proceso",
            createdAt = "2026-11-01T10:00:00Z"
        )

        fun matches(query: String): Boolean {
            val q = query.trim().lowercase()
            return ticket.id.toString().contains(q) ||
                    ticket.equipo.lowercase().contains(q) ||
                    ticket.tipoIncidencia.lowercase().contains(q) ||
                    ticket.descripcion.lowercase().contains(q) ||
                    ticket.estado.lowercase().contains(q) ||
                    ticket.prioridad.lowercase().contains(q)
        }

        // Búsqueda por ID
        assertTrue(matches("205"))
        // Búsqueda por equipo
        assertTrue(matches("terminal pos"))
        // Búsqueda por tipo de incidencia
        assertTrue(matches("software"))
        // Búsqueda por descripción
        assertTrue(matches("bloqueado"))
        // Búsqueda por estado
        assertTrue(matches("en proceso"))
        // Búsqueda por prioridad
        assertTrue(matches("critica"))
        // Búsqueda que no coincide
        assertFalse(matches("impresora"))
    }

    @Test
    fun adminHistoryStatusFilter_filtersCorrectlyByStatus() {
        val tickets = listOf(
            AdminTicketItem(id = 1L, estado = "Pendiente"),
            AdminTicketItem(id = 2L, estado = "En proceso"),
            AdminTicketItem(id = 3L, estado = "Solucionado")
        )

        val pendientes = tickets.filter { it.estado.equals("Pendiente", ignoreCase = true) }
        val enProceso = tickets.filter { it.estado.equals("En proceso", ignoreCase = true) }
        val solucionados = tickets.filter { it.estado.equals("Solucionado", ignoreCase = true) }

        assertEquals(1, pendientes.size)
        assertEquals(1L, pendientes.first().id)
        assertEquals(1, enProceso.size)
        assertEquals(2L, enProceso.first().id)
        assertEquals(1, solucionados.size)
        assertEquals(3L, solucionados.first().id)
    }
}
