package com.example.bicypower.remote.dto

data class PurchaseRequestDtoRemote(
    val usuarioId: Long,
    val items: List<PurchaseItemRequestDtoRemote>
)
