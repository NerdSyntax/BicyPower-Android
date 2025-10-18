package com.example.bicypower.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bicypower.ui.components.AppBottomBar
import com.example.bicypower.ui.screen.*

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomRoutes  // LOGIN/REGISTER/FORGOT no están en bottomRoutes

    Scaffold(
        bottomBar = { if (showBottomBar) AppBottomBar(navController = navController, cartCount = 0) }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,  // parte en Home
            modifier = Modifier.padding(inner)
        ) {
            // ---------- TABS ----------
            composable(Routes.HOME) {
                HomeScreen(
                    onGoLogin = { navController.navigate(Routes.LOGIN) },
                    onGoRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.PROFILE)  { ProfileScreen() }
            composable(Routes.CART)     { CartScreen() }
            composable(Routes.SUPPORT)  { SupportScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLogout = {
                        // Ir a Login y limpiar la pila para que no vuelva a Home con back
                        navController.navigate(Routes.LOGIN) {
                            // Si tu versión soporta popUpTo(route:String), usa esta:
                            popUpTo(Routes.HOME) { inclusive = true }
                            // Si no, usa el startDestinationId:
                            // popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ---------- AUTH ----------
            composable(Routes.LOGIN) {
                LoginScreenVm(
                    onLoginOkNavigateHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoRegister = { navController.navigate(Routes.REGISTER) },
                    onGoForgot = { navController.navigate(Routes.FORGOT) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreenVm(
                    onRegisteredNavigateLogin = {
                        navController.popBackStack()
                        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                    },
                    onGoLogin = {
                        navController.popBackStack()
                        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                    }
                )
            }
            composable(Routes.FORGOT) {
                ForgotPasswordScreenVm(
                    onEmailSentNavigateLogin = {
                        navController.popBackStack()
                        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                    },
                    onGoLogin = {
                        navController.popBackStack()
                        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                    }
                )
            }
        }
    }
}
