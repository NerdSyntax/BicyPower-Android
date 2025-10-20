package com.example.bicypower.navigation

object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val CART = "cart"
    const val SUPPORT = "support"
    const val SETTINGS = "settings"

    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"

    const val PRODUCT = "product/{id}"
    fun product(id: String) = "product/$id"

    const val ADMIN_HOME = "admin_home"
}

val bottomRoutes = setOf(Routes.HOME, Routes.PROFILE, Routes.CART, Routes.SUPPORT, Routes.SETTINGS)
