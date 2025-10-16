package com.example.bicypower.navigation

// Clase sellada para rutas: evita "strings mágicos" y facilita refactors
sealed class Route(val path: String) {
    data object Home     : Route("home")
    data object Login    : Route("login")
    data object Register : Route("register")
}

/*
 * Centraliza los strings de rutas. Si cambias "home" por "inicio",
 * solo lo modificas aquí.
 */
