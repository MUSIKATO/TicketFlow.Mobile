package com.devcore.ticketflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devcore.ticketflow.AdminActivity
import com.devcore.ticketflow.SupabaseClient
import com.devcore.ticketflow.ui.screens.auth.LoginScreen
import com.devcore.ticketflow.ui.screens.auth.RegisterScreen
import com.devcore.ticketflow.ui.screens.user.CreateTicketScreen
import com.devcore.ticketflow.ui.screens.user.TicketDetailScreen
import com.devcore.ticketflow.ui.screens.user.UserDashboardScreen
import com.devcore.ticketflow.ui.screens.user.UserProfileScreen
import com.devcore.ticketflow.ui.screens.user.UserTicketsScreen
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
@Suppress("FunctionName")
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val scope = rememberCoroutineScope()

    fun performSignOut() {
        scope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                SupabaseClient.perfil = null
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e // Re-throw CancellationException to maintain proper cancellation
            } catch (e: Exception) {
                // Log error if needed - signOut failure should not prevent navigation
            }
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOGIN,
        modifier = modifier
    ) {
        // ==========================================
        // INTEGRANTE 1: AUTENTICACIÓN
        // ==========================================
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToDashboard = { role ->
                    val destination = when (role.lowercase()) {
                        "admin" -> NavRoutes.ADMIN_DASHBOARD
                        else -> NavRoutes.USER_DASHBOARD
                    }
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                }
            )
        }

        // ==========================================
        // INTEGRANTE 2: REGISTRO Y DASHBOARD USUARIO
        // ==========================================
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.USER_DASHBOARD) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.USER_DASHBOARD) {
            UserDashboardScreen(
                onNavigateToCreateTicket = {
                    navController.navigate(NavRoutes.CREATE_TICKET)
                },
                onNavigateToTickets = {
                    navController.navigate(NavRoutes.USER_TICKETS)
                },
                onNavigateToDetail = { ticketId ->
                    navController.navigate(NavRoutes.ticketDetail(ticketId))
                },
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.USER_PROFILE)
                }
            )
        }

        // ==========================================
        // INTEGRANTE 3: TICKETS (CREACIÓN, LISTADO, DETALLE)
        // ==========================================
        composable(NavRoutes.CREATE_TICKET) {
            CreateTicketScreen(
                onNavigateBack = { navController.popBackStack() },
                onTicketCreated = { ticketId ->
                    navController.navigate(NavRoutes.ticketDetail(ticketId)) {
                        popUpTo(NavRoutes.USER_TICKETS) { inclusive = false }
                    }
                }
            )
        }

        composable(NavRoutes.USER_TICKETS) {
            UserTicketsScreen(
                onNavigateToDetail = { ticketId ->
                    navController.navigate(NavRoutes.ticketDetail(ticketId))
                },
                onNavigateToCreateTicket = {
                    navController.navigate(NavRoutes.CREATE_TICKET)
                },
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.USER_DASHBOARD) {
                        popUpTo(NavRoutes.USER_DASHBOARD) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.USER_PROFILE)
                }
            )
        }

        composable(
            route = NavRoutes.TICKET_DETAIL,
            arguments = listOf(navArgument("ticketId") { type = NavType.LongType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getLong("ticketId") ?: 0L
            TicketDetailScreen(
                ticketId = ticketId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // INTEGRANTE 4: DASHBOARD Y GESTIÓN ADMINISTRATIVA
        // ==========================================
        composable(NavRoutes.ADMIN_DASHBOARD) {
            PlaceholderScreen(
                title = "Panel Administrativo",
                assignedMember = "Integrante 4",
                route = NavRoutes.ADMIN_DASHBOARD,
                legacyActivityClass = AdminActivity::class.java,
                onSignOut = { performSignOut() }
            )
        }

        composable(NavRoutes.ADMIN_TICKETS) {
            PlaceholderScreen(
                title = "Gestión de Tickets (Admin)",
                assignedMember = "Integrante 4",
                route = NavRoutes.ADMIN_TICKETS,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ADMIN_USERS) {
            PlaceholderScreen(
                title = "Gestión de Usuarios",
                assignedMember = "Integrante 4",
                route = NavRoutes.ADMIN_USERS,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ADMIN_TEAMS) {
            PlaceholderScreen(
                title = "Gestión de Equipos",
                assignedMember = "Integrante 4",
                route = NavRoutes.ADMIN_TEAMS,
                onBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // INTEGRANTE 5: PERFIL Y REPORTES
        // ==========================================
        composable(NavRoutes.USER_PROFILE) {
            UserProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = { performSignOut() }
            )
        }

        composable(NavRoutes.USER_EDIT_PROFILE) {
            // No se necesita, la edición está integrada en UserProfileScreen
            navController.popBackStack()
        }

        composable(NavRoutes.ADMIN_REPORTS) {
            PlaceholderScreen(
                title = "Reportes Estadísticos",
                assignedMember = "Integrante 5",
                route = NavRoutes.ADMIN_REPORTS,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
