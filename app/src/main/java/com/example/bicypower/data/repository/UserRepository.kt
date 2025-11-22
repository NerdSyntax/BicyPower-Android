package com.example.bicypower.data.repository

import com.example.bicypower.data.local.user.UserDao
import com.example.bicypower.data.local.user.UserEntity
import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.ChangePasswordRequestDtoRemote
import com.example.bicypower.data.remote.dto.ForgotPasswordRequestDtoRemote
import com.example.bicypower.data.remote.dto.LoginRequestDtoRemote
import com.example.bicypower.data.remote.dto.RegisterRequestDtoRemote
import com.example.bicypower.data.remote.dto.ResetPasswordRequestDtoRemote
import com.example.bicypower.data.remote.dto.UsuarioDtoRemote
import com.example.bicypower.data.remote.dto.VerifyCodeRequestDtoRemote

class UserRepository(
    private val userDao: UserDao
) {

    private val api = BicyPowerRemoteModule.api

    // ------------------------------------------------------------
    // LOGIN REMOTO
    // ------------------------------------------------------------
    suspend fun loginRemote(email: String, password: String): Result<UsuarioDtoRemote> {
        return try {
            val body = LoginRequestDtoRemote(email = email, password = password)
            val response = api.login(body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Error al iniciar sesión (${response.code()})"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // REGISTRO REMOTO
    // ------------------------------------------------------------
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
                    Exception(
                        response.errorBody()?.string()
                            ?: "Error del servidor (${response.code()})"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // VERIFICAR CÓDIGO (Registro)
    // ------------------------------------------------------------
    suspend fun verifyCode(email: String, code: String): Result<Unit> {
        return try {
            val body = VerifyCodeRequestDtoRemote(
                email = email,
                codigo = code
            )

            val response = api.verifyCode(body)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Código de verificación inválido (${response.code()})"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // CAMBIAR CONTRASEÑA (usuario autenticado)
    // ------------------------------------------------------------
    suspend fun changePasswordRemote(
        email: String,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val body = ChangePasswordRequestDtoRemote(
                email = email,
                currentPassword = currentPassword,
                newPassword = newPassword
            )

            val response = api.changePassword(body)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Error al cambiar la contraseña (${response.code()})"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // ENVIAR CÓDIGO DE RECUPERACIÓN (forgot-password)
    // ------------------------------------------------------------
    suspend fun forgotPasswordRemote(email: String): Result<Unit> {
        return try {
            val body = ForgotPasswordRequestDtoRemote(email = email)
            val response = api.forgotPassword(body)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Error al enviar correo (${response.code()})"
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // RESET PASSWORD CON CÓDIGO
    // ------------------------------------------------------------
    suspend fun resetPasswordRemote(
        email: String,
        code: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val body = ResetPasswordRequestDtoRemote(
                email = email,
                codigo = code,
                newPassword = newPassword
            )

            val response = api.resetPassword(body)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Error al restablecer contraseña (${response.code()})"
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // BASE LOCAL (Room) – si quieres seguir usándola
    // ------------------------------------------------------------
    suspend fun getLocalByEmail(email: String): UserEntity? {
        return userDao.getByEmail(email)
    }
}
