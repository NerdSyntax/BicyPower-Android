package com.example.bicypower.navigation

object Routes {
    // Tabs cliente
    const val HOME     = "home"
    const val PROFILE  = "profile"
    const val CART     = "cart"
    const val SUPPORT  = "support"
    const val SETTINGS = "settings"

    // Admin
    const val ADMIN_HOME = "admin_home"

    // Staff
    const val STAFF_HOME = "staff_home"

    // Auth
    const val LOGIN      = "login"
    const val REGISTER   = "register"
    const val FORGOT     = "forgot"
    const val VERIFY_CODE = "verifyCode/{email}"

    // helper para navegar con email
    fun verifyCode(email: String) = "verifyCode/$email"

    // Cambiar contraseña
    const val CHANGE_PASSWORD = "change_password"

    // Perfil -> subsecciones
    const val ORDERS    = "orders"
    const val ADDRESSES = "addresses"
    const val PAYMENTS  = "payments"

    // Producto detalle
    const val PRODUCT = "product/{id}"
    fun product(id: String) = "product/$id"
}

// Rutas que muestran la bottom bar (rol cliente)
val bottomRoutes = setOf(
    Routes.HOME,
    Routes.PROFILE,
    Routes.CART,
    Routes.SUPPORT,
    Routes.SETTINGS
)
