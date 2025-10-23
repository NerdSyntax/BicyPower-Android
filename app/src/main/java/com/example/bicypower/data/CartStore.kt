package com.example.bicypower.data

import com.example.bicypower.data.local.product.ProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Carrito simple en memoria (suficiente para las pantallas de demo)
object CartStore {
    // Mapa productId -> cantidad (productId puede ser: "p1" del catálogo fijo o "db:<id>" desde Room)
    private val _items = MutableStateFlow<Map<String, Int>>(emptyMap())
    val items: StateFlow<Map<String, Int>> = _items.asStateFlow()

    // Snapshot de productos provenientes de DB para poder calcular total y detallar
    // Clave: "db:<idRoom>"
    private val dbSnapshots = mutableMapOf<String, Product>()

    fun add(productId: String, qty: Int = 1) {
        if (qty <= 0) return
        _items.value = _items.value.toMutableMap().apply {
            val current = get(productId) ?: 0
            put(productId, current + qty)
        }
    }

    // Nuevo: agregar usando un ProductEntity de Room
    fun addDb(p: ProductEntity, qty: Int = 1) {
        val id = "db:${p.id}"
        // Guardamos un snapshot con el modelo Product (para reusar la UI del carrito)
        dbSnapshots[id] = Product(
            id = id,
            name = p.name,
            description = p.description,
            price = p.price,
            emoji = "🚲"
        )
        add(id, qty)
    }

    fun set(productId: String, qty: Int) {
        _items.value = _items.value.toMutableMap().apply {
            if (qty <= 0) remove(productId) else put(productId, qty)
        }
    }

    fun remove(productId: String) {
        _items.value = _items.value.toMutableMap().apply { remove(productId) }
    }

    fun clear() {
        _items.value = emptyMap()
    }

    fun count(): Int = _items.value.values.sum()

    fun total(): Double = _items.value.entries.sumOf { (id, q) ->
        val price = Catalog.byId(id)?.price ?: dbSnapshots[id]?.price ?: 0.0
        price * q
    }

    // Útil para el CartScreen: items con el producto expandido (usamos Product también para los de DB)
    data class DetailedItem(val product: Product, val quantity: Int, val lineTotal: Double)

    fun detailed(): List<DetailedItem> =
        _items.value.mapNotNull { (id, q) ->
            val p: Product? = Catalog.byId(id) ?: dbSnapshots[id]
            p?.let { DetailedItem(it, q, it.price * q) }
        }
}
