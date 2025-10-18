package com.example.bicypower.navigation

// Clase sellada para rutas: evita "strings mágicos" y facilita refactors
sealed class Route(val path: String) {
    data object Home     : Route("home")
    data object Login    : Route("login")
    data object Register : Route("register")
    data object Forgot   : Route("forgot")   // 👈 nueva ruta
}

/*
 * Centraliza los strings de rutas. Si cambias "home" por "inicio",
 * solo lo modificas aquí.
 */

// (opcional) set de rutas de autenticación para ocultar menú en AppNavGraph
val AUTH_ROUTES = setOf(Route.Login.path, Route.Register.path, Route.Forgot.path)
