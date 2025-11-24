package com.example.bicypower.data.remote

import com.example.bicypower.data.remote.dto.ProductDtoRemote
import retrofit2.Response
import retrofit2.http.*

interface ProductsApi {

    @GET("/api/productos")
    suspend fun getProducts(): Response<List<ProductDtoRemote>>

    @GET("/api/productos/{id}")
    suspend fun getProduct(@Path("id") id: Long): Response<ProductDtoRemote>

    @POST("/api/productos")
    suspend fun createProduct(
        @Body dto: ProductDtoRemote
    ): Response<ProductDtoRemote>

    @PUT("/api/productos/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body dto: ProductDtoRemote
    ): Response<ProductDtoRemote>

    @DELETE("/api/productos/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Long
    ): Response<Unit>
}
