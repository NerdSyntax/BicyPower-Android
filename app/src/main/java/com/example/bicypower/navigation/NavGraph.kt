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
import com.example.bicypower.ui.screen.HomeScreen
import com.example.bicypower.ui.screen.LoginScreenVm
import com.example.bicypower.ui.screen.RegisterScreenVm

@Composable
fun AppNavGraph(navController: NavHostController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Ruta actual (si quieres resaltar en el Drawer)
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    // Helpers
    val goHome: () -> Unit     = { navController.navigate(Route.Home.path)     { launchSingleTop = true } }
    val goLogin: () -> Unit    = { navController.navigate(Route.Login.path)    { launchSingleTop = true } }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) { launchSingleTop = true } }

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
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) {
                    HomeScreen(onGoLogin = goLogin, onGoRegister = goRegister)
                }
                composable(Route.Login.path) {
                    LoginScreenVm(
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }
            }
        }
    }
}
