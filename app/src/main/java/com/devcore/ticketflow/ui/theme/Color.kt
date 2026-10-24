package com.devcore.ticketflow.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// PALETA CORPORATIVA PERFUMERÍA VIP
// ==========================================

// Azul corporativo oscuro
val CorporateNavy = Color(0xFF1A365D)
val CorporateNavyDark = Color(0xFF142D4A)
val CorporateNavyLight = Color(0xFF244872)

// Azul secundario
val CorporateBlue = Color(0xFF2B6CB0)
val CorporateBlueDark = Color(0xFF235596)
val CorporateBlueLight = Color(0xFF3B82F6)

// Fondo general
val BgScreen = Color(0xFFF8FAFC)
val BgScreenDark = Color(0xFF0F172A)

// Superficies y tarjetas
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E293B)
val SurfaceVariantDark = Color(0xFF1A2B40)

// Texto principal
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)
val TextPrimaryDark = Color(0xFFF1F5F9)
val TextSecondaryDark = Color(0xFF94A3B8)

// Bordes discretos
val BorderColor = Color(0xFFE2E8F0)
val BorderColorDark = Color(0xFF334155)

// ==========================================
// ESTADOS DE PRIORIDAD (VERDE, ÁMBAR, NARANJA, ROJO)
// ==========================================

val PriorityLow = Color(0xFF10B981)          // Verde
val PriorityLowBg = Color(0xFFD1FAE5)

val PriorityMedium = Color(0xFFF59E0B)        // Amarillo/ámbar
val PriorityMediumBg = Color(0xFFFEF3C7)

val PriorityHigh = Color(0xFFF97316)          // Naranja
val PriorityHighBg = Color(0xFFFFEDD5)

val PriorityCritical = Color(0xFFEF4444)      // Rojo
val PriorityCriticalBg = Color(0xFFFEE2E2)

// ==========================================
// ESTADOS DE TICKETS (AMARILLO, AZUL, VERDE)
// ==========================================

val TicketStatusPending = Color(0xFFFBBF24)          // Amarillo suave
val TicketStatusPendingBg = Color(0xFFFFF7ED)

val TicketStatusInProgress = Color(0xFF3B82F6)      // Azul suave
val TicketStatusInProgressBg = Color(0xFFDBEAFE)

val TicketStatusResolved = Color(0xFF10B981)        // Verde suave
val TicketStatusResolvedBg = Color(0xFFD1FAE5)

// ==========================================
// COLORES COMPATIBLES (para compatibilidad con código existente)
// ==========================================

val PrimaryNavy = CorporateNavy
val PrimaryNavyDark = CorporateNavyDark
val PrimaryNavyLight = CorporateNavyLight

val StatusCritical = PriorityCritical
val StatusCriticalBg = PriorityCriticalBg

val StatusAmber = PriorityMedium
val StatusAmberBg = PriorityMediumBg

val StatusInfoBlue = TicketStatusInProgress
val StatusInfoBlueBg = TicketStatusInProgressBg

val StatusSuccessGreen = PriorityLow
val StatusSuccessGreenBg = PriorityLowBg

val StatusPending = TicketStatusPending
val StatusPendingBg = TicketStatusPendingBg

val StatusOrange = PriorityHigh
val StatusOrangeBg = PriorityHighBg

val CardStroke = BorderColor
val CardStrokeDark = BorderColorDark
