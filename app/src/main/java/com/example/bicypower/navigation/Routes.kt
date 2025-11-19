package com.example.bicypower.navigation

object Routes {
    // Tabs
    const val HOME = "home"
    const val PROFILE = "profile"
    const val CART = "cart"
    const val SUPPORT = "support"
    const val SETTINGS = "settings"

    // Admin
    const val ADMIN_HOME = "admin_home"

    // Auth
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"

    // Perfil -> subsecciones
    const val ORDERS = "orders"
    const val ADDRESSES = "addresses"
    const val PAYMENTS = "payments"

    // 👉 NUEVA RUTA: cambiar contraseña
    const val CHANGE_PASSWORD = "change_password"

    // Product detail
    const val PRODUCT = "product/{id}"
    fun product(id: String) = "product/$id"
}

val bottomRoutes = setOf(
    Routes.HOME, Routes.PROFILE, Routes.CART, Routes.SUPPORT, Routes.SETTINGS
)
