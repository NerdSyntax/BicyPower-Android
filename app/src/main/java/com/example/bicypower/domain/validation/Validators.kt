package com.example.bicypower.domain.validation

import android.util.Patterns

// ----------------------------------------------------------------------
// VALIDACIONES GENERALES (USUARIO / STAFF / REGISTRO)
// ----------------------------------------------------------------------

// Email
fun validateEmail(email: String): String? {
    if (email.isBlank()) return "El email es obligatorio"
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    return if (!ok) "Formato de email inválido" else null
}

// Nombre genérico: solo letras y espacios
fun validateNameLettersOnly(name: String): String? {
    if (name.isBlank()) return "El nombre es obligatorio"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(name)) "Solo letras y espacios" else null
}

// Teléfono
fun validatePhoneDigitsOnly(phone: String): String? {
    if (phone.isBlank()) return "El teléfono es obligatorio"
    if (!phone.all { it.isDigit() }) return "Solo números"
    if (phone.length !in 8..15) return "Debe tener entre 8 y 15 dígitos"
    return null
}

// Contraseña fuerte
fun validateStrongPassword(pass: String): String? {
    if (pass.isBlank()) return "La contraseña es obligatoria"
    if (pass.length < 8) return "Mínimo 8 caracteres"
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula"
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula"
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo"
    if (pass.contains(' ')) return "No debe contener espacios"
    return null
}

// Confirmación
fun validateConfirm(pass: String, confirm: String): String? {
    if (confirm.isBlank()) return "Confirma tu contraseña"
    return if (pass != confirm) "Las contraseñas no coinciden" else null
}

// ----------------------------------------------------------------------
// VALIDACIONES ADMIN (PRODUCTOS)
// ----------------------------------------------------------------------

// Nombre de producto: obligatorio, solo letras y espacios
fun validateProductName(name: String): String? {
    if (name.isBlank()) return "El nombre es obligatorio"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(name))
        "El nombre no debe contener números ni símbolos"
    else null
}

// Campo obligatorio genérico
fun validateRequired(value: String, fieldName: String): String? {
    return if (value.isBlank()) "$fieldName es obligatorio" else null
}

// Precio: obligatorio, numérico y NO negativo
fun validatePrice(price: String): String? {
    if (price.isBlank()) return "El precio es obligatorio"

    val numero = price.trim().toDoubleOrNull()
        ?: return "El precio debe ser un número válido"

    if (numero < 0.0) return "El precio no puede ser negativo"
    return null
}

// Stock: obligatorio, numérico y NO negativo
fun validateStock(stock: String): String? {
    if (stock.isBlank()) return "El stock es obligatorio"

    val numero = stock.trim().toIntOrNull()
        ?: return "El stock debe ser un número válido"

    if (numero < 0) return "El stock no puede ser negativo"
    return null
}
