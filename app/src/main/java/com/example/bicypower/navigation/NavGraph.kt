package com.example.bicypower.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import bottomRoutes
import com.example.bicypower.data.CartStore
import com.example.bicypower.data.local.session.UserSession
import com.example.bicypower.data.remote.dto.ProductDtoRemote
import com.example.bicypower.ui.components.AppBottomBar
import com.example.bicypower.ui.screen.*
import com.example.bicypower.ui.screen.admin.AdminHomeScreen
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val session = remember { UserSession(context) }

    val isLoggedIn by session.isLoggedIn.collectAsState(initial = false)
    val role by session.role.collectAsState(initial = "")
    val userId by session.userId.collectAsState(initial = 0L)

    val startDest = remember(isLoggedIn, role) {
        when {
            !isLoggedIn -> Routes.LOGIN
            role == "ADMIN" -> Routes.ADMIN_HOME
            role == "STAFF" -> Routes.STAFF_HOME
            else -> Routes.HOME
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
                    AppBottomBar(navController = navController, cartCount = cartCount)
                }
            }
        ) { inner ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(inner)
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenProduct = { id -> navController.navigate(Routes.product(id)) },
                        onAddToCart = { p: ProductDtoRemote -> CartStore.add(p) }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onOpenOrders = { navController.navigate(Routes.ORDERS) },
                        onOpenAddresses = { navController.navigate(Routes.ADDRESSES) },
                        onOpenPayments = { navController.navigate(Routes.PAYMENTS) }
                    )
                }

                composable(Routes.CART) {
                    CartScreen(onCheckout = { navController.navigate(Routes.ORDERS) })
                }

                composable(Routes.SUPPORT) { SupportScreen() }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onLogout = {
                            scope.launch {
                                session.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                        onCheckBike = { navController.navigate(Routes.CHECK_BIKE) }
                    )
                }

                composable(Routes.CHECK_BIKE) {
                    CheckBikeScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.CHANGE_PASSWORD) {
                    ChangePasswordScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.ADMIN_HOME) {
                    AdminHomeScreen(
                        onLogout = {
                            scope.launch {
                                session.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable(Routes.STAFF_HOME) {
                    StaffHomeScreen(
                        onLogout = {
                            scope.launch {
                                session.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable(Routes.LOGIN) {
                    LoginScreenModern(
                        onLoginOk = { user: AuthUser ->
                            scope.launch {
                                // ✅ tu UserSession tiene setLoggedIn, no setSession
                                session.setLoggedIn(
                                    userId = user.id,
                                    role = user.role,
                                    name = user.name,
                                    email = user.email
                                )
                            }

                            when (user.role) {
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
                        onGoVerifyCode = { email -> navController.navigate(Routes.verifyCode(email)) }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreenVm(
                        onRegisteredNavigateVerify = { email ->
                            navController.popBackStack()
                            navController.navigate(Routes.verifyCode(email)) { launchSingleTop = true }
                        },
                        onGoLogin = {
                            navController.popBackStack()
                            navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                        }
                    )
                }

                composable(Routes.FORGOT) {
                    ForgotPasswordScreenVm(
                        onCodeSentNavigateReset = { email ->
                            navController.navigate(Routes.resetPassword(email)) { launchSingleTop = true }
                        },
                        onGoLogin = {
                            navController.popBackStack()
                            navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                        }
                    )
                }

                composable(
                    route = Routes.RESET_PASSWORD,
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    ResetPasswordScreenVm(
                        email = email,
                        onResetOkGoLogin = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = Routes.VERIFY_CODE,
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
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
                        onBackToLogin = { navController.popBackStack() }
                    )
                }

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

                composable(Routes.ORDERS) {
                    OrdersScreen(
                        onBack = { navController.popBackStack() },
                        userId = userId
                    )
                }

                composable(Routes.ADDRESSES) { AddressesScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.PAYMENTS) { PaymentMethodsScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}

data class AuthUser(
    val id: Long,
    val role: String,
    val name: String,
    val email: String
)
