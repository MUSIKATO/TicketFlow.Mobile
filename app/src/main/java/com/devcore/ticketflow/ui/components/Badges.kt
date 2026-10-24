package com.devcore.ticketflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devcore.ticketflow.ui.theme.CorporateBlue
import com.devcore.ticketflow.ui.theme.CorporateNavy
import com.devcore.ticketflow.ui.theme.PriorityCritical
import com.devcore.ticketflow.ui.theme.PriorityCriticalBg
import com.devcore.ticketflow.ui.theme.PriorityHigh
import com.devcore.ticketflow.ui.theme.PriorityHighBg
import com.devcore.ticketflow.ui.theme.PriorityLow
import com.devcore.ticketflow.ui.theme.PriorityLowBg
import com.devcore.ticketflow.ui.theme.PriorityMedium
import com.devcore.ticketflow.ui.theme.PriorityMediumBg
import com.devcore.ticketflow.ui.theme.SurfaceLight
import com.devcore.ticketflow.ui.theme.TextSecondary
import com.devcore.ticketflow.ui.theme.TicketStatusInProgress
import com.devcore.ticketflow.ui.theme.TicketStatusInProgressBg
import com.devcore.ticketflow.ui.theme.TicketStatusPending
import com.devcore.ticketflow.ui.theme.TicketStatusPendingBg
import com.devcore.ticketflow.ui.theme.TicketStatusResolved
import com.devcore.ticketflow.ui.theme.TicketStatusResolvedBg

/**
 * Badge de prioridad reutilizable
 * Colores: Baja (verde), Media (ámbar), Alta (naranja), Crítica (rojo)
 */
@Composable
fun PriorityBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (priority.lowercase()) {
        "baja" -> PriorityLowBg to PriorityLow
        "media" -> PriorityMediumBg to PriorityMedium
        "alta" -> PriorityHighBg to PriorityHigh
        "crítica", "critica" -> PriorityCriticalBg to PriorityCritical
        else -> PriorityMediumBg to PriorityMedium
    }

    Text(
        text = priority.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = textColor,
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Badge de estado reutilizable
 * Colores: Pendiente (amarillo), En proceso (azul), Solucionado (verde)
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "pendiente" -> TicketStatusPendingBg to TicketStatusPending
        "en proceso", "en_proceso" -> TicketStatusInProgressBg to TicketStatusInProgress
        "solucionado" -> TicketStatusResolvedBg to TicketStatusResolved
        else -> TicketStatusPendingBg to TicketStatusPending
    }

    Text(
        text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = textColor,
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Badge de rol reutilizable
 */
@Composable
fun RoleBadge(
    role: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (role.lowercase()) {
        "admin", "administrador" -> CorporateBlue to SurfaceLight
        "user", "usuario" -> PriorityLow to SurfaceLight
        else -> TextSecondary to SurfaceLight
    }

    Text(
        text = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = textColor,
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
