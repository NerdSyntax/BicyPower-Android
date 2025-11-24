package com.example.bicypower.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BicyPowerRemoteModule {

    // Usuarios (ms-usuarios, puerto 8080)
    private const val BASE_URL_USERS = "http://10.0.2.2:8080/"
    // Productos (ms-productos, puerto 8081)
    private const val BASE_URL_PRODUCTS = "http://10.0.2.2:8081/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofitUsers: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_USERS)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitProducts: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_PRODUCTS)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ⚠️ Mantengo este nombre para no romper nada de usuarios
    val api: UsersApi = retrofitUsers.create(UsersApi::class.java)

    // 👇 nuevo API de productos
    val productsApi: ProductsApi = retrofitProducts.create(ProductsApi::class.java)
}
