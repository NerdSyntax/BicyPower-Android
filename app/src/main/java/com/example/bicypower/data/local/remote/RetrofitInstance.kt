package com.example.bicypower.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Emulador: 10.0.2.2 apunta al localhost del PC
    private const val BASE_URL_USERS = "http://10.0.2.2:8080/"
    private const val BASE_URL_COMPRA = "http://10.0.2.2:8083/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private fun buildRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitUsers: Retrofit = buildRetrofit(BASE_URL_USERS)
    private val retrofitCompra: Retrofit = buildRetrofit(BASE_URL_COMPRA)

    val usersApi: UsersApi = retrofitUsers.create(UsersApi::class.java)

    // NUEVO: API de compras
    val purchasesApi: PurchasesApi = retrofitCompra.create(PurchasesApi::class.java)
}
