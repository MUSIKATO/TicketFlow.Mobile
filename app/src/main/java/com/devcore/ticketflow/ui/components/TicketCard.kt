package com.devcore.ticketflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devcore.ticketflow.ui.theme.CardStroke
import com.devcore.ticketflow.ui.theme.StatusAmber
import com.devcore.ticketflow.ui.theme.StatusAmberBg
import com.devcore.ticketflow.ui.theme.StatusCritical
import com.devcore.ticketflow.ui.theme.StatusCriticalBg
import com.devcore.ticketflow.ui.theme.StatusInfoBlue
import com.devcore.ticketflow.ui.theme.StatusInfoBlueBg
import com.devcore.ticketflow.ui.theme.StatusOrange
import com.devcore.ticketflow.ui.theme.StatusOrangeBg
import com.devcore.ticketflow.ui.theme.StatusPending
import com.devcore.ticketflow.ui.theme.StatusPendingBg
import com.devcore.ticketflow.ui.theme.StatusSuccessGreen
import com.devcore.ticketflow.ui.theme.StatusSuccessGreenBg
import com.devcore.ticketflow.ui.theme.TextPrimary
import com.devcore.ticketflow.ui.theme.TextSecondary
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tarjeta reutilizable de ticket.
 * Recibe datos simples (no un modelo concreto) para poder usarse en cualquier pantalla.
 */
@Composable
fun TicketCard(
    id: Long,
    tipoIncidencia: String,
    equipo: String,
    descripcion: String,
    prioridad: String,
    estado: String,
    fecha: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#$id",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    val (prioColor, prioBg) = prioridadColor(prioridad)
                    TicketBadge(
                        text = prioridad.ifEmpty { "Normal" },
                        textColor = prioColor,
                        backgroundColor = prioBg
                    )

                    val (estColor, estBg) = estadoColor(estado)
                    TicketBadge(
                        text = estado.ifEmpty { "Pendiente" },
                        textColor = estColor,
                        backgroundColor = estBg
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver detalle",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tipoIncidencia.ifEmpty { "Sin tipo de incidencia" },
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = equipo.ifEmpty { "Sin equipo especificado" },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = descripcion.ifEmpty { "Sin descripción" },
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (fecha.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun TicketBadge(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun prioridadColor(prioridad: String): Pair<Color, Color> = when (prioridad.lowercase()) {
    "baja" -> StatusSuccessGreen to StatusSuccessGreenBg
    "media" -> StatusAmber to StatusAmberBg
    "alta" -> StatusOrange to StatusOrangeBg
    "critica", "crítica" -> StatusCritical to StatusCriticalBg
    else -> TextSecondary to CardStroke
}

fun estadoColor(estado: String): Pair<Color, Color> = when (estado.lowercase()) {
    "pendiente" -> StatusPending to StatusPendingBg
    "en proceso" -> StatusInfoBlue to StatusInfoBlueBg
    "solucionado" -> StatusSuccessGreen to StatusSuccessGreenBg
    else -> TextSecondary to CardStroke
}

/** Convierte la fecha ISO de Supabase (created_at) a un texto legible en la zona horaria del teléfono. */
fun formatTicketDate(iso: String): String = try {
    OffsetDateTime.parse(iso)
        .atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault()))
} catch (e: Exception) {
    ""
}