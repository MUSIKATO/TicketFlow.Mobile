package com.devcore.ticketflow.console.services

import com.devcore.ticketflow.console.models.EstadoTicket
import com.devcore.ticketflow.console.models.PrioridadTicket

class ReporteService(private val ticketService: TicketService) {

    fun generarReporte(): String {
        val tickets = ticketService.readAll()
        val total = tickets.size
        
        val pendientes = tickets.count { it.estado == EstadoTicket.PENDIENTE }
        val enProceso = tickets.count { it.estado == EstadoTicket.EN_PROCESO }
        val solucionados = tickets.count { it.estado == EstadoTicket.SOLUCIONADO }
        
        val criticos = tickets.count { it.prioridad == PrioridadTicket.CRITICA }
        val alta = tickets.count { it.prioridad == PrioridadTicket.ALTA }
        val media = tickets.count { it.prioridad == PrioridadTicket.MEDIA }
        val baja = tickets.count { it.prioridad == PrioridadTicket.BAJA }

        val equipoConMasIncidencias = tickets.groupingBy { it.equipoId }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: "Ninguno"

        return """
            ================================
                   REPORTE TICKETFLOW
            ================================
            Total de tickets: $total
            
            Pendientes: $pendientes
            En proceso: $enProceso
            Solucionados: $solucionados
            
            Críticos: $criticos
            
            Prioridad Alta: $alta
            Prioridad Media: $media
            Prioridad Baja: $baja
            
            Equipo con más incidencias: $equipoConMasIncidencias
            ================================
        """.trimIndent()
    }
}
