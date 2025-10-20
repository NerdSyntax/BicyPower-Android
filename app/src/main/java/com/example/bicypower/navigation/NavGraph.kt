package com.example.bicypower.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.Product
import com.example.bicypower.ui.components.AppBottomBar
import com.example.bicypower.ui.screen.*
import com.example.bicypower.ui.screen.admin.AdminHomeScreen
import kotlinx.coroutines.flow.map

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomRoutes

    val cartCount by CartStore.items.map { it.values.sum() }.collectAsState(initial = 0)

    Scaffold(
        bottomBar = { if (showBottomBar) AppBottomBar(navController = navController, cartCount = cartCount) }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(inner)
        ) {
            // ---------- TABS ----------
            composable(Routes.HOME) { _: NavBackStackEntry ->
                // HomeScreen te entrega Product -> usa p.id
                HomeScreen(
                    onOpenProduct = { id: String -> navController.navigate(Routes.product(id)) },
                    onAddToCart   = { id: String -> CartStore.add(id) } as (Product) -> Unit
                )

            }
            composable(Routes.PROFILE) { _: NavBackStackEntry -> ProfileScreen() }
            composable(Routes.CART)    { _: NavBackStackEntry -> CartScreen() }
            composable(Routes.SUPPORT) { _: NavBackStackEntry -> SupportScreen() }
            composable(Routes.SETTINGS) { _: NavBackStackEntry ->
                SettingsScreen(onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }

            // ---------- ADMIN ----------
            composable(Routes.ADMIN_HOME) { _: NavBackStackEntry -> AdminHomeScreen() }

            // ---------- AUTH ----------
            composable(Routes.LOGIN) { _: NavBackStackEntry ->
                LoginScreenVm(
                    onLoginOk = { role ->
                        when (role) {
                            "ADMIN" -> navController.navigate(Routes.ADMIN_HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }; launchSingleTop = true
                            }
                            else -> navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }; launchSingleTop = true
                            }
                        }
                    },
                    onGoRegister = { navController.navigate(Routes.REGISTER) },
                    onGoForgot   = { navController.navigate(Routes.FORGOT) }
                )
            }
            composable(Routes.REGISTER) { _: NavBackStackEntry ->
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
            composable(Routes.FORGOT) { _: NavBackStackEntry ->
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

            // ---------- PRODUCT DETAIL ----------
            composable(
                route = Routes.PRODUCT,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack: NavBackStackEntry ->
                val id = backStack.arguments?.getString("id") ?: return@composable
                ProductDetailScreen(productId = id)
            }
        }
    }
}
