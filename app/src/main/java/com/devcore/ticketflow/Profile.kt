package com.devcore.ticketflow

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val fecha_creacion: String? = null
)