package com.example.bicypower.data.remote.dto

data class PurchaseItemDtoRemote(
    val productoId: Long,
    val cantidad: Int,
    val precioUnitario: Double
)

data class PurchaseResponseDtoRemote(
    val id: Long,
    val usuarioId: Long,
    val total: Double,
    val fecha: String,
    val items: List<PurchaseItemDtoRemote>
)

data class PurchaseItemRequestDtoRemote(
    val productoId: Long,
    val cantidad: Int,
    val precioUnitario: Double
)

data class PurchaseRequestDtoRemote(
    val usuarioId: Long,
    val items: List<PurchaseItemRequestDtoRemote>
)
