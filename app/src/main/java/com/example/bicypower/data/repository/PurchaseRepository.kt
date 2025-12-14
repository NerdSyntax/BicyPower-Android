package com.example.bicypower.data.repository

import com.example.bicypower.data.remote.RetrofitInstance
import com.example.bicypower.data.remote.dto.PurchaseRequestDtoRemote
import com.example.bicypower.data.remote.dto.PurchaseResponseDtoRemote

class PurchaseRepository {

    private val api = RetrofitInstance.purchasesApi

    suspend fun getMyOrders(userId: Long): List<PurchaseResponseDtoRemote> =
        api.getByUser(userId)

    suspend fun getAllOrdersAdmin(): List<PurchaseResponseDtoRemote> =
        api.getAllAdmin()

    suspend fun create(req: PurchaseRequestDtoRemote): PurchaseResponseDtoRemote =
        api.create(req)
}
