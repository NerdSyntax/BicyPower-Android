package com.example.bicypower.data.remote

import com.example.bicypower.data.remote.dto.LoginRequestDtoRemote
import com.example.bicypower.data.remote.dto.RegisterRequestDtoRemote
import com.example.bicypower.data.remote.dto.UsuarioDtoRemote
import com.example.bicypower.data.remote.dto.VerifyCodeRequestDtoRemote
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersApi {

    // Registro de usuario
    @POST("api/usuarios") // tu controller acepta "", "/", "/register"
    suspend fun register(
        @Body request: RegisterRequestDtoRemote
    ): Response<UsuarioDtoRemote>

    // Login
    @POST("api/usuarios/login")
    suspend fun login(
        @Body request: LoginRequestDtoRemote
    ): Response<UsuarioDtoRemote>

    // Verificación de código
    @POST("api/usuarios/verify-code")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequestDtoRemote
    ): Response<Void>
}
