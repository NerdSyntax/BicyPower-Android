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
    const val LOGIN       = "login"
    const val REGISTER    = "register"
    const val FORGOT      = "forgot"

    const val VERIFY_CODE    = "verifyCode/{email}"
    fun verifyCode(email: String) = "verifyCode/$email"

    const val RESET_PASSWORD = "reset_password/{email}"
    fun resetPassword(email: String) = "reset_password/$email"

    const val CHANGE_PASSWORD = "change_password"

    const val ORDERS    = "orders"
    const val ADDRESSES = "addresses"
    const val PAYMENTS  = "payments"

    const val PRODUCT = "product/{id}"
    fun product(id: String) = "product/$id"

    // 👉 API externa
    const val CHECK_BIKE = "check_bike"
}

// Rutas con bottom bar
val bottomRoutes = setOf(
    Routes.HOME,
    Routes.PROFILE,
    Routes.CART,
    Routes.SUPPORT,
    Routes.SETTINGS
)
