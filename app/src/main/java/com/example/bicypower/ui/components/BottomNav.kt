package com.example.bicypower.ui.components

import Routes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AppBottomBar(
    navController: NavHostController,
    cartCount: Int
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String? = backStackEntry?.destination?.route

    val items = listOf(
        BottomItem(Routes.HOME, "Tienda", Icons.Filled.Home),
        BottomItem(Routes.PROFILE, "Perfil", Icons.Filled.Person),
        BottomItem(Routes.CART, "Carrito", Icons.Filled.ShoppingCart),
        BottomItem(Routes.SUPPORT, "Ayuda", Icons.Filled.Help),
        BottomItem(Routes.SETTINGS, "Ajustes", Icons.Filled.Settings),
    )

    NavigationBar {
        items.forEach { item ->
            val selected = isSameRoute(currentRoute, item.route)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    val popped = navController.popBackStack(item.route, inclusive = false)
                    if (!popped) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (item.route == Routes.CART && cartCount > 0) {
                        BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) }
            )
        }
    }
}

private fun isSameRoute(currentRoute: String?, tabRoute: String): Boolean {
    if (currentRoute == null) return false
    val plain = currentRoute.substringBefore("?")
    return plain == tabRoute || plain.startsWith("$tabRoute/")
}
