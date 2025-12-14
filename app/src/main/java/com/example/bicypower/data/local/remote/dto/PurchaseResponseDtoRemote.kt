package com.example.bicypower.remote.dto

data class PurchaseResponseDtoRemote(
    val id: Long,
    val usuarioId: Long,
    val total: Double,
    val fecha: String,
    val items: List<PurchaseItemDtoRemote>
)
