package com.devcore.ticketflow.ui.screens.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TicketAuditLog(
    val id: Long = 0,
    @SerialName("ticket_id")
    val ticketId: Long = 0,
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("user_name")
    val userName: String? = null,
    val operation: String = "",
    @SerialName("field_name")
    val fieldName: String? = null,
    @SerialName("old_value")
    val oldValue: String? = null,
    @SerialName("new_value")
    val newValue: String? = null,
    @SerialName("created_at")
    val createdAt: String = ""
)

fun TicketAuditLog.getDisplayOperation(): String {
    return when (operation.uppercase()) {
        "INSERT" -> "Ticket creado"
        "UPDATE" -> when (fieldName) {
            "estado" -> "Estado modificado"
            "prioridad" -> "Prioridad modificada"
            "descripcion" -> "Descripción modificada"
            "equipo" -> "Equipo modificado"
            "tipo_incidencia" -> "Tipo de incidencia modificado"
            else -> "Campo modificado"
        }
        "DELETE" -> "Ticket eliminado"
        else -> operation
    }
}

fun TicketAuditLog.getDisplayValueChange(): String? {
    if (operation == "INSERT" || operation == "DELETE") return newValue
    return if (oldValue != null && newValue != null) {
        "$oldValue → $newValue"
    } else {
        newValue ?: oldValue
    }
}
