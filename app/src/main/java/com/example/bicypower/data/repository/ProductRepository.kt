package com.example.bicypower.data.repository

import com.example.bicypower.data.local.product.ProductDao
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.ProductDtoRemote
import com.example.bicypower.data.remote.dto.toEntity
import com.example.bicypower.data.remote.dto.toDtoRemote
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {

    private val api = BicyPowerRemoteModule.productsApi

    // Flow para que lo escuche el ViewModel
    fun observeAll(): Flow<List<ProductEntity>> = productDao.observeAll()

    // ---- Cargar todo desde el microservicio a Room ----
    suspend fun syncFromRemote(): Result<Unit> {
        return try {
            val response = api.getProducts()
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.map { it.toEntity() }
                productDao.clearAll()
                productDao.insertAll(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cargar productos (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---- Crear producto ----
    suspend fun createProduct(
        name: String,
        price: Double,
        imageUrl: String,
        description: String,
        stock: Int
    ): Result<Unit> {
        return try {
            val dto = ProductDtoRemote(
                id = null,
                nombre = name,
                descripcion = description,
                precio = price,
                imagenUrl = imageUrl,
                activo = true,
                stock = stock
            )

            val response = api.createProduct(dto)
            if (response.isSuccessful && response.body() != null) {
                val entity = response.body()!!.toEntity()
                productDao.insert(entity)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al crear producto (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---- Helpers para actualizar campos individuales ----

    suspend fun updatePrice(id: Long, newPrice: Double): Result<Unit> =
        updateFullProduct(id) { it.copy(precio = newPrice) }

    suspend fun updateImage(id: Long, newUrl: String): Result<Unit> =
        updateFullProduct(id) { it.copy(imagenUrl = newUrl) }

    suspend fun updateStock(id: Long, newStock: Int): Result<Unit> =
        updateFullProduct(id) { it.copy(stock = newStock) }

    private suspend fun updateFullProduct(
        id: Long,
        transform: (ProductDtoRemote) -> ProductDtoRemote
    ): Result<Unit> {
        return try {
            val local = productDao.findById(id)
                ?: return Result.failure(Exception("Producto local no encontrado"))

            val baseDto = local.toDtoRemote()
            val dto = transform(baseDto)

            val response = api.updateProduct(id, dto)
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toEntity()
                productDao.insert(updated)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al actualizar producto (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---- Eliminar producto ----
    suspend fun deleteProduct(id: Long): Result<Unit> {
        return try {
            val response = api.deleteProduct(id)
            if (response.isSuccessful) {
                productDao.deleteById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar producto (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
