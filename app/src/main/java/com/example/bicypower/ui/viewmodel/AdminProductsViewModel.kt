package com.example.bicypower.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.local.database.BicyPowerDatabase
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.local.storage.copyImageToAppFiles
import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.toDtoRemote
import com.example.bicypower.data.remote.dto.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ------------------------------------------------------------
// STATE
// ------------------------------------------------------------
data class AdminProductsState(
    val items: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = true,

    // Crear
    val showCreate: Boolean = false,
    val pName: String = "",
    val pPrice: String = "",
    val pImage: String = "",
    val pDesc: String = "",
    val pStock: String = "",

    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,

    // Editar precio
    val editId: Long? = null,
    val editPrice: String = "",

    // Editar imagen
    val editImageId: Long? = null,
    val editImageUrl: String = "",

    // Editar stock
    val editStockId: Long? = null,
    val editStock: String = "",

    // Eliminar
    val confirmDeleteId: Long? = null
)

// ------------------------------------------------------------
// VIEWMODEL
// ------------------------------------------------------------
class AdminProductsViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = BicyPowerDatabase.getInstance(app).productDao()
    private val api = BicyPowerRemoteModule.productsApi

    private val _state = MutableStateFlow(AdminProductsState())
    val state = _state.asStateFlow()

    init {
        observeLocalDb()
        syncFromRemote()
    }

    // ------------------------------------------------------------
    // 1) OBSERVAR ROOM
    // ------------------------------------------------------------
    private fun observeLocalDb() {
        viewModelScope.launch {
            dao.observeAll()
                .catch { e ->
                    Log.e("AdminProductsVM", "observeAll error", e)
                    _state.value = _state.value.copy(isLoading = false, errorMsg = e.message)
                }
                .collectLatest { list ->
                    _state.value = _state.value.copy(items = list, isLoading = false)
                }
        }
    }

    // ------------------------------------------------------------
    // 2) TRAER PRODUCTOS DEL MICROSERVICIO
    // ------------------------------------------------------------
    private fun syncFromRemote() {
        viewModelScope.launch {
            runCatching {
                val response = api.getProducts()
                if (response.isSuccessful && response.body() != null) {
                    val entities = response.body()!!.map { it.toEntity() }
                    withContext(Dispatchers.IO) {
                        dao.clearAll()
                        dao.insertAll(entities)
                    }
                } else {
                    throw Exception("Error ${response.code()} al cargar productos")
                }
            }.onFailure {
                _state.value = _state.value.copy(errorMsg = it.message)
            }
        }
    }

    // ------------------------------------------------------------
    // CREAR PRODUCTO
    // ------------------------------------------------------------
    fun openCreate() {
        _state.value = _state.value.copy(showCreate = true, errorMsg = null)
    }

    fun closeCreate() {
        _state.value = _state.value.copy(
            showCreate = false,
            isSubmitting = false,
            pName = "", pPrice = "", pImage = "", pDesc = "", pStock = "",
            errorMsg = null
        )
    }

    fun onName(v: String) = applyState { copy(pName = v) }
    fun onPrice(v: String) = applyState { copy(pPrice = v) }
    fun onImage(v: String) = applyState { copy(pImage = v) }
    fun onDesc(v: String) = applyState { copy(pDesc = v) }
    fun onStock(v: String) = applyState { copy(pStock = v) }

    fun create() {
        val s = _state.value
        val price = s.pPrice.toDoubleOrNull()
        val stock = s.pStock.toIntOrNull() ?: 0

        if (s.pName.isBlank() || price == null) {
            _state.value = s.copy(errorMsg = "Nombre y precio válidos son obligatorios")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true, errorMsg = null)

            runCatching {
                val finalImage = withContext(Dispatchers.IO) {
                    val raw = s.pImage.trim()
                    val uri = Uri.parse(raw)
                    if (uri.scheme == "content") copyImageToAppFiles(getApplication(), uri).toString()
                    else raw
                }

                val dto = ProductEntity(
                    name = s.pName.trim(),
                    description = s.pDesc.trim(),
                    price = price,
                    imageUrl = finalImage,
                    stock = stock,
                    active = true
                ).toDtoRemote()

                val response = api.createProduct(dto)

                if (!response.isSuccessful || response.body() == null) {
                    throw Exception("Error al crear producto (${response.code()})")
                }

                val saved = response.body()!!.toEntity()
                dao.insert(saved)
            }.onSuccess { closeCreate() }
                .onFailure {
                    _state.value = _state.value.copy(isSubmitting = false, errorMsg = it.message)
                }
        }
    }

    // ------------------------------------------------------------
    // EDITAR PRECIO
    // ------------------------------------------------------------
    fun openEditPrice(id: Long, current: Double) {
        _state.value = _state.value.copy(editId = id, editPrice = current.toString())
    }

    fun onEditPrice(v: String) = applyState { copy(editPrice = v) }

    fun closeEdit() {
        _state.value = _state.value.copy(editId = null, editPrice = "")
    }

    fun applyEditPrice() {
        val s = _state.value
        val id = s.editId ?: return
        val newPrice = s.editPrice.toDoubleOrNull() ?: return

        viewModelScope.launch {
            runCatching {
                val local = dao.findById(id) ?: throw Exception("Producto no existe local")
                val dto = local.toDtoRemote().copy(precio = newPrice)

                val response = api.updateProduct(id, dto)
                if (!response.isSuccessful) throw Exception("Error actualizando (${response.code()})")

                val updated = response.body()!!.toEntity()
                dao.insert(updated)
            }.onSuccess { closeEdit() }
                .onFailure { applyError(it) }
        }
    }

    // ------------------------------------------------------------
    // EDITAR IMAGEN
    // ------------------------------------------------------------
    fun openEditImage(id: Long, currentUrl: String) {
        _state.value = _state.value.copy(editImageId = id, editImageUrl = currentUrl)
    }

    fun onEditImageUrl(v: String) = applyState { copy(editImageUrl = v) }

    fun closeEditImage() {
        _state.value = _state.value.copy(editImageId = null, editImageUrl = "")
    }

    fun applyEditImage() {
        val s = _state.value
        val id = s.editImageId ?: return

        viewModelScope.launch {
            runCatching {
                val local = dao.findById(id) ?: throw Exception("Producto no existe local")

                val finalUrl = withContext(Dispatchers.IO) {
                    val uri = Uri.parse(s.editImageUrl)
                    if (uri.scheme == "content") copyImageToAppFiles(getApplication(), uri).toString()
                    else s.editImageUrl.trim()
                }

                val dto = local.toDtoRemote().copy(imagenUrl = finalUrl)

                val response = api.updateProduct(id, dto)
                if (!response.isSuccessful) throw Exception("Error al actualizar imagen (${response.code()})")

                val updated = response.body()!!.toEntity()
                dao.insert(updated)
            }.onSuccess { closeEditImage() }
                .onFailure { applyError(it) }
        }
    }

    fun clearImage() {
        val id = _state.value.editImageId ?: return
        onEditImageUrl("")
        applyEditImage()
    }

    // ------------------------------------------------------------
    // EDITAR STOCK
    // ------------------------------------------------------------
    fun openEditStock(id: Long, current: Int) {
        _state.value = _state.value.copy(editStockId = id, editStock = current.toString())
    }

    fun onEditStock(v: String) = applyState { copy(editStock = v) }

    fun closeEditStock() {
        _state.value = _state.value.copy(editStockId = null, editStock = "")
    }

    fun applyEditStock() {
        val s = _state.value
        val id = s.editStockId ?: return
        val stock = s.editStock.toIntOrNull() ?: return

        viewModelScope.launch {
            runCatching {
                val local = dao.findById(id) ?: throw Exception("Producto no existe local")
                val dto = local.toDtoRemote().copy(stock = stock)

                val response = api.updateProduct(id, dto)
                if (!response.isSuccessful) throw Exception("Error al actualizar stock (${response.code()})")

                val updated = response.body()!!.toEntity()
                dao.insert(updated)
            }.onSuccess { closeEditStock() }
                .onFailure { applyError(it) }
        }
    }

    // ------------------------------------------------------------
    // ELIMINAR
    // ------------------------------------------------------------
    fun askDelete(id: Long) {
        _state.value = _state.value.copy(confirmDeleteId = id)
    }

    fun cancelDelete() {
        _state.value = _state.value.copy(confirmDeleteId = null)
    }

    fun confirmDelete() {
        val id = _state.value.confirmDeleteId ?: return

        viewModelScope.launch {
            runCatching {
                val response = api.deleteProduct(id)
                if (!response.isSuccessful) throw Exception("Error al eliminar (${response.code()})")

                dao.deleteById(id)
            }.onSuccess { cancelDelete() }
                .onFailure { applyError(it) }
        }
    }

    // ------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------
    private fun applyState(block: AdminProductsState.() -> AdminProductsState) {
        _state.value = _state.value.block()
    }

    private fun applyError(e: Throwable) {
        _state.value = _state.value.copy(errorMsg = e.message)
    }
}
