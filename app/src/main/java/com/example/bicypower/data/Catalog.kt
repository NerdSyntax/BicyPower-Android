package com.example.bicypower.data

// Modelo simple que varias pantallas usan
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val emoji: String = ""
)

object Catalog {
    // Catálogo de ejemplo (IDs en string porque así lo usas en navegación)
    val all: List<Product> = listOf(
        Product(id = "p1", name = "MTB X-200", description = "Mountain bike aluminio", price = 499_990.0, emoji = "🚵"),
        Product(id = "p2", name = "Casco Pro", description = "Casco certificado", price = 39_990.0, emoji = "🪖"),
        Product(id = "p3", name = "Luces LED", description = "Kit delantera/trasera USB", price = 14_990.0, emoji = "💡"),
        Product(id = "p4", name = "Guantes Gel", description = "Antideslizantes", price = 12_990.0, emoji = "🧤")
    )

    fun byId(id: String): Product? = all.find { it.id == id }
}
