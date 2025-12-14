package com.example.bicypower.data.repository

import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.ProductDtoRemote

class ProductRepository {

    private val api = BicyPowerRemoteModule.productsApi

    suspend fun getAll(): Result<List<ProductDtoRemote>> = runCatching {
        val resp = api.getProducts()
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception("Error ${resp.code()} cargando productos")
        }
        resp.body()!!
    }

    suspend fun update(id: Long, body: ProductDtoRemote): Result<ProductDtoRemote> = runCatching {
        val resp = api.updateProduct(id, body)
        if (!resp.isSuccessful || resp.body() == null) {
            throw Exception("Error ${resp.code()} actualizando producto")
        }
        resp.body()!!
    }
}
