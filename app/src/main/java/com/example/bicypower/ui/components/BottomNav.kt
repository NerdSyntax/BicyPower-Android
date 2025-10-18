package com.example.bicypower.ui.components



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

object BottomDestinations {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val CART = "cart"
    const val SUPPORT = "support"
    const val SETTINGS = "settings"
}

val bottomItems = listOf(
    BottomItem(BottomDestinations.HOME, "Home", Icons.Filled.Home),
    BottomItem(BottomDestinations.PROFILE, "Profile", Icons.Filled.Person),
    BottomItem(BottomDestinations.CART, "Cart", Icons.Filled.ShoppingCart),
    BottomItem(BottomDestinations.SUPPORT, "Support", Icons.Filled.HeadsetMic),
    BottomItem(BottomDestinations.SETTINGS, "Settings", Icons.Filled.Settings),
)

@Composable
fun AppBottomBar(
    navController: NavHostController,
    cartCount: Int = 0, // pásame el total del carrito cuando lo tengas
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomItems.forEach { item ->
            val selected = currentDestination.isRouteSelected(item.route)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (item.route == BottomDestinations.CART && cartCount > 0) {
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

private fun NavDestination?.isRouteSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
