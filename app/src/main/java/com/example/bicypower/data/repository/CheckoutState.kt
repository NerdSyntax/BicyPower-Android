package com.example.bicypower.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Dirección reutilizable en todo el flujo
data class Address(
    val id: Long,
    val nombre: String,
    val linea1: String,
    val linea2: String,
    val ciudad: String,
    val region: String,
    val zip: String,
    val telefono: String
)

// Estado global de checkout
object CheckoutState {

    // Lista de direcciones guardadas
    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses

    // Flags para validar en el carrito
    private val _hasAddress = MutableStateFlow(false)
    val hasAddress: StateFlow<Boolean> = _hasAddress

    private val _hasPayment = MutableStateFlow(false)
    val hasPayment: StateFlow<Boolean> = _hasPayment

    fun addAddress(address: Address) {
        _addresses.value = _addresses.value + address
        _hasAddress.value = _addresses.value.isNotEmpty()
    }

    fun removeAddress(id: Long) {
        _addresses.value = _addresses.value.filterNot { it.id == id }
        _hasAddress.value = _addresses.value.isNotEmpty()
    }

    // Desde PaymentMethodsScreen actualizamos si hay tarjeta o no
    fun updatePaymentCount(count: Int) {
        _hasPayment.value = count > 0
    }
}

// Pedido
data class Order(
    val id: Long,
    val total: Double,
    val itemsCount: Int,
    val createdAt: Long,
    val addressSummary: String,
    val shippingDays: Int
)

// Lista de pedidos realizados
object OrderStore {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    fun add(order: Order) {
        // último pedido primero
        _orders.value = listOf(order) + _orders.value
    }
}
