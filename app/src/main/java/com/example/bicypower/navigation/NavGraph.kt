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
import com.example.bicypower.ui.components.AppBottomBar
import com.example.bicypower.ui.screen.*
import com.example.bicypower.ui.screen.admin.AdminHomeScreen
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.bicypower.data.local.storage.UserPreferences

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    // ---- Sesión persistida (flows) ----
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }

    // Si ya tienes esto afuera en otro archivo, puedes borrarlo:
    val bottomRoutes = remember {
        setOf(Routes.HOME, Routes.PROFILE, Routes.CART, Routes.SUPPORT, Routes.SETTINGS)
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomRoutes

    val cartCount by CartStore.items.map { it.values.sum() }.collectAsState(initial = 0)
    val scope = rememberCoroutineScope()

    // Ruta SPLASH local (si ya tienes Routes.SPLASH, úsala y borra esto)
    val splashRoute = "splash"

    Scaffold(
        bottomBar = {
            if (showBottomBar) AppBottomBar(navController = navController, cartCount = cartCount)
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = splashRoute, // ✅ SIEMPRE SPLASH
            modifier = Modifier.padding(inner)
        ) {
            // ---------- SPLASH / DECIDER ----------
            composable(splashRoute) {
                SplashDecider(
                    onGoLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(splashRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoUserHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(splashRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoAdminHome = {
                        navController.navigate(Routes.ADMIN_HOME) {
                            popUpTo(splashRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ---------- TABS ----------
            composable(Routes.HOME) { _: NavBackStackEntry ->
                HomeScreen(
                    onOpenProduct = { id: String -> navController.navigate(Routes.product(id)) },
                    onAddToCart   = { p: Product -> CartStore.add(p.id) }
                )
            }
            composable(Routes.PROFILE)  { _: NavBackStackEntry -> ProfileScreen() }
            composable(Routes.CART)     { _: NavBackStackEntry -> CartScreen() }
            composable(Routes.SUPPORT)  { _: NavBackStackEntry -> SupportScreen() }
            composable(Routes.SETTINGS) { _: NavBackStackEntry ->
                SettingsScreen(
                    onLogout = {
                        scope.launch {
                            prefs.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true } // limpia backstack
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
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // ---------- AUTH ----------
            composable(Routes.LOGIN) { _: NavBackStackEntry ->
                LoginScreenVm(
                    onLoginOk = { roleLogged ->
                        // ✅ Ya guardamos sesión en LoginScreenVm de forma atómica.
                        // Aquí solo navegamos y limpiamos el backstack.
                        when (roleLogged) {
                            "ADMIN" -> navController.navigate(Routes.ADMIN_HOME) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            else -> navController.navigate(Routes.HOME) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
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
                ProductDetailScreen(
                    productId = id,
                    onBack = { navController.popBackStack() },
                    onGoToCart = { navController.navigate(Routes.CART) }
                )
            }
        }
    }
}

/**
 * Pantalla ultra-simple que decide a dónde ir según DataStore.
 * Se ejecuta UNA sola vez por arranque, limpia backstack y navega.
 */
@Composable
private fun SplashDecider(
    onGoLogin: () -> Unit,
    onGoUserHome: () -> Unit,
    onGoAdminHome: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val isLoggedIn by prefs.isLoggedIn.collectAsState(initial = false)
    val role by prefs.role.collectAsState(initial = "")

    // Navega una vez cuando tengamos los valores
    LaunchedEffect(isLoggedIn, role) {
        if (!isLoggedIn) {
            onGoLogin()
        } else {
            if (role == "ADMIN") onGoAdminHome() else onGoUserHome()
        }
    }

    // Aquí podrías mostrar un logotipo o un loader.
}
