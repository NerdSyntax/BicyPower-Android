package com.example.bicypower.data.remote

import com.example.bicypower.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface UsersApi {

    // ---------- LOGIN ----------
    @POST("/api/usuarios/login")
    suspend fun login(
        @Body body: LoginRequestDtoRemote
    ): Response<UsuarioDtoRemote>


    // ---------- REGISTER ----------
    @POST("/api/usuarios")
    suspend fun register(
        @Body body: RegisterRequestDtoRemote
    ): Response<UsuarioDtoRemote>


    // ---------- VERIFY CODE (activar cuenta) ----------
    @POST("/api/usuarios/verify-code")
    suspend fun verifyCode(
        @Body body: VerifyCodeRequestDtoRemote
    ): Response<Unit>


    // ---------- CHANGE PASSWORD ----------
    @PUT("/api/usuarios/change-password")
    suspend fun changePassword(
        @Body body: ChangePasswordRequestDtoRemote
    ): Response<Unit>


    // ---------- FORGOT PASSWORD ----------
    @POST("/api/usuarios/forgot-password")
    suspend fun forgotPassword(
        @Body body: ForgotPasswordRequestDtoRemote
    ): Response<Unit>


    // ---------- RESET PASSWORD ----------
    @POST("/api/usuarios/reset-password")
    suspend fun resetPassword(
        @Body body: ResetPasswordRequestDtoRemote
    ): Response<Unit>
}
