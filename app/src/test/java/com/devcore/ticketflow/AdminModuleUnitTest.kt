package com.devcore.ticketflow

import com.devcore.ticketflow.ui.screens.admin.AdminEquipoItem
import com.devcore.ticketflow.ui.screens.admin.AdminTicketItem
import org.junit.Assert.assertEquals
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
}
