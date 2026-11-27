package com.devcore.ticketflow.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devcore.ticketflow.ui.components.TicketFlowErrorBanner
import com.devcore.ticketflow.ui.components.TicketFlowLoading
import com.devcore.ticketflow.ui.theme.BorderColor
import com.devcore.ticketflow.ui.theme.CardStroke
import com.devcore.ticketflow.ui.theme.CorporateBlue
import com.devcore.ticketflow.ui.theme.CorporateNavy
import com.devcore.ticketflow.ui.theme.CorporateNavyDark
import com.devcore.ticketflow.ui.theme.PriorityCritical
import com.devcore.ticketflow.ui.theme.PriorityCriticalBg
import com.devcore.ticketflow.ui.theme.PriorityHigh
import com.devcore.ticketflow.ui.theme.PriorityHighBg
import com.devcore.ticketflow.ui.theme.PriorityLow
import com.devcore.ticketflow.ui.theme.PriorityLowBg
import com.devcore.ticketflow.ui.theme.PriorityMedium
import com.devcore.ticketflow.ui.theme.PriorityMediumBg
import com.devcore.ticketflow.ui.theme.StatusInfoBlue
import com.devcore.ticketflow.ui.theme.StatusInfoBlueBg
import com.devcore.ticketflow.ui.theme.StatusPending
import com.devcore.ticketflow.ui.theme.StatusPendingBg
import com.devcore.ticketflow.ui.theme.StatusSuccessGreen
import com.devcore.ticketflow.ui.theme.StatusSuccessGreenBg
import com.devcore.ticketflow.ui.theme.TextPrimary
import com.devcore.ticketflow.ui.theme.TextSecondary
import com.devcore.ticketflow.ui.theme.TicketStatusInProgress
import com.devcore.ticketflow.ui.theme.TicketStatusPending
import com.devcore.ticketflow.ui.theme.TicketStatusResolved
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminReportsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reportes y estadísticas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = CorporateNavy
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::refrescar,
                        enabled = !state.isLoading && !state.isRefreshing
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CorporateNavy
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar datos",
                                tint = CorporateNavy
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subtítulo del módulo
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Resumen de actividad y estado de los equipos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Banner de error si ocurre alguno
            state.errorMessage?.let { error ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TicketFlowErrorBanner(
                        message = error,
                        onDismiss = viewModel::dismissError
                    )
                    OutlinedButton(
                        onClick = viewModel::cargarDatos,
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CorporateNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reintentar consulta")
                    }
                }
            }

            // Selector interactivo de periodos
            PeriodSelectorRow(
                selected = state.periodoSeleccionado,
                onSelect = viewModel::onPeriodoSelected,
                enabled = !state.isLoading
            )

            // Contenido reactivo: Cargando vs Datos
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TicketFlowLoading(message = "Analizando estadísticas de Supabase...")
                }
            } else {
                // A. Tarjetas de resumen (3 indicadores principales)
                ResumenIndicadoresSection(
                    total = state.totalTickets,
                    criticos = state.criticalTickets,
                    criticosPct = state.criticalPercentage,
                    resueltos = state.resolvedTickets,
                    resueltosPct = state.resolvedPercentage
                )

                if (state.totalTickets == 0) {
                    EmptyPeriodCard(
                        periodo = state.periodoSeleccionado,
                        onResetPeriod = { viewModel.onPeriodoSelected(PeriodoFiltro.TODO) }
                    )
                } else {
                    // B. Incidencias por tipo de equipo (Gráfico de barras)
                    IncidenciasPorTipoEquipoSection(
                        tipos = state.incidenciasPorTipoEquipo
                    )

                    // C. Distribución por estado (Gráfico de dona circular)
                    DistribucionPorEstadoDonutSection(
                        estados = state.distribucionPorEstado,
                        totalTickets = state.totalTickets
                    )

                    // D. Equipos con más incidencias
                    EquiposConMasIncidenciasSection(
                        equipos = state.equiposConMasIncidencias
                    )

                    // E. Incidencias por prioridad
                    IncidenciasPorPrioridadSection(
                        prioridades = state.incidenciasPorPrioridad
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Selector interactivo de periodos (Filtros dinámicos)
 */
@Composable
private fun PeriodSelectorRow(
    selected: PeriodoFiltro,
    onSelect: (PeriodoFiltro) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodoFiltro.values().forEach { periodo ->
            val isSelected = selected == periodo
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(periodo) },
                enabled = enabled,
                label = {
                    Text(
                        text = periodo.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CorporateNavy,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = isSelected,
                    borderColor = BorderColor,
                    selectedBorderColor = CorporateNavy
                )
            )
        }
    }
}

/**
 * A. TARJETAS DE RESUMEN
 * Tres indicadores principales: Total Tickets, Incidencias Críticas y Tickets Resueltos.
 * Cada una muestra valor numérico, título e indicador/porcentaje de variación.
 */
@Composable
private fun ResumenIndicadoresSection(
    total: Int,
    criticos: Int,
    criticosPct: Float,
    resueltos: Int,
    resueltosPct: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Resumen de indicadores",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tarjeta 1: Total tickets
            MetricCard(
                title = "Total Tickets",
                value = "$total",
                subtitle = "Registrados",
                icon = Icons.Default.ConfirmationNumber,
                accentColor = CorporateNavy,
                badgeBg = CorporateNavy.copy(alpha = 0.1f),
                badgeColor = CorporateNavy,
                modifier = Modifier.weight(1f)
            )

            // Tarjeta 2: Incidencias críticas
            MetricCard(
                title = "Críticos",
                value = "$criticos",
                subtitle = String.format(Locale.getDefault(), "%.1f%% del total", criticosPct),
                icon = Icons.Default.Warning,
                accentColor = PriorityCritical,
                badgeBg = PriorityCriticalBg,
                badgeColor = PriorityCritical,
                modifier = Modifier.weight(1f)
            )

            // Tarjeta 3: Tickets resueltos
            MetricCard(
                title = "Resueltos",
                value = "$resueltos",
                subtitle = String.format(Locale.getDefault(), "%.1f%% éxito", resueltosPct),
                icon = Icons.Default.CheckCircle,
                accentColor = StatusSuccessGreen,
                badgeBg = StatusSuccessGreenBg,
                badgeColor = StatusSuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeBg: Color,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icono destacado
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(badgeBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Valor numérico grande
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            // Título
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Indicador secundario / variación
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Medium,
                    color = badgeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * B. INCIDENCIAS POR TIPO DE EQUIPO
 * Gráfico de barras horizontales proporcionales agrupadas por tipo de hardware.
 */
@Composable
private fun IncidenciasPorTipoEquipoSection(
    tipos: List<TipoEquipoCount>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Incidencias por tipo de equipo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Distribución según categoría de hardware",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = CorporateNavy,
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

            if (tipos.isEmpty()) {
                Text(
                    text = "No hay registros disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                tipos.forEach { item ->
                    TipoEquipoBarItem(item = item)
                }
            }
        }
    }
}

@Composable
private fun TipoEquipoBarItem(item: TipoEquipoCount) {
    val icon = when (item.tipo.lowercase()) {
        "pc" -> Icons.Default.Computer
        "laptop" -> Icons.Default.Laptop
        "impresora" -> Icons.Default.Print
        "monitor" -> Icons.Default.Devices
        "teclado" -> Icons.Default.Keyboard
        "mouse" -> Icons.Default.Mouse
        "red" -> Icons.Default.Router
        else -> Icons.Default.Devices
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CorporateBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = item.tipo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Text(
                text = "${item.count} (${String.format(Locale.getDefault(), "%.1f", item.porcentaje)}%)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = CorporateNavy
            )
        }

        // Barra proporcional con bordes redondeados
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(CorporateNavy.copy(alpha = 0.08f), RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (item.porcentaje / 100f).coerceIn(0.02f, 1f))
                    .height(10.dp)
                    .background(
                        color = CorporateNavy,
                        shape = RoundedCornerShape(5.dp)
                    )
            )
        }
    }
}

/**
 * C. DISTRIBUCIÓN POR ESTADO
 * Gráfico de dona (Canvas) representando Pendientes, En proceso y Resueltos, con leyenda detallada.
 */
@Composable
private fun DistribucionPorEstadoDonutSection(
    estados: List<EstadoCount>,
    totalTickets: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Distribución por estado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Proporción de tickets según flujo de trabajo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = CorporateNavy,
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gráfico de dona en Canvas
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChartCanvas(
                        estados = estados,
                        totalTickets = totalTickets,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Centro del dona con número total
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalTickets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tickets",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Leyenda descriptiva a la derecha
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estados.forEach { estado ->
                        val color = when (estado.estado.lowercase()) {
                            "pendiente" -> TicketStatusPending
                            "en proceso" -> TicketStatusInProgress
                            "solucionado" -> TicketStatusResolved
                            else -> CorporateBlue
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, CircleShape)
                                )
                                Text(
                                    text = estado.estado,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${estado.count} (${String.format(Locale.getDefault(), "%.0f", estado.porcentaje)}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChartCanvas(
    estados: List<EstadoCount>,
    totalTickets: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(10.dp)) {
        if (totalTickets == 0) {
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.4f),
                style = Stroke(width = 24.dp.toPx())
            )
            return@Canvas
        }

        var startAngle = -90f
        val strokeWidth = 24.dp.toPx()

        estados.forEach { item ->
            val sweepAngle = (item.count.toFloat() / totalTickets.toFloat()) * 360f
            if (sweepAngle > 0f) {
                val color = when (item.estado.lowercase()) {
                    "pendiente" -> TicketStatusPending
                    "en proceso" -> TicketStatusInProgress
                    "solucionado" -> TicketStatusResolved
                    else -> CorporateBlue
                }

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle - 2f, // Pequeño espacio estético entre arcos
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
    }
}

/**
 * D. EQUIPOS CON MÁS INCIDENCIAS
 * Ranking de los equipos con mayor volumen de tickets y barra de nivel de frecuencia.
 */
@Composable
private fun EquiposConMasIncidenciasSection(
    equipos: List<EquipoCount>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Equipos con más incidencias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Top equipos que requieren mayor atención",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = CorporateNavy,
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

            if (equipos.isEmpty()) {
                Text(
                    text = "No se encontraron equipos registrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                equipos.forEachIndexed { index, equipo ->
                    EquipoRankingItem(
                        ranking = index + 1,
                        item = equipo
                    )
                    if (index < equipos.size - 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipoRankingItem(
    ranking: Int,
    item: EquipoCount
) {
    val rankBadgeBg = when (ranking) {
        1 -> PriorityCriticalBg
        2 -> PriorityHighBg
        3 -> PriorityMediumBg
        else -> CorporateNavy.copy(alpha = 0.08f)
    }
    val rankBadgeColor = when (ranking) {
        1 -> PriorityCritical
        2 -> PriorityHigh
        3 -> PriorityMedium
        else -> CorporateNavy
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge de posición / ranking
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(rankBadgeBg, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$ranking",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = rankBadgeColor
                    )
                }

                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${item.count} incidencias",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = rankBadgeColor
            )
        }

        // Indicador visual de frecuencia relativa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(CorporateNavy.copy(alpha = 0.06f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = item.frecuenciaRelativa.coerceIn(0.04f, 1f))
                    .height(6.dp)
                    .background(
                        color = rankBadgeColor,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

/**
 * E. INCIDENCIAS POR PRIORIDAD
 * Distribución según nivel de severidad (Crítica, Alta, Media, Baja) con colores coherentes.
 */
@Composable
private fun IncidenciasPorPrioridadSection(
    prioridades: List<PrioridadCount>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Incidencias por prioridad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Severidad reportada en los tickets",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = CorporateNavy,
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

            prioridades.forEach { item ->
                PrioridadProgressItem(item = item)
            }
        }
    }
}

@Composable
private fun PrioridadProgressItem(item: PrioridadCount) {
    val (color, bgColor) = when (item.prioridad.lowercase()) {
        "crítica", "critica" -> Pair(PriorityCritical, PriorityCriticalBg)
        "alta" -> Pair(PriorityHigh, PriorityHighBg)
        "media" -> Pair(PriorityMedium, PriorityMediumBg)
        "baja" -> Pair(PriorityLow, PriorityLowBg)
        else -> Pair(CorporateNavy, CorporateNavy.copy(alpha = 0.1f))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Text(
                    text = item.prioridad,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Text(
                text = "${item.count} (${String.format(Locale.getDefault(), "%.1f", item.porcentaje)}%)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(bgColor, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (item.porcentaje / 100f).coerceIn(0.01f, 1f))
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Estado vacío para cuando no hay registros en el periodo seleccionado
 */
@Composable
private fun EmptyPeriodCard(
    periodo: PeriodoFiltro,
    onResetPeriod: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardStroke))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(CorporateNavy.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CorporateNavy,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Sin incidencias en \"${periodo.label}\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "No se encontraron tickets registrados que coincidan con este rango temporal. Puedes cambiar de periodo o consultar todo el historial.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (periodo != PeriodoFiltro.TODO) {
                OutlinedButton(
                    onClick = onResetPeriod,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CorporateNavy)
                ) {
                    Text("Ver todo el historial")
                }
            }
        }
    }
}
