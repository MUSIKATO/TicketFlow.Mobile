package com.devcore.ticketflow.console.models

abstract class Usuario(
    val id: String,
    val nombre: String,
    val correo: String
) {
    abstract val esAdmin: Boolean
}

class UsuarioRegular(
    id: String,
    nombre: String,
    correo: String
) : Usuario(id, nombre, correo) {
    override val esAdmin: Boolean = false
}

class Administrador(
    id: String,
    nombre: String,
    correo: String
) : Usuario(id, nombre, correo) {
    override val esAdmin: Boolean = true
}
