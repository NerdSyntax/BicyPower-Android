package com.example.bicypower.data.remote

import com.example.bicypower.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsersApi {

    @POST("/api/usuarios/login")
    suspend fun login(
        @Body body: LoginRequestDtoRemote
    ): Response<UsuarioDtoRemote>

    @POST("/api/usuarios")
    suspend fun register(
        @Body body: RegisterRequestDtoRemote
    ): Response<UsuarioDtoRemote>

    @POST("/api/usuarios/verify-code")
    suspend fun verifyCode(
        @Body body: VerifyCodeRequestDtoRemote
    ): Response<Unit>

    @PUT("/api/usuarios/change-password")
    suspend fun changePassword(
        @Body body: ChangePasswordRequestDtoRemote
    ): Response<Unit>

    @POST("/api/usuarios/forgot-password")
    suspend fun forgotPassword(
        @Body body: ForgotPasswordRequestDtoRemote
    ): Response<Unit>

    @POST("/api/usuarios/reset-password")
    suspend fun resetPassword(
        @Body body: ResetPasswordRequestDtoRemote
    ): Response<Unit>

    @GET("/api/usuarios")
    suspend fun getUsers(): Response<List<UsuarioDtoRemote>>

    @DELETE("/api/usuarios/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>
}
