package com.example.bicypower.navigation

object Routes {
    // Auth
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val FORGOT = "auth/forgot"

    // Bottom tabs
    const val HOME = "home"
    const val PROFILE = "profile"
    const val CART = "cart"
    const val SUPPORT = "support"
    const val SETTINGS = "settings"
}

// Para saber cuándo mostrar la bottom bar
val bottomRoutes = setOf(
    Routes.HOME, Routes.PROFILE, Routes.CART, Routes.SUPPORT, Routes.SETTINGS
)
