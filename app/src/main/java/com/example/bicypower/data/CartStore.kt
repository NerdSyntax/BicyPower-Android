package com.example.bicypower.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Carrito simple en memoria (suficiente para las pantallas de demo)
object CartStore {
    // Mapa productId -> cantidad
    private val _items = MutableStateFlow<Map<String, Int>>(emptyMap())
    val items: StateFlow<Map<String, Int>> = _items.asStateFlow()

    fun add(productId: String, qty: Int = 1) {
        if (qty <= 0) return
        _items.value = _items.value.toMutableMap().apply {
            val current = get(productId) ?: 0
            put(productId, current + qty)
        }
    }

    fun set(productId: String, qty: Int) {
        _items.value = _items.value.toMutableMap().apply {
            if (qty <= 0) remove(productId) else put(productId, qty)
        }
    }

    fun remove(productId: String) {
        _items.value = _items.value.toMutableMap().apply { remove(productId) }
    }

    fun clear() { _items.value = emptyMap() }

    fun count(): Int = _items.value.values.sum()

    fun total(): Double = _items.value.entries.sumOf { (id, q) ->
        (Catalog.byId(id)?.price ?: 0.0) * q
    }

    // Útil para el CartScreen: items con el producto expandido
    data class DetailedItem(val product: Product, val quantity: Int, val lineTotal: Double)

    fun detailed(): List<DetailedItem> =
        _items.value.mapNotNull { (id, q) ->
            Catalog.byId(id)?.let { p -> DetailedItem(p, q, p.price * q) }
        }
}
