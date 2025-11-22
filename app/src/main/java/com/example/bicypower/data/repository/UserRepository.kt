package com.example.bicypower.data.repository

import com.example.bicypower.data.local.user.UserDao
import com.example.bicypower.data.local.user.UserEntity
import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.LoginRequestDtoRemote
import com.example.bicypower.data.remote.dto.RegisterRequestDtoRemote
import com.example.bicypower.data.remote.dto.UsuarioDtoRemote
import com.example.bicypower.data.remote.dto.VerifyCodeRequestDtoRemote

class UserRepository(
    private val userDao: UserDao
) {

    private val api = BicyPowerRemoteModule.api

    // ---------- LOGIN REMOTO ----------
    suspend fun loginRemote(email: String, password: String): Result<UsuarioDtoRemote> {
        return try {
            val body = LoginRequestDtoRemote(email = email, password = password)
            val response = api.login(body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // leemos mensaje de error de backend si queremos
                Result.failure(
                    Exception(response.errorBody()?.string()
                        ?: "Error al iniciar sesión (${response.code()})")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- REGISTRO REMOTO ----------
    suspend fun registerRemote(
        nombre: String,
        email: String,
        telefono: String,
        password: String
    ): Result<UsuarioDtoRemote> {
        return try {
            val body = RegisterRequestDtoRemote(
                nombre = nombre,
                email = email,
                telefono = telefono,
                password = password
            )

            val response = api.register(body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string()
                        ?: "Error del servidor (${response.code()})")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- VERIFICAR CÓDIGO ----------
    suspend fun verifyCode(email: String, code: String): Result<Unit> {
        return try {
            val body = VerifyCodeRequestDtoRemote(
                email = email,
                codigo = code   // 👈 IMPORTANTE: "codigo"
            )

            val response = api.verifyCode(body)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string()
                        ?: "Código de verificación inválido (${response.code()})")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- LOCAL (para forgot password que ya usas) ----------
    suspend fun getLocalByEmail(email: String): UserEntity? {
        return userDao.getByEmail(email)
    }
}
