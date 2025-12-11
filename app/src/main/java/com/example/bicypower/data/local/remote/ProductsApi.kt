package com.example.bicypower.data.remote

import com.example.bicypower.data.remote.dto.ProductDtoRemote
import okhttp3.MultipartBody
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

    // NUEVO: subir imagen como archivo (BLOB)
    @Multipart
    @POST("/api/productos/{id}/imagen")
    suspend fun uploadImage(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): Response<Unit>
}
