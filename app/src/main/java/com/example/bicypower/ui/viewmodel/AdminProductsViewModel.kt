package com.example.bicypower.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.ProductDtoRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class AdminProductsState(
    val items: List<ProductDtoRemote> = emptyList(),
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

class AdminProductsViewModel(app: Application) : AndroidViewModel(app) {

    private val api = BicyPowerRemoteModule.productsApi

    private val _state = MutableStateFlow(AdminProductsState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMsg = null)

            runCatching {
                val resp = api.getProducts()
                if (!resp.isSuccessful || resp.body() == null) {
                    throw Exception("Error ${resp.code()} al cargar productos")
                }
                resp.body()!!
            }.onSuccess { list ->
                _state.value = _state.value.copy(items = list, isLoading = false)
            }.onFailure { e ->
                Log.e("AdminProductsVM", "refresh error", e)
                _state.value = _state.value.copy(isLoading = false, errorMsg = e.message)
            }
        }
    }

    // ----------------------------
    // Crear
    // ----------------------------
    fun openCreate() = applyState { copy(showCreate = true, errorMsg = null) }

    fun closeCreate() = applyState {
        copy(
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

    private fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val resolver = getApplication<Application>().contentResolver
        val input: InputStream = resolver.openInputStream(uri)!!
        val buffer = ByteArrayOutputStream()
        val temp = ByteArray(4096)
        var read: Int
        while (input.read(temp).also { read = it } != -1) {
            buffer.write(temp, 0, read)
        }
        input.close()

        val bytes = buffer.toByteArray()
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), bytes)
        return MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
    }

    fun create() {
        val s = _state.value
        val price = s.pPrice.toDoubleOrNull()
        val stock = s.pStock.toIntOrNull() ?: 0

        if (s.pName.isBlank() || price == null) {
            applyError("Nombre y precio válidos son obligatorios")
            return
        }

        viewModelScope.launch {
            applyState { copy(isSubmitting = true, errorMsg = null) }

            runCatching {
                val rawImage = s.pImage.trim()
                val uri = Uri.parse(rawImage)
                val isLocalContent = uri.scheme == "content" || uri.scheme == "file"

                val dto = ProductDtoRemote(
                    id = 0L, // backend debería asignar
                    nombre = s.pName.trim(),
                    descripcion = s.pDesc.trim(),
                    precio = price,
                    stock = stock,
                    activo = true,
                    imagenUrl = if (isLocalContent) "" else rawImage,
                    bytesImagen = null
                )

                val createResp = api.createProduct(dto)
                if (!createResp.isSuccessful || createResp.body() == null) {
                    throw Exception("Error al crear producto (${createResp.code()})")
                }

                val created = createResp.body()!!

                val createdId = created.id
                if (isLocalContent && createdId != null && createdId != 0L) {
                    withContext(Dispatchers.IO) {
                        val part = uriToMultipart(uri)
                        val uploadResp = api.uploadImage(createdId, part)
                        if (!uploadResp.isSuccessful) {
                            throw Exception("Producto creado, pero error al subir imagen (${uploadResp.code()})")
                        }
                    }
                }

                created
            }.onSuccess {
                closeCreate()
                refresh()
            }.onFailure {
                applyState { copy(isSubmitting = false, errorMsg = it.message) }
            }
        }
    }

    // ----------------------------
    // Editar precio
    // ----------------------------
    fun openEditPrice(id: Long, current: Double) =
        applyState { copy(editId = id, editPrice = current.toString()) }

    fun onEditPrice(v: String) = applyState { copy(editPrice = v) }

    fun closeEdit() = applyState { copy(editId = null, editPrice = "") }

    fun applyEditPrice() {
        val s = _state.value
        val id = s.editId ?: return
        val newPrice = s.editPrice.toDoubleOrNull() ?: return

        viewModelScope.launch {
            runCatching {
                val current = s.items.firstOrNull { it.id == id }
                    ?: throw Exception("Producto no existe en memoria")

                val dto = current.copy(precio = newPrice)

                val resp = api.updateProduct(id, dto)
                if (!resp.isSuccessful || resp.body() == null) {
                    throw Exception("Error actualizando (${resp.code()})")
                }
            }.onSuccess {
                closeEdit()
                refresh()
            }.onFailure { applyError(it.message) }
        }
    }

    // ----------------------------
    // Editar imagen (URL o archivo)
    // ----------------------------
    fun openEditImage(id: Long, currentUrl: String) =
        applyState { copy(editImageId = id, editImageUrl = currentUrl) }

    fun onEditImageUrl(v: String) = applyState { copy(editImageUrl = v) }

    fun closeEditImage() = applyState { copy(editImageId = null, editImageUrl = "") }

    fun applyEditImage() {
        val s = _state.value
        val id = s.editImageId ?: return

        viewModelScope.launch {
            runCatching {
                val current = s.items.firstOrNull { it.id == id }
                    ?: throw Exception("Producto no existe en memoria")

                val uri = Uri.parse(s.editImageUrl.trim())
                val isLocalContent = uri.scheme == "content" || uri.scheme == "file"

                if (!isLocalContent) {
                    // URL normal
                    val dto = current.copy(imagenUrl = s.editImageUrl.trim())
                    val resp = api.updateProduct(id, dto)
                    if (!resp.isSuccessful || resp.body() == null) {
                        throw Exception("Error al actualizar imagen (${resp.code()})")
                    }
                } else {
                    // content/file -> subir archivo
                    withContext(Dispatchers.IO) {
                        val part = uriToMultipart(uri)
                        val uploadResp = api.uploadImage(id, part)
                        if (!uploadResp.isSuccessful) {
                            throw Exception("Error subiendo imagen (${uploadResp.code()})")
                        }
                    }
                }
            }.onSuccess {
                closeEditImage()
                refresh()
            }.onFailure { applyError(it.message) }
        }
    }

    // ----------------------------
    // Editar stock
    // ----------------------------
    fun openEditStock(id: Long, current: Int) =
        applyState { copy(editStockId = id, editStock = current.toString()) }

    fun onEditStock(v: String) = applyState { copy(editStock = v) }

    fun closeEditStock() = applyState { copy(editStockId = null, editStock = "") }

    fun applyEditStock() {
        val s = _state.value
        val id = s.editStockId ?: return
        val stock = s.editStock.toIntOrNull() ?: return

        viewModelScope.launch {
            runCatching {
                val current = s.items.firstOrNull { it.id == id }
                    ?: throw Exception("Producto no existe en memoria")

                val dto = current.copy(stock = stock)

                val resp = api.updateProduct(id, dto)
                if (!resp.isSuccessful || resp.body() == null) {
                    throw Exception("Error al actualizar stock (${resp.code()})")
                }
            }.onSuccess {
                closeEditStock()
                refresh()
            }.onFailure { applyError(it.message) }
        }
    }

    // ----------------------------
    // Eliminar
    // ----------------------------
    fun askDelete(id: Long) = applyState { copy(confirmDeleteId = id) }
    fun cancelDelete() = applyState { copy(confirmDeleteId = null) }

    fun confirmDelete() {
        val id = _state.value.confirmDeleteId ?: return

        viewModelScope.launch {
            runCatching {
                val resp = api.deleteProduct(id)
                if (!resp.isSuccessful) {
                    throw Exception("Error al eliminar (${resp.code()})")
                }
            }.onSuccess {
                cancelDelete()
                refresh()
            }.onFailure { applyError(it.message) }
        }
    }

    // ----------------------------
    // Helpers
    // ----------------------------
    private fun applyState(block: AdminProductsState.() -> AdminProductsState) {
        _state.value = _state.value.block()
    }

    private fun applyError(msg: String?) {
        _state.value = _state.value.copy(errorMsg = msg)
    }
}
