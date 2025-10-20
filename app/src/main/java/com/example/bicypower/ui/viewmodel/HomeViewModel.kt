package com.example.bicypower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.product.ProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
                _state.value = _state.value.copy(items = list, isLoading = false, errorMsg = null)
            }
        }
    }
}
