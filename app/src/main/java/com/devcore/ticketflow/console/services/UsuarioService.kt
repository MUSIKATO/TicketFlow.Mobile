package com.devcore.ticketflow.console.services

import com.devcore.ticketflow.console.interfaces.CrudService
import com.devcore.ticketflow.console.models.Administrador
import com.devcore.ticketflow.console.models.Usuario
import com.devcore.ticketflow.console.models.UsuarioRegular

class UsuarioService : CrudService<Usuario, String> {
    private val usuarios = mutableListOf<Usuario>()

    init {
        // Inicializamos usuarios por defecto para poder iniciar sesión en consola
        usuarios.add(UsuarioRegular("USR-001", "Juan Perez", "juan@test.com"))
        usuarios.add(Administrador("ADM-001", "Admin General", "admin@ticketflow.com"))
    }

    override fun create(item: Usuario) {
        if (findById(item.id) == null) {
            usuarios.add(item)
        }
    }

    override fun readAll(): List<Usuario> = usuarios.toList()

    override fun findById(id: String): Usuario? = usuarios.find { it.id == id }

    override fun update(item: Usuario) {
        val index = usuarios.indexOfFirst { it.id == item.id }
        if (index != -1) {
            usuarios[index] = item
        }
    }

    override fun delete(id: String): Boolean {
        return usuarios.removeIf { it.id == id }
    }
}
