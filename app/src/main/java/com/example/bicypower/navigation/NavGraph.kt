package com.example.bicypower.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.Product
import com.example.bicypower.data.local.storage.UserPreferences
import com.example.bicypower.ui.components.AppBottomBar
import com.example.bicypower.ui.screen.*
import com.example.bicypower.ui.screen.admin.AdminHomeScreen
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    // Sesión persistida
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val isLoggedIn by prefs.isLoggedIn.collectAsState(initial = false)
    val role by prefs.role.collectAsState(initial = "")

    val startDest = remember(isLoggedIn, role) {
        when {
            !isLoggedIn     -> Routes.LOGIN
            role == "ADMIN" -> Routes.ADMIN_HOME
            else            -> Routes.HOME
        }
    }

    key(startDest) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        val showBottomBar = currentRoute in bottomRoutes

        val cartCount by CartStore.items.map { it.values.sum() }.collectAsState(initial = 0)
        val scope = rememberCoroutineScope()

        Scaffold(
            bottomBar = {
                if (showBottomBar) AppBottomBar(navController = navController, cartCount = cartCount)
            }
        ) { inner ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(inner)
            ) {
                // ---------- TABS ----------
                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenProduct = { id: String -> navController.navigate(Routes.product(id)) },
                        onAddToCart   = { p: Product -> CartStore.add(p.id) }
                    )
                }

                // Profile ahora recibe lambdas para abrir subpantallas
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onOpenOrders = { navController.navigate(Routes.ORDERS) },
                        onOpenAddresses = { navController.navigate(Routes.ADDRESSES) },
                        onOpenPayments = { navController.navigate(Routes.PAYMENTS) }
                    )
                }

                composable(Routes.CART)    { CartScreen() }
                composable(Routes.SUPPORT) { SupportScreen() }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onLogout = {
                            scope.launch {
                                prefs.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                // ---------- ADMIN ----------
                composable(Routes.ADMIN_HOME) {
                    AdminHomeScreen(
                        onLogout = {
                            scope.launch {
                                prefs.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                // ---------- AUTH ----------
                composable(Routes.LOGIN) {
                    LoginScreenVm(
                        onLoginOk = { roleLogged ->
                            scope.launch { prefs.setSession(true, roleLogged) }
                            if (roleLogged == "ADMIN") {
                                navController.navigate(Routes.ADMIN_HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }; launchSingleTop = true
                                }
                            } else {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }; launchSingleTop = true
                                }
                            }
                        },
                        onGoRegister = { navController.navigate(Routes.REGISTER) },
                        onGoForgot   = { navController.navigate(Routes.FORGOT) }
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

                // ---------- PRODUCT DETAIL ----------
                composable(
                    route = Routes.PRODUCT,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStack: NavBackStackEntry ->
                    val id = backStack.arguments?.getString("id") ?: return@composable
                    ProductDetailScreen(
                        productId = id,
                        onBack = { navController.popBackStack() },
                        onGoToCart = { navController.navigate(Routes.CART) }
                    )
                }

                // ---------- PROFILE SUBPAGES (con botón atrás) ----------
                composable(Routes.ORDERS) {
                    OrdersScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ADDRESSES) {
                    AddressesScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.PAYMENTS) {
                    PaymentMethodsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
