package com.devcore.ticketflow.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.devcore.ticketflow.ui.theme.CorporateNavy

/**
 * Barra inferior de navegación para administrador
 */
@Composable
fun AdminBottomNavigation(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "admin_dashboard",
            icon = Icons.Default.Home,
            label = "Home"
        ),
        BottomNavItem(
            route = "admin_tickets",
            icon = Icons.Default.ConfirmationNumber,
            label = "Tickets"
        ),
        BottomNavItem(
            route = "admin_teams",
            icon = Icons.Default.Devices,
            label = "Equipos"
        ),
        BottomNavItem(
            route = "admin_users",
            icon = Icons.Default.Group,
            label = "Usuarios"
        ),
        BottomNavItem(
            route = "admin_profile",
            icon = Icons.Default.Person,
            label = "Perfil"
        )
    )

    NavigationBar(
        modifier = modifier,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val isSelected = selectedRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CorporateNavy,
                    selectedTextColor = CorporateNavy,
                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = CorporateNavy.copy(alpha = 0.12f)
                )
            )
        }
    }
}

/**
 * Barra inferior de navegación para usuario estándar
 */
@Composable
fun UserBottomNavigation(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "user_dashboard",
            icon = Icons.Default.Home,
            label = "Home"
        ),
        BottomNavItem(
            route = "user_tickets",
            icon = Icons.Default.ConfirmationNumber,
            label = "Tickets"
        ),
        BottomNavItem(
            route = "user_profile",
            icon = Icons.Default.Person,
            label = "Perfil"
        )
    )

    NavigationBar(
        modifier = modifier,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val isSelected = selectedRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CorporateNavy,
                    selectedTextColor = CorporateNavy,
                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = CorporateNavy.copy(alpha = 0.12f)
                )
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)
