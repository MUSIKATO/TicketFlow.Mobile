package com.devcore.ticketflow.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import com.devcore.ticketflow.ui.components.AdminBottomNavigation
import com.devcore.ticketflow.ui.components.UserBottomNavigation
import com.devcore.ticketflow.ui.screens.admin.AdminDashboardScreen
import com.devcore.ticketflow.ui.screens.admin.AdminProfileScreen
import com.devcore.ticketflow.ui.screens.admin.AdminReportsScreen
import com.devcore.ticketflow.ui.screens.admin.AdminTeamsScreen
import com.devcore.ticketflow.ui.screens.admin.AdminTicketsScreen
import com.devcore.ticketflow.ui.screens.admin.AdminUsersScreen
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
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegistrationCompleted = { requiresConfirmation ->
                    // Si requiere confirmación de correo, regresar al login
                    // Si no requiere, también regresar al login (el usuario puede iniciar sesión)
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.USER_DASHBOARD) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    UserBottomNavigation(
                        selectedRoute = "user_dashboard",
                        onNavigate = { route ->
                            when (route) {
                                "user_dashboard" -> navController.navigate(NavRoutes.USER_DASHBOARD) {
                                    popUpTo(NavRoutes.USER_DASHBOARD) { inclusive = true }
                                }
                                "user_tickets" -> navController.navigate(NavRoutes.USER_TICKETS) {
                                    popUpTo(NavRoutes.USER_DASHBOARD)
                                }
                                "user_profile" -> navController.navigate(NavRoutes.USER_PROFILE) {
                                    popUpTo(NavRoutes.USER_DASHBOARD)
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
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
            }
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
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    UserBottomNavigation(
                        selectedRoute = "user_tickets",
                        onNavigate = { route ->
                            when (route) {
                                "user_dashboard" -> navController.navigate(NavRoutes.USER_DASHBOARD) {
                                    popUpTo(NavRoutes.USER_DASHBOARD) { inclusive = true }
                                }
                                "user_tickets" -> navController.navigate(NavRoutes.USER_TICKETS) {
                                    popUpTo(NavRoutes.USER_DASHBOARD)
                                }
                                "user_profile" -> navController.navigate(NavRoutes.USER_PROFILE) {
                                    popUpTo(NavRoutes.USER_DASHBOARD)
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
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
            }
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
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    AdminBottomNavigation(
                        selectedRoute = "admin_dashboard",
                        onNavigate = { route ->
                            when (route) {
                                "admin_dashboard" -> navController.navigate(NavRoutes.ADMIN_DASHBOARD) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD) { inclusive = true }
                                }
                                "admin_tickets" -> navController.navigate(NavRoutes.ADMIN_TICKETS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_teams" -> navController.navigate(NavRoutes.ADMIN_TEAMS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_users" -> navController.navigate(NavRoutes.ADMIN_USERS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_profile" -> navController.navigate(NavRoutes.ADMIN_PROFILE) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    AdminDashboardScreen(
                        onNavigateToTickets = {
                            navController.navigate(NavRoutes.ADMIN_TICKETS)
                        },
                        onNavigateToUsers = {
                            navController.navigate(NavRoutes.ADMIN_USERS)
                        },
                        onNavigateToTeams = {
                            navController.navigate(NavRoutes.ADMIN_TEAMS)
                        },
                        onNavigateToReports = {
                            navController.navigate(NavRoutes.ADMIN_REPORTS)
                        },
                        onSignOut = { performSignOut() }
                    )
                }
            }
        }

        composable(NavRoutes.ADMIN_TICKETS) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    AdminBottomNavigation(
                        selectedRoute = "admin_tickets",
                        onNavigate = { route ->
                            when (route) {
                                "admin_dashboard" -> navController.navigate(NavRoutes.ADMIN_DASHBOARD) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD) { inclusive = true }
                                }
                                "admin_tickets" -> navController.navigate(NavRoutes.ADMIN_TICKETS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_teams" -> navController.navigate(NavRoutes.ADMIN_TEAMS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_users" -> navController.navigate(NavRoutes.ADMIN_USERS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_profile" -> navController.navigate(NavRoutes.ADMIN_PROFILE) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    AdminTicketsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetail = { ticketId ->
                            navController.navigate(NavRoutes.ticketDetail(ticketId))
                        },
                        onNavigateToCreate = {
                            navController.navigate(NavRoutes.CREATE_TICKET)
                        }
                    )
                }
            }
        }

        composable(NavRoutes.ADMIN_USERS) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    AdminBottomNavigation(
                        selectedRoute = "admin_users",
                        onNavigate = { route ->
                            when (route) {
                                "admin_dashboard" -> navController.navigate(NavRoutes.ADMIN_DASHBOARD) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD) { inclusive = true }
                                }
                                "admin_tickets" -> navController.navigate(NavRoutes.ADMIN_TICKETS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_teams" -> navController.navigate(NavRoutes.ADMIN_TEAMS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_users" -> navController.navigate(NavRoutes.ADMIN_USERS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_profile" -> navController.navigate(NavRoutes.ADMIN_PROFILE) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    AdminUsersScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        composable(NavRoutes.ADMIN_TEAMS) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    AdminBottomNavigation(
                        selectedRoute = "admin_teams",
                        onNavigate = { route ->
                            when (route) {
                                "admin_dashboard" -> navController.navigate(NavRoutes.ADMIN_DASHBOARD) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD) { inclusive = true }
                                }
                                "admin_tickets" -> navController.navigate(NavRoutes.ADMIN_TICKETS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_teams" -> navController.navigate(NavRoutes.ADMIN_TEAMS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_users" -> navController.navigate(NavRoutes.ADMIN_USERS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_profile" -> navController.navigate(NavRoutes.ADMIN_PROFILE) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    AdminTeamsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // ==========================================
        // INTEGRANTE 5: PERFIL Y REPORTES
        // ==========================================
        composable(NavRoutes.USER_PROFILE) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    UserBottomNavigation(
                        selectedRoute = "user_profile",
                        onNavigate = { route ->
                            when (route) {
                                "user_dashboard" -> navController.navigate(NavRoutes.USER_DASHBOARD) {
                                    popUpTo(NavRoutes.USER_DASHBOARD) { inclusive = true }
                                }
                                "user_tickets" -> navController.navigate(NavRoutes.USER_TICKETS) {
                                    popUpTo(NavRoutes.USER_DASHBOARD)
                                }
                                "user_profile" -> navController.navigate(NavRoutes.USER_PROFILE) {
                                    popUpTo(NavRoutes.USER_DASHBOARD)
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    UserProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSignOut = { performSignOut() }
                    )
                }
            }
        }

        composable(NavRoutes.USER_EDIT_PROFILE) {
            // No se necesita, la edición está integrada en UserProfileScreen
            navController.popBackStack()
        }

        composable(NavRoutes.ADMIN_PROFILE) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    AdminBottomNavigation(
                        selectedRoute = "admin_profile",
                        onNavigate = { route ->
                            when (route) {
                                "admin_dashboard" -> navController.navigate(NavRoutes.ADMIN_DASHBOARD) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD) { inclusive = true }
                                }
                                "admin_tickets" -> navController.navigate(NavRoutes.ADMIN_TICKETS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_teams" -> navController.navigate(NavRoutes.ADMIN_TEAMS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_users" -> navController.navigate(NavRoutes.ADMIN_USERS) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                                "admin_profile" -> navController.navigate(NavRoutes.ADMIN_PROFILE) {
                                    popUpTo(NavRoutes.ADMIN_DASHBOARD)
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    AdminProfileScreen(
                        onSignOut = { performSignOut() }
                    )
                }
            }
        }

        composable(NavRoutes.ADMIN_REPORTS) {
            AdminReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
