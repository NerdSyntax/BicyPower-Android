package com.example.bicypower.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

import com.example.bicypower.ui.components.AppTopBar
import com.example.bicypower.ui.components.AppDrawer
import com.example.bicypower.ui.components.defaultDrawerItems

@Composable
fun AppNavGraph(navController: NavHostController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Ruta actual
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val startRoute = Route.Login.path

    // Rutas de autenticación (ocultan Drawer/TopBar)
    val authRoutes = setOf(Route.Login.path, Route.Register.path, Route.Forgot.path)
    val isAuthRoute = (currentRoute ?: startRoute) in authRoutes

    // Helpers de navegación
    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            // limpia pantallas de auth del back stack
            popUpTo(Route.Login.path) { inclusive = true }
            launchSingleTop = true
        }
    }
    val goLogin: () -> Unit = {
        navController.navigate(Route.Login.path) { launchSingleTop = true }
    }
    val goRegister: () -> Unit = {
        navController.navigate(Route.Register.path) { launchSingleTop = true }
    }
    val goForgot: () -> Unit = {
        navController.navigate(Route.Forgot.path) { launchSingleTop = true }
    }

    // Contenido principal (NavHost)
    val content: @Composable () -> Unit = {
        NavHost(
            navController = navController,
            startDestination = startRoute
        ) {
            composable(Route.Login.path) {
                com.example.bicypower.ui.screen.LoginScreenVm(
                    onLoginOkNavigateHome = goHome,
                    onGoRegister = goRegister,
                    onGoForgot = goForgot
                )
            }
            composable(Route.Register.path) {
                com.example.bicypower.ui.screen.RegisterScreenVm(
                    onRegisteredNavigateLogin = goLogin,
                    onGoLogin = goLogin
                )
            }
            composable(Route.Forgot.path) {
                com.example.bicypower.ui.screen.ForgotPasswordScreenVm(
                    onEmailSentNavigateLogin = goLogin,
                    onGoLogin = goLogin
                )
            }
            composable(Route.Home.path) {
                com.example.bicypower.ui.screen.HomeScreen(
                    onGoLogin = goLogin,
                    onGoRegister = goRegister
                )
            }
        }
    }

    if (isAuthRoute) {
        // En pantallas de auth NO mostramos menú ni topbar
        content()
    } else {
        // En el resto, Drawer + TopBar
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(
                    currentRoute = currentRoute,
                    items = defaultDrawerItems(
                        onHome = { scope.launch { drawerState.close() }; goHome() },
                        onLogin = { scope.launch { drawerState.close() }; goLogin() },
                        onRegister = { scope.launch { drawerState.close() }; goRegister() }
                    )
                )
            }
        ) {
            Scaffold(
                topBar = {
                    AppTopBar(
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onHome = goHome,
                        onLogin = goLogin,
                        onRegister = goRegister
                    )
                }
            ) { innerPadding ->
                androidx.compose.material3.Surface(Modifier.padding(innerPadding)) {
                    content()
                }
            }
        }
    }
}
