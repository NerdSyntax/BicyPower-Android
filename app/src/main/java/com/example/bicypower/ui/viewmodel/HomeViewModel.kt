package com.example.bicypower.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.remote.dto.ProductDtoRemote
import com.example.bicypower.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val items: List<ProductDtoRemote> = emptyList(),
    val isLoading: Boolean = true,
    val errorMsg: String? = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ProductRepository()

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMsg = null)

            repo.getAll()
                .onSuccess { list: List<ProductDtoRemote> ->
                    _state.value = _state.value.copy(items = list, isLoading = false)
                }
                .onFailure { e: Throwable ->
                    Log.e("HomeVM", "getAll error", e)
                    _state.value = _state.value.copy(isLoading = false, errorMsg = e.message)
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMsg = null)
    }

    fun addToCartRemote(p: ProductDtoRemote, onOk: (ProductDtoRemote) -> Unit) {
        val id = p.id ?: run {
            _state.value = _state.value.copy(errorMsg = "Producto inválido (sin id)")
            return
        }

        val stockValue = p.stock ?: 0
        if (stockValue <= 0) {
            _state.value = _state.value.copy(errorMsg = "Producto agotado")
            return
        }

        viewModelScope.launch {
            val updated = p.copy(stock = (stockValue - 1).coerceAtLeast(0))

            repo.update(id, updated)
                .onSuccess { serverUpdated: ProductDtoRemote ->
                    val newList = _state.value.items.map { item ->
                        if (item.id == id) serverUpdated else item
                    }
                    _state.value = _state.value.copy(items = newList)
                    onOk(serverUpdated)
                }
                .onFailure { e: Throwable ->
                    _state.value = _state.value.copy(errorMsg = e.message ?: "No se pudo agregar")
                }
        }
    }
}
