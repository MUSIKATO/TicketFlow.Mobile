package com.devcore.ticketflow.console.models

import com.devcore.ticketflow.console.utils.TicketValidationException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Ticket(
    val id: Int,
    val usuarioId: String,
    val equipoId: String,
    val tipoIncidencia: String,
    var descripcion: String,
    var prioridad: PrioridadTicket,
    var estado: EstadoTicket = EstadoTicket.PENDIENTE,
    val fechaCreacion: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
) {
    init {
        if (tipoIncidencia.isBlank()) {
            throw TicketValidationException("El tipo de incidencia no puede estar vacío.")
        }
        if (descripcion.isBlank()) {
            throw TicketValidationException("La descripción del ticket no puede estar vacía.")
        }
    }
}
