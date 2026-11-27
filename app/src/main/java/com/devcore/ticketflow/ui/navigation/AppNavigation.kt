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
import com.devcore.ticketflow.SupabaseClient
import com.devcore.ticketflow.ui.components.AdminBottomNavigation
import com.devcore.ticketflow.ui.components.UserBottomNavigation
import com.devcore.ticketflow.ui.screens.admin.AdminDashboardScreen
import com.devcore.ticketflow.ui.screens.admin.AdminHistoryScreen
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
                throw e
            } catch (e: Exception) {
                // signOut failure should not prevent local navigation
            }
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Navegación centralizada para la barra inferior de usuario con prevención de duplicados
    fun navigateUser(route: String) {
        when (route) {
            "user_dashboard" -> navController.navigate(NavRoutes.USER_DASHBOARD) {
                popUpTo(NavRoutes.USER_DASHBOARD) { inclusive = true }
                launchSingleTop = true
            }
            "user_tickets" -> navController.navigate(NavRoutes.USER_TICKETS) {
                popUpTo(NavRoutes.USER_DASHBOARD)
                launchSingleTop = true
            }
            "user_profile" -> navController.navigate(NavRoutes.USER_PROFILE) {
                popUpTo(NavRoutes.USER_DASHBOARD)
                launchSingleTop = true
            }
        }
    }

    // Navegación centralizada para la barra inferior de administrador con prevención de duplicados
    fun navigateAdmin(route: String) {
        when (route) {
            "admin_dashboard" -> navController.navigate(NavRoutes.ADMIN_DASHBOARD) {
                popUpTo(NavRoutes.ADMIN_DASHBOARD) { inclusive = true }
                launchSingleTop = true
            }
            "admin_tickets" -> navController.navigate(NavRoutes.ADMIN_TICKETS) {
                popUpTo(NavRoutes.ADMIN_DASHBOARD)
                launchSingleTop = true
            }
            "admin_teams" -> navController.navigate(NavRoutes.ADMIN_TEAMS) {
                popUpTo(NavRoutes.ADMIN_DASHBOARD)
                launchSingleTop = true
            }
            "admin_users" -> navController.navigate(NavRoutes.ADMIN_USERS) {
                popUpTo(NavRoutes.ADMIN_DASHBOARD)
                launchSingleTop = true
            }
            "admin_reports" -> navController.navigate(NavRoutes.ADMIN_REPORTS) {
                popUpTo(NavRoutes.ADMIN_DASHBOARD)
                launchSingleTop = true
            }
            "admin_profile" -> navController.navigate(NavRoutes.ADMIN_PROFILE) {
                popUpTo(NavRoutes.ADMIN_DASHBOARD)
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOGIN,
        modifier = modifier
    ) {
        // ==========================================
        // AUTENTICACIÓN
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

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onRegistrationCompleted = { _ -> }
            )
        }

        // ==========================================
        // DASHBOARD Y TICKETS DE USUARIO
        // ==========================================
        composable(NavRoutes.USER_DASHBOARD) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    UserBottomNavigation(
                        selectedRoute = "user_dashboard",
                        onNavigate = ::navigateUser
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
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

        composable(NavRoutes.CREATE_TICKET) {
            CreateTicketScreen(
                onNavigateBack = { navController.popBackStack() },
                onTicketCreated = { ticketId ->
                    navController.navigate(NavRoutes.ticketDetail(ticketId)) {
                        popUpTo(NavRoutes.CREATE_TICKET) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.USER_TICKETS) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    UserBottomNavigation(
                        selectedRoute = "user_tickets",
                        onNavigate = ::navigateUser
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
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
        // MÓDULOS DE ADMINISTRADOR
        // ==========================================
        composable(NavRoutes.ADMIN_DASHBOARD) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    AdminBottomNavigation(
                        selectedRoute = "admin_dashboard",
                        onNavigate = ::navigateAdmin
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
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
                        onNavigateToHistory = {
                            navController.navigate(NavRoutes.ADMIN_HISTORY)
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
                        onNavigate = ::navigateAdmin
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
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
                        onNavigate = ::navigateAdmin
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
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
                        onNavigate = ::navigateAdmin
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AdminTeamsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // ==========================================
        // PERFILES Y REPORTES
        // ==========================================
        composable(NavRoutes.USER_PROFILE) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    UserBottomNavigation(
                        selectedRoute = "user_profile",
                        onNavigate = ::navigateUser
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    UserProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSignOut = { performSignOut() }
                    )
                }
            }
        }

        composable(NavRoutes.ADMIN_PROFILE) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    AdminBottomNavigation(
                        selectedRoute = "admin_profile",
                        onNavigate = ::navigateAdmin
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AdminProfileScreen(
                        onSignOut = { performSignOut() }
                    )
                }
            }
        }

        composable(NavRoutes.ADMIN_REPORTS) {
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    AdminBottomNavigation(
                        selectedRoute = "admin_reports",
                        onNavigate = ::navigateAdmin
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AdminReportsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        composable(NavRoutes.ADMIN_HISTORY) {
            AdminHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { ticketId ->
                    navController.navigate(NavRoutes.ticketDetail(ticketId))
                }
            )
        }
    }
}
