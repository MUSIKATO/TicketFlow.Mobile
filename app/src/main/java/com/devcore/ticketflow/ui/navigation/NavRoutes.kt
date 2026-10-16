package com.devcore.ticketflow.ui.navigation

/**
 * Catálogo central de rutas de navegación para TicketFlow Mobile (Etapa 3).
 * Cada ruta documenta el integrante responsable y su estado.
 */
object NavRoutes {
    // Integrante 1: Autenticación base e integración
    const val LOGIN = "login"

    // Integrante 2: Registro y Panel Principal del Usuario
    const val REGISTER = "register"
    const val USER_DASHBOARD = "user_dashboard"

    // Integrante 3: Creación, listado y detalle de tickets
    const val CREATE_TICKET = "create_ticket"
    const val USER_TICKETS = "user_tickets"
    const val TICKET_DETAIL = "ticket_detail/{ticketId}"

    fun ticketDetail(ticketId: Long): String = "ticket_detail/$ticketId"

    // Integrante 4: Panel y gestión administrativa
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_TICKETS = "admin_tickets"
    const val ADMIN_USERS = "admin_users"
    const val ADMIN_TEAMS = "admin_teams"

    // Integrante 5: Perfil, reportes, pruebas y documentación
    const val USER_PROFILE = "user_profile"
    const val USER_EDIT_PROFILE = "user_edit_profile"
    const val ADMIN_REPORTS = "admin_reports"
}
