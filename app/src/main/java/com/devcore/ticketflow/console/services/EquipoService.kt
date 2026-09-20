package com.devcore.ticketflow.console.services

import com.devcore.ticketflow.console.interfaces.CrudService
import com.devcore.ticketflow.console.models.Equipo

class EquipoService : CrudService<Equipo, String> {
    private val equipos = mutableListOf<Equipo>()

    init {
        // Inicialización de datos de prueba
        equipos.add(Equipo("EQ-001", "PC-001"))
        equipos.add(Equipo("EQ-002", "PC-002"))
        equipos.add(Equipo("EQ-003", "PC-003"))
        equipos.add(Equipo("EQ-004", "Impresora-001"))
    }

    override fun create(item: Equipo) {
        if (findById(item.id) == null) {
            equipos.add(item)
        }
    }

    override fun readAll(): List<Equipo> = equipos.toList()

    override fun findById(id: String): Equipo? = equipos.find { it.id == id || it.nombre == id }

    override fun update(item: Equipo) {
        val index = equipos.indexOfFirst { it.id == item.id }
        if (index != -1) {
            equipos[index] = item
        }
    }

    override fun delete(id: String): Boolean {
        return equipos.removeIf { it.id == id }
    }
}
