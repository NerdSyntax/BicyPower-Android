package com.example.bicypower.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrdersState(
    val isLoading: Boolean = false,
    val orders: List<com.example.bicypower.data.remote.dto.PurchaseResponseDtoRemote> = emptyList(),
    val errorMsg: String? = null
)

class OrdersViewModel(
    private val repo: PurchaseRepository = PurchaseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersState())
    val state: StateFlow<OrdersState> = _state.asStateFlow()

    fun loadMyOrders(userId: Long) {
        viewModelScope.launch {
            _state.value = OrdersState(isLoading = true)
            try {
                _state.value = OrdersState(orders = repo.getMyOrders(userId))
            } catch (e: Exception) {
                _state.value = OrdersState(errorMsg = e.message ?: "Error cargando pedidos")
            }
        }
    }

    fun loadAllOrdersAdmin() {
        viewModelScope.launch {
            _state.value = OrdersState(isLoading = true)
            try {
                _state.value = OrdersState(orders = repo.getAllOrdersAdmin())
            } catch (e: Exception) {
                _state.value = OrdersState(errorMsg = e.message ?: "Error cargando pedidos (admin)")
            }
        }
    }
}
