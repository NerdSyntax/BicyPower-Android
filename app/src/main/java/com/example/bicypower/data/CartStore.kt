package com.example.bicypower.data

import com.example.bicypower.data.remote.dto.ProductDtoRemote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CartStore {

    private val _items = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val items: StateFlow<Map<Long, Int>> = _items.asStateFlow()

    // snapshot por id para totales/detalle
    private val snapshots = mutableMapOf<Long, ProductDtoRemote>()

    fun add(product: ProductDtoRemote, qty: Int = 1) {
        val id = product.id ?: return
        if (qty <= 0) return

        snapshots[id] = product
        _items.value = _items.value.toMutableMap().apply {
            val current = get(id) ?: 0
            put(id, current + qty)
        }
    }

    fun set(productId: Long, qty: Int) {
        _items.value = _items.value.toMutableMap().apply {
            if (qty <= 0) remove(productId) else put(productId, qty)
        }
        if (qty <= 0) snapshots.remove(productId)
    }

    fun remove(productId: Long) {
        _items.value = _items.value.toMutableMap().apply { remove(productId) }
        snapshots.remove(productId)
    }

    fun clear() {
        _items.value = emptyMap()
        snapshots.clear()
    }

    fun count(): Int = _items.value.values.sum()

    fun total(): Double =
        _items.value.entries.sumOf { (id, q) ->
            val precio = snapshots[id]?.precio ?: 0.0
            precio * q
        }

    data class DetailedItem(
        val product: ProductDtoRemote,
        val quantity: Int,
        val lineTotal: Double
    )

    fun detailed(): List<DetailedItem> =
        _items.value.mapNotNull { (id, q) ->
            val p = snapshots[id] ?: return@mapNotNull null
            val precio = p.precio ?: 0.0
            DetailedItem(p, q, precio * q)
        }
}
