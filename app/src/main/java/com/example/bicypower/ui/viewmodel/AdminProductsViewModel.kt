package com.example.bicypower.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.local.storage.copyImageToAppFiles
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

    // Crear
    val showCreate: Boolean = false,
    val pName: String = "",
    val pPrice: String = "",
    val pImage: String = "",
    val pDesc: String = "",
    val pStock: String = "",                 // 👈 NUEVO: stock inicial como texto

    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,

    // Editar precio
    val editId: Long? = null,
    val editPrice: String = "",

    // Editar imagen
    val editImageId: Long? = null,
    val editImageUrl: String = "",

    // Editar stock
    val editStockId: Long? = null,           // 👈 NUEVO
    val editStock: String = "",              // 👈 NUEVO

    // Borrar
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
                    Log.e("AdminProductsVM", "observeAll error", e)
                    _state.value = _state.value.copy(isLoading = false, errorMsg = e.message)
                    emit(emptyList())
                }
                .collectLatest { list ->
                    _state.value = _state.value.copy(items = list, isLoading = false, errorMsg = null)
                }
        }
    }

    /* ------------------ Crear ------------------ */
    fun openCreate() {
        _state.value = _state.value.copy(showCreate = true, errorMsg = null)
    }

    fun closeCreate() {
        _state.value = _state.value.copy(
            showCreate = false,
            isSubmitting = false,
            errorMsg = null,
            pName = "", pPrice = "", pImage = "", pDesc = "", pStock = ""
        )
    }

    fun onName(v: String)  { _state.value = _state.value.copy(pName = v) }
    fun onPrice(v: String) { _state.value = _state.value.copy(pPrice = v) }
    fun onImage(v: String) { _state.value = _state.value.copy(pImage = v) }
    fun onDesc(v: String)  { _state.value = _state.value.copy(pDesc = v) }
    fun onStock(v: String) { _state.value = _state.value.copy(pStock = v) } // 👈 NUEVO

    fun create() {
        val s = _state.value
        val price = s.pPrice.replace(',', '.').toDoubleOrNull()
        val stock = s.pStock.trim().toIntOrNull() ?: 0  // si está vacío, 0

        if (s.pName.isBlank() || price == null) {
            _state.value = s.copy(errorMsg = "Nombre y precio válidos son obligatorios")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMsg = null)
            runCatching {
                withContext(Dispatchers.IO) {
                    val finalImageUrl = try {
                        val uri = Uri.parse(s.pImage)
                        if (uri != null && uri.scheme == "content") {
                            copyImageToAppFiles(getApplication(), uri).toString()
                        } else s.pImage.trim()
                    } catch (_: Throwable) {
                        s.pImage.trim()
                    }

                    dao.insert(
                        ProductEntity(
                            name = s.pName.trim(),
                            description = s.pDesc.trim(),
                            price = price,
                            imageUrl = finalImageUrl,
                            stock = stock.coerceAtLeast(0)
                        )
                    )
                }
            }.onSuccess { closeCreate() }
                .onFailure {
                    _state.value = _state.value.copy(isSubmitting = false, errorMsg = it.message)
                }
        }
    }

    /* --------------- Editar precio --------------- */
    fun openEditPrice(id: Long, current: Double) {
        _state.value = _state.value.copy(editId = id, editPrice = current.toString())
    }
    fun onEditPrice(v: String) { _state.value = _state.value.copy(editPrice = v) }
    fun closeEdit() { _state.value = _state.value.copy(editId = null, editPrice = "") }
    fun applyEditPrice() {
        val id = _state.value.editId ?: return
        val price = _state.value.editPrice.replace(',', '.').toDoubleOrNull() ?: return
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { dao.updatePrice(id, price) }
            }.onSuccess { closeEdit() }
                .onFailure { _state.value = _state.value.copy(errorMsg = it.message) }
        }
    }

    /* --------------- Editar imagen --------------- */
    fun openEditImage(id: Long, currentUrl: String) {
        _state.value = _state.value.copy(editImageId = id, editImageUrl = currentUrl)
    }
    fun onEditImageUrl(v: String) { _state.value = _state.value.copy(editImageUrl = v) }
    fun closeEditImage() { _state.value = _state.value.copy(editImageId = null, editImageUrl = "") }

    fun applyEditImage() {
        val id = _state.value.editImageId ?: return
        val urlRaw = _state.value.editImageUrl

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val finalUrl = try {
                        val uri = Uri.parse(urlRaw)
                        if (uri != null && uri.scheme == "content") {
                            copyImageToAppFiles(getApplication(), uri).toString()
                        } else urlRaw.trim()
                    } catch (_: Throwable) {
                        urlRaw.trim()
                    }
                    dao.updateImage(id, finalUrl)
                }
            }.onSuccess { closeEditImage() }
                .onFailure { _state.value = _state.value.copy(errorMsg = it.message) }
        }
    }

    fun clearImage() {
        val id = _state.value.editImageId ?: return
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { dao.updateImage(id, "") }
            }.onSuccess { closeEditImage() }
                .onFailure { _state.value = _state.value.copy(errorMsg = it.message) }
        }
    }

    /* --------------- Editar stock --------------- */
    fun openEditStock(id: Long, currentStock: Int) {
        _state.value = _state.value.copy(editStockId = id, editStock = currentStock.toString(), errorMsg = null)
    }
    fun onEditStock(v: String) { _state.value = _state.value.copy(editStock = v) }
    fun closeEditStock() { _state.value = _state.value.copy(editStockId = null, editStock = "") }

    fun applyEditStock() {
        val id = _state.value.editStockId ?: return
        val newStock = _state.value.editStock.trim().toIntOrNull()
        if (newStock == null || newStock < 0) {
            _state.value = _state.value.copy(errorMsg = "Stock inválido")
            return
        }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { dao.updateStock(id, newStock) }
            }.onSuccess { closeEditStock() }
                .onFailure { _state.value = _state.value.copy(errorMsg = it.message) }
        }
    }

    /* ------------------ Eliminar ------------------ */
    fun askDelete(id: Long) { _state.value = _state.value.copy(confirmDeleteId = id) }
    fun cancelDelete() { _state.value = _state.value.copy(confirmDeleteId = null) }
    fun confirmDelete() {
        val id = _state.value.confirmDeleteId ?: return
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { dao.deleteById(id) }
            }.onSuccess { cancelDelete() }
                .onFailure { _state.value = _state.value.copy(errorMsg = it.message) }
        }
    }
}
