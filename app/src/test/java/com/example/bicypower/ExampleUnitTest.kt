package com.example.bicypower

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios simples para validaciones básicas de BicyPower.
 * Se ejecutan en la JVM (no necesitan Android).
 */
class ExampleUnitTest {

    // --- Helper para validar email con una regex simple ---
    private fun isValidEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return regex.matches(email)
    }

    // ========================
    // 1) VALIDACIÓN DE EMAIL
    // ========================

    @Test
    fun emailInvalido_retornaFalse() {
        val email = "correo_invalido"
        assertFalse(isValidEmail(email))
    }

    @Test
    fun emailValido_retornaTrue() {
        val email = "usuario@gmail.com"
        assertTrue(isValidEmail(email))
    }

    // ========================
    // 2) VALIDACIÓN DE PRECIO
    // ========================

    @Test
    fun precioNegativo_retornaFalse() {
        val price = -10.0
        val esValido = price >= 0
        assertFalse(esValido)
    }

    @Test
    fun precioPositivo_retornaTrue() {
        val price = 5990.0
        val esValido = price >= 0
        assertTrue(esValido)
    }

    // ========================
    // 3) VALIDACIÓN DE STOCK
    // ========================

    @Test
    fun stockCero_esAgotado() {
        val stock = 0
        val esAgotado = stock <= 0
        assertTrue(esAgotado)
    }

    @Test
    fun stockMayorQueCero_disponible() {
        val stock = 5
        val disponible = stock > 0
        assertTrue(disponible)
    }

    // =====================================
    // 4) VALIDACIÓN DE NOMBRE DE PRODUCTO
    // =====================================

    @Test
    fun nombreVacio_noEsValido() {
        val name = ""
        val esValido = name.isNotBlank()
        assertFalse(esValido)
    }

    @Test
    fun nombreCorrecto_esValido() {
        val name = "Casco Pro"
        val esValido = name.isNotBlank()
        assertTrue(esValido)
    }

    // =====================================
    // 5) VALIDACIÓN NÚMERO DE SERIE BICICLETA
    // =====================================

    @Test
    fun serieCorta_noEsValida() {
        val serial = "123"
        val esValida = serial.length >= 6
        assertFalse(esValida)
    }

    @Test
    fun serieLarga_esValida() {
        val serial = "WTU122XC1505X"
        val esValida = serial.length >= 6
        assertTrue(esValida)
    }

    // =====================================
    // 6) VALIDACIÓN BÁSICA DE URL DE IMAGEN
    // =====================================

    @Test
    fun urlInvalida_retornaFalse() {
        val url = "imagen_sin_http"
        val esValida = url.startsWith("http")
        assertFalse(esValida)
    }

    @Test
    fun urlValida_retornaTrue() {
        val url = "https://misimagenes.com/foto.png"
        val esValida = url.startsWith("http")
        assertTrue(esValida)
    }
}
