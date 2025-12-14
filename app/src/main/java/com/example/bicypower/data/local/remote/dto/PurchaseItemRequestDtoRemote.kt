package com.example.bicypower.remote.dto

data class PurchaseItemRequestDtoRemote(
    val productoId: Long,
    val cantidad: Int,
    val precioUnitario: Double
)
