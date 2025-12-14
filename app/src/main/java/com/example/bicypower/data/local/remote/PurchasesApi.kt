package com.example.bicypower.data.remote

import com.example.bicypower.data.remote.dto.PurchaseRequestDtoRemote
import com.example.bicypower.data.remote.dto.PurchaseResponseDtoRemote
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PurchasesApi {

    @GET("api/compras/usuario/{userId}")
    suspend fun getByUser(@Path("userId") userId: Long): List<PurchaseResponseDtoRemote>

    @GET("api/compras")
    suspend fun getAllAdmin(@Header("X-ROLE") role: String = "ADMIN"): List<PurchaseResponseDtoRemote>

    @POST("api/compras")
    suspend fun create(@Body body: PurchaseRequestDtoRemote): PurchaseResponseDtoRemote
}
