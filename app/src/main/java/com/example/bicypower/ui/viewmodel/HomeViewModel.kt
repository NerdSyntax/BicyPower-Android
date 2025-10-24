package com.example.bicypower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.product.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val items: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMsg: String? = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = BicyPowerDatabase.getInstance(app).productDao()

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dao.observeAll().collectLatest { list ->
                _state.value = _state.value.copy(
                    items = list,
                    isLoading = false,
                    errorMsg = null
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMsg = null)
    }

    /**
     * Resta 1 del stock si hay disponible y, si todo sale bien, llama a [onOk]
     * (para que tú agregues al carrito con tu CartStore).
     */
    fun addToCart(p: ProductEntity, onOk: () -> Unit) {
        if (p.stock <= 0) {
            _state.value = _state.value.copy(errorMsg = "Producto agotado")
            return
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    dao.updateStock(p.id, (p.stock - 1).coerceAtLeast(0))
                }
            }.onSuccess {
                onOk()
            }.onFailure { e ->
                _state.value = _state.value.copy(errorMsg = e.message ?: "Error al actualizar stock")
            }
        }
    }
}
