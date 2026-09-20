package com.devcore.ticketflow.console

import com.devcore.ticketflow.console.models.*
import com.devcore.ticketflow.console.services.EquipoService
import com.devcore.ticketflow.console.services.ReporteService
import com.devcore.ticketflow.console.services.TicketService
import com.devcore.ticketflow.console.services.UsuarioService
import com.devcore.ticketflow.console.utils.Logger
import java.util.Scanner

val ticketService = TicketService()
val usuarioService = UsuarioService()
val equipoService = EquipoService()
val reporteService = ReporteService(ticketService)
val scanner = Scanner(System.`in`)

fun main() {
    var continuar = true
    while (continuar) {
        println("\n================================")
        println("          TICKETFLOW")
        println("================================")
        println("1. Iniciar como Usuario")
        println("2. Iniciar como Administrador")
        println("3. Salir")
        print("Seleccione una opción: ")

        when (readln().trim()) {
            "1" -> menuUsuario()
            "2" -> menuAdministrador()
            "3" -> {
                println("Saliendo del sistema...")
                continuar = false
            }
            else -> {
                val errorMsg = "Opción de menú principal incorrecta."
                println(errorMsg)
                Logger.logError("Login", errorMsg)
            }
        }
    }
}

fun menuUsuario() {
    val usuario = usuarioService.findById("USR-001") ?: return
    var salir = false
    while (!salir) {
        println("\n--- Menú Usuario (${usuario.nombre}) ---")
        println("1. Crear ticket")
        println("2. Ver mis tickets")
        println("3. Buscar uno de mis tickets")
        println("4. Ver detalle de un ticket")
        println("5. Salir")
        print("Seleccione una opción: ")

        when (readln().trim()) {
            "1" -> crearTicketFlujo(usuario.id)
            "2" -> listarTickets(ticketService.findByUsuarioId(usuario.id))
            "3" -> buscarTicket(usuario.id)
            "4" -> verDetalleTicket(usuario.id)
            "5" -> salir = true
            else -> println("Opción inválida.")
        }
    }
}

fun menuAdministrador() {
    val admin = usuarioService.findById("ADM-001") ?: return
    var salir = false
    while (!salir) {
        println("\n--- Menú Administrador (${admin.nombre}) ---")
        println("1. Crear ticket")
        println("2. Listar todos los tickets")
        println("3. Buscar ticket")
        println("4. Ver detalle")
        println("5. Actualizar ticket (Descripción)")
        println("6. Cambiar estado")
        println("7. Cambiar prioridad")
        println("8. Eliminar ticket")
        println("9. Generar reporte")
        println("10. Salir")
        print("Seleccione una opción: ")

        when (readln().trim()) {
            "1" -> crearTicketFlujo(admin.id)
            "2" -> listarTickets(ticketService.readAll())
            "3" -> buscarTicket(null)
            "4" -> verDetalleTicket(null)
            "5" -> actualizarTicket()
            "6" -> cambiarEstadoTicket()
            "7" -> cambiarPrioridadTicket()
            "8" -> eliminarTicket()
            "9" -> println(reporteService.generarReporte())
            "10" -> salir = true
            else -> println("Opción inválida.")
        }
    }
}

fun crearTicketFlujo(usuarioId: String) {
    try {
        println("\n--- Crear Nuevo Ticket ---")
        println("Equipos disponibles:")
        equipoService.readAll().forEach { println("- ${it.id}: ${it.nombre}") }
        
        print("Ingrese ID del equipo afectado: ")
        val equipoId = readln().trim()
        if (equipoService.findById(equipoId) == null) {
            val errorMsg = "Equipo '$equipoId' inexistente."
            println("Error: $errorMsg")
            Logger.logError("CrearTicket", errorMsg)
            return
        }

        print("Ingrese tipo de incidencia: ")
        val tipo = readln().trim()

        print("Ingrese descripción: ")
        val desc = readln().trim()

        println("Prioridades: 1. BAJA, 2. MEDIA, 3. ALTA, 4. CRITICA")
        print("Seleccione prioridad (1-4): ")
        val prioSel = readln().trim()
        val prioridad = when (prioSel) {
            "1" -> PrioridadTicket.BAJA
            "2" -> PrioridadTicket.MEDIA
            "3" -> PrioridadTicket.ALTA
            "4" -> PrioridadTicket.CRITICA
            else -> {
                val errorMsg = "Prioridad inválida seleccionada: $prioSel"
                println("Error: $errorMsg")
                Logger.logError("CrearTicket", errorMsg)
                return
            }
        }

        val nuevoTicket = Ticket(
            id = 0,
            usuarioId = usuarioId,
            equipoId = equipoId,
            tipoIncidencia = tipo,
            descripcion = desc,
            prioridad = prioridad
        )
        
        ticketService.create(nuevoTicket)
        println("¡Ticket creado exitosamente!")

    } catch (e: Exception) {
        println("Error al crear ticket: ${e.message}")
        Logger.logError("CrearTicket", e.message ?: "Error desconocido")
    }
}

fun listarTickets(lista: List<Ticket>) {
    if (lista.isEmpty()) {
        println("No hay tickets para mostrar.")
        return
    }
    println("\n--- Lista de Tickets ---")
    lista.forEach { 
        println("[#${it.id}] ${it.tipoIncidencia} - ${it.estado} - ${it.prioridad}")
    }
}

fun buscarTicket(usuarioRestringido: String?) {
    print("Ingrese ID del ticket a buscar: ")
    val idStr = readln().trim()
    val id = idStr?.toIntOrNull()
    if (id == null) {
        val errorMsg = "ID inválido ingresado para búsqueda: $idStr"
        println("Error: $errorMsg")
        Logger.logError("BuscarTicket", errorMsg)
        return
    }

    val ticket = ticketService.findById(id)
    if (ticket == null || (usuarioRestringido != null && ticket.usuarioId != usuarioRestringido)) {
        val errorMsg = "Ticket con ID $id inexistente o no tienes permiso."
        println("Error: $errorMsg")
        Logger.logError("BuscarTicket", errorMsg)
        return
    }
    
    println("Ticket encontrado: [#${ticket.id}] ${ticket.tipoIncidencia} (${ticket.estado})")
}

fun verDetalleTicket(usuarioRestringido: String?) {
    print("Ingrese ID del ticket para ver detalle: ")
    val idStr = readln().trim()
    val id = idStr?.toIntOrNull()
    if (id == null) {
        println("Error: ID numérico requerido.")
        return
    }

    val ticket = ticketService.findById(id)
    if (ticket == null || (usuarioRestringido != null && ticket.usuarioId != usuarioRestringido)) {
        println("Error: Ticket no encontrado o acceso denegado.")
        return
    }

    println("\n--- Detalle del Ticket #${ticket.id} ---")
    println("Usuario: ${ticket.usuarioId}")
    println("Equipo: ${ticket.equipoId}")
    println("Tipo: ${ticket.tipoIncidencia}")
    println("Descripción: ${ticket.descripcion}")
    println("Prioridad: ${ticket.prioridad}")
    println("Estado: ${ticket.estado}")
    println("Fecha Creación: ${ticket.fechaCreacion}")
}

fun actualizarTicket() {
    try {
        print("Ingrese ID del ticket a actualizar: ")
        val id = readln().trim().toIntOrNull() ?: throw IllegalArgumentException("ID inválido")
        
        val ticket = ticketService.findById(id)
        if (ticket == null) {
            val errorMsg = "Ticket con ID $id inexistente."
            println("Error: $errorMsg")
            Logger.logError("ActualizarTicket", errorMsg)
            return
        }

        print("Ingrese nueva descripción (actual: ${ticket.descripcion}): ")
        val desc = readln().trim()
        if (desc.isNotBlank()) {
            val ticketMod = ticket.copy()
            ticketMod.descripcion = desc
            ticketService.update(ticketMod)
            println("Ticket actualizado correctamente.")
        } else {
            println("No se modificó la descripción.")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        Logger.logError("ActualizarTicket", e.message ?: "Error")
    }
}

fun cambiarEstadoTicket() {
    try {
        print("Ingrese ID del ticket: ")
        val id = readln().trim().toIntOrNull() ?: throw IllegalArgumentException("ID inválido")
        
        println("Estados: 1. PENDIENTE, 2. EN_PROCESO, 3. SOLUCIONADO")
        print("Seleccione nuevo estado (1-3): ")
        val estSel = readln().trim()
        val estado = when (estSel) {
            "1" -> EstadoTicket.PENDIENTE
            "2" -> EstadoTicket.EN_PROCESO
            "3" -> EstadoTicket.SOLUCIONADO
            else -> {
                val errorMsg = "Estado inválido: $estSel"
                println("Error: $errorMsg")
                Logger.logError("CambiarEstado", errorMsg)
                return
            }
        }
        
        ticketService.updateEstado(id, estado)
        println("Estado actualizado correctamente a $estado.")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        Logger.logError("CambiarEstado", e.message ?: "Error")
    }
}

fun cambiarPrioridadTicket() {
    try {
        print("Ingrese ID del ticket: ")
        val id = readln().trim().toIntOrNull() ?: throw IllegalArgumentException("ID inválido")
        
        println("Prioridades: 1. BAJA, 2. MEDIA, 3. ALTA, 4. CRITICA")
        print("Seleccione nueva prioridad (1-4): ")
        val prioSel = readln().trim()
        val prioridad = when (prioSel) {
            "1" -> PrioridadTicket.BAJA
            "2" -> PrioridadTicket.MEDIA
            "3" -> PrioridadTicket.ALTA
            "4" -> PrioridadTicket.CRITICA
            else -> {
                val errorMsg = "Prioridad inválida seleccionada: $prioSel"
                println("Error: $errorMsg")
                Logger.logError("CambiarPrioridad", errorMsg)
                return
            }
        }
        
        ticketService.updatePrioridad(id, prioridad)
        println("Prioridad actualizada correctamente a $prioridad.")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        Logger.logError("CambiarPrioridad", e.message ?: "Error")
    }
}

fun eliminarTicket() {
    try {
        print("Ingrese ID del ticket a eliminar: ")
        val id = readln().trim().toIntOrNull() ?: throw IllegalArgumentException("ID inválido")
        
        ticketService.delete(id)
        println("Ticket eliminado exitosamente.")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        Logger.logError("EliminarTicket", e.message ?: "Error")
    }
}
