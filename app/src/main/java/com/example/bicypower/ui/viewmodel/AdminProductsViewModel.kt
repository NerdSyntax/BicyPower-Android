package com.example.bicypower.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.product.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminProductsState(
    val items: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showCreate: Boolean = false,
    val pName: String = "", val pPrice: String = "",
    val pImage: String = "", val pDesc: String = "",
    val isSubmitting: Boolean = false, val errorMsg: String? = null,
    val editId: Long? = null, val editPrice: String = "",
    val confirmDeleteId: Long? = null
)

class AdminProductsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = BicyPowerDatabase.getInstance(app).productDao()

    private val _state = MutableStateFlow(AdminProductsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dao.observeAll()
                .catch { e ->
                    Log.e("AdminProductsVM","observeAll error",e)
                    _state.value = _state.value.copy(isLoading = false, errorMsg = e.message)
                    emit(emptyList())
                }
                .collectLatest { list ->
                    _state.value = _state.value.copy(items = list, isLoading = false, errorMsg = null)
                }
        }
    }

    fun openCreate() { _state.value = _state.value.copy(showCreate = true, errorMsg = null) }
    fun closeCreate(){ _state.value = _state.value.copy(showCreate = false, isSubmitting = false, errorMsg = null,
        pName = "", pPrice = "", pImage = "", pDesc = "") }
    fun onName(v:String){ _state.value = _state.value.copy(pName=v) }
    fun onPrice(v:String){ _state.value = _state.value.copy(pPrice=v) }
    fun onImage(v:String){ _state.value = _state.value.copy(pImage=v) }
    fun onDesc(v:String){ _state.value = _state.value.copy(pDesc=v) }

    fun create() {
        val s = _state.value
        val price = s.pPrice.replace(',', '.').toDoubleOrNull()
        if (s.pName.isBlank() || price == null) {
            _state.value = s.copy(errorMsg = "Nombre y precio válidos son obligatorios"); return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMsg = null)
            runCatching {
                withContext(Dispatchers.IO) {
                    dao.insert(ProductEntity(
                        name = s.pName.trim(),
                        description = s.pDesc.trim(),
                        price = price,
                        imageUrl = s.pImage.trim()
                    ))
                }
            }.onSuccess { closeCreate() }
                .onFailure { _state.value = _state.value.copy(isSubmitting = false, errorMsg = it.message) }
        }
    }

    fun openEditPrice(id: Long, current: Double) {
        _state.value = _state.value.copy(editId = id, editPrice = current.toString())
    }
    fun onEditPrice(v:String){ _state.value = _state.value.copy(editPrice=v) }
    fun closeEdit(){ _state.value = _state.value.copy(editId=null, editPrice="") }
    fun applyEditPrice(){
        val id = _state.value.editId ?: return
        val price = _state.value.editPrice.replace(',', '.').toDoubleOrNull() ?: return
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO){ dao.updatePrice(id, price) } }
                .onSuccess { closeEdit() }
                .onFailure { _state.value = _state.value.copy(errorMsg = it.message) }
        }
    }

    fun askDelete(id: Long){ _state.value = _state.value.copy(confirmDeleteId = id) }
    fun cancelDelete(){ _state.value = _state.value.copy(confirmDeleteId = null) }
    fun confirmDelete(){
        val id = _state.value.confirmDeleteId ?: return
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO){ dao.deleteById(id) } }
                .onSuccess { cancelDelete() }
                .onFailure { _state.value = _state.value.copy(errorMsg = it.message) }
        }
    }
}
