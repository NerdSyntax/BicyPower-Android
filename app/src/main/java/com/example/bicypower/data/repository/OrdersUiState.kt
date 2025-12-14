package com.example.bicypower.data.repository

import com.example.bicypower.remote.dto.PurchaseResponseDtoRemote

data class OrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<PurchaseResponseDtoRemote> = emptyList(),
    val error: String? = null
)
