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
            role == "STAFF" -> Routes.STAFF_HOME
            else            -> Routes.HOME   // CLIENT u otro
        }
    }

    key(startDest) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        val showBottomBar = currentRoute in bottomRoutes

        val cartCount by CartStore.items.map { it.values.sum() }.collectAsState(initial = 0)
        val scope = rememberCoroutineScope()

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        navController = navController,
                        cartCount = cartCount
                    )
                }
            }
        ) { inner ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(inner)
            ) {

                // ----------------- HOME CLIENTE -----------------
                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenProduct = { id: String ->
                            navController.navigate(Routes.product(id))
                        },
                        onAddToCart = { p: Product ->
                            CartStore.add(p.id)
                        }
                    )
                }

                // ----------------- PROFILE CLIENTE -----------------
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onOpenOrders = { navController.navigate(Routes.ORDERS) },
                        onOpenAddresses = { navController.navigate(Routes.ADDRESSES) },
                        onOpenPayments = { navController.navigate(Routes.PAYMENTS) }
                    )
                }

                // ----------------- CART -----------------
                composable(Routes.CART) {
                    CartScreen(
                        onCheckout = {
                            navController.navigate(Routes.ORDERS)
                        }
                    )
                }

                // ----------------- SUPPORT -----------------
                composable(Routes.SUPPORT) {
                    SupportScreen()
                }

                // ----------------- SETTINGS -----------------
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
                        },
                        onChangePassword = {
                            navController.navigate(Routes.CHANGE_PASSWORD)
                        }
                    )
                }

                // ----------------- CHANGE PASSWORD -----------------
                composable(Routes.CHANGE_PASSWORD) {
                    ChangePasswordScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ----------------- ADMIN -----------------
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

                // ----------------- STAFF -----------------
                composable(Routes.STAFF_HOME) {
                    StaffHomeScreen(
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

                // ----------------- AUTH -----------------
                composable(Routes.LOGIN) {
                    LoginScreenModern(
                        onLoginOk = { roleLogged ->
                            scope.launch { prefs.setSession(true, roleLogged) }

                            when (roleLogged) {
                                "ADMIN" -> navController.navigate(Routes.ADMIN_HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                    launchSingleTop = true
                                }
                                "STAFF" -> navController.navigate(Routes.STAFF_HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                    launchSingleTop = true
                                }
                                else -> navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onGoRegister = { navController.navigate(Routes.REGISTER) },
                        onGoForgot = { navController.navigate(Routes.FORGOT) },
                        onGoVerifyCode = { email ->
                            navController.navigate(Routes.verifyCode(email))
                        }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreenVm(
                        onRegisteredNavigateVerify = { email ->
                            // sacamos REGISTER del backstack
                            navController.popBackStack()
                            // vamos a la pantalla de código con el correo correcto
                            navController.navigate(Routes.verifyCode(email)) {
                                launchSingleTop = true
                            }
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

                // ----------- VERIFY CODE -----------
                composable(
                    route = Routes.VERIFY_CODE,
                    arguments = listOf(
                        navArgument("email") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    VerifyCodeScreenVm(
                        email = email,
                        onVerified = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onBackToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // ----------------- PRODUCT DETAIL -----------------
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

                // ----------------- PROFILE SUB SCREENS -----------------
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
