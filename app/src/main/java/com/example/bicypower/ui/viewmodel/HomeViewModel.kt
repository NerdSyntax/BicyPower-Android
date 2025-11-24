package com.example.bicypower.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    private val api = BicyPowerRemoteModule.productsApi

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        observeLocalDb()
        syncFromRemote()   // <-- ESTA ES LA PARTE QUE TE FALTABA
    }

    // -------------------------
    // OBSERVA LA BASE DE DATOS LOCAL
    // -------------------------
    private fun observeLocalDb() {
        viewModelScope.launch {
            dao.observeAll()
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMsg = e.message
                    )
                }
                .collectLatest { list ->
                    _state.value = _state.value.copy(
                        items = list,
                        isLoading = false
                    )
                }
        }
    }

    // -------------------------
    // TRAE PRODUCTOS DEL MICROSERVICIO
    // -------------------------
    private fun syncFromRemote() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            runCatching {
                val response = api.getProducts()

                if (!response.isSuccessful || response.body() == null) {
                    throw Exception("Error al cargar productos (${response.code()})")
                }

                val entities = response.body()!!.map { it.toEntity() }

                withContext(Dispatchers.IO) {
                    dao.clearAll()
                    dao.insertAll(entities)
                }

            }.onFailure { e ->
                Log.e("HomeVM", "syncFromRemote error", e)
                _state.value = _state.value.copy(
                    errorMsg = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMsg = null)
    }

    fun addToCart(p: ProductEntity, onOk: () -> Unit) {
        if (p.stock <= 0) {
            _state.value = _state.value.copy(errorMsg = "Producto agotado")
            return
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    dao.updateStock(p.id, p.stock - 1)
                }
            }.onSuccess {
                onOk()
            }.onFailure { e ->
                _state.value = _state.value.copy(errorMsg = e.message)
            }
        }
    }
}
