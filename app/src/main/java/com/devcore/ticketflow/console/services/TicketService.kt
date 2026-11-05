package com.devcore.ticketflow.console.services

import com.devcore.ticketflow.console.interfaces.CrudService
import com.devcore.ticketflow.console.models.EstadoTicket
import com.devcore.ticketflow.console.models.PrioridadTicket
import com.devcore.ticketflow.console.models.Ticket
import com.devcore.ticketflow.console.utils.TicketNotFoundException

class TicketService : CrudService<Ticket, Int> {
    private val tickets = mutableListOf<Ticket>()
    private var idCounter = 1

    override fun create(item: Ticket) {
        // Asignamos un nuevo ID secuencial al crearlo
        val nuevoTicket = item.copy(id = idCounter++)
        tickets.add(nuevoTicket)
    }

    override fun readAll(): List<Ticket> = tickets.toList()

    override fun findById(id: Int): Ticket? = tickets.find { it.id == id }

    fun findByUsuarioId(usuarioId: String): List<Ticket> = tickets.filter { it.usuarioId == usuarioId }

    override fun update(item: Ticket) {
        val index = tickets.indexOfFirst { it.id == item.id }
        if (index != -1) {
            tickets[index] = item
        } else {
            throw TicketNotFoundException("No se puede actualizar. Ticket con ID ${item.id} no existe.")
        }
    }

    fun updateEstado(id: Int, nuevoEstado: EstadoTicket) {
        val ticket = findById(id) ?: throw TicketNotFoundException("Ticket con ID $id no existe para cambiar estado.")
        ticket.estado = nuevoEstado
    }

    fun updatePrioridad(id: Int, nuevaPrioridad: PrioridadTicket) {
        val ticket = findById(id) ?: throw TicketNotFoundException("Ticket con ID $id no existe para cambiar prioridad.")
        ticket.prioridad = nuevaPrioridad
    }

    override fun delete(id: Int): Boolean {
        val exists = tickets.any { it.id == id }
        if (!exists) {
            throw TicketNotFoundException("No se puede eliminar. Ticket con ID $id no existe.")
        }
        return tickets.removeIf { it.id == id }
    }
}
