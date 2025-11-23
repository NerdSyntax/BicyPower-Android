package com.example.bicypower.data.repository

import com.example.bicypower.data.local.user.UserDao
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

    // ---------- LOGIN ----------
    suspend fun loginRemote(email: String, password: String): Result<UsuarioDtoRemote> {
        return try {
            val body = LoginRequestDtoRemote(email, password)
            val response = api.login(body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- REGISTER NORMAL (CLIENT) ----------
    suspend fun registerRemote(
        name: String,
        email: String,
        phone: String,
        pass: String
    ): Result<UsuarioDtoRemote> {
        return try {
            val body = RegisterRequestDtoRemote(
                nombre = name,
                email = email,
                telefono = phone,
                password = pass,
                rol = "CLIENT"          // <- registro normal de la app
            )

            val response = api.register(body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error del servidor (${response.code()})"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- REGISTER STAFF (ADMIN) ----------
    suspend fun registerStaffRemote(
        name: String,
        email: String,
        phone: String,
        pass: String
    ): Result<UsuarioDtoRemote> {
        return try {
            val body = RegisterRequestDtoRemote(
                nombre = name,
                email = email,
                telefono = phone,
                password = pass,
                rol = "STAFF"           // <- acá lo mandamos como STAFF
            )

            val response = api.register(body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error del servidor (${response.code()})"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- FORGOT PASSWORD ----------
    suspend fun forgotPasswordRemote(email: String): Result<Unit> {
        return try {
            val body = ForgotPasswordRequestDtoRemote(email)
            val response = api.forgotPassword(body)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo enviar el correo (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- VERIFY CODE ----------
    suspend fun verifyCode(email: String, code: String): Result<Unit> {
        return try {
            val body = VerifyCodeRequestDtoRemote(email = email, codigo = code)
            val response = api.verifyCode(body)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Código inválido (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- RESET PASSWORD ----------
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
                    Exception("No se pudo cambiar la contraseña (${response.code()})")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- CHANGE PASSWORD ----------
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
                    Exception("No se pudo actualizar (${response.code()})")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- LISTAR USUARIOS (ADMIN) ----------
    suspend fun getAllUsersRemote(): Result<List<UsuarioDtoRemote>> {
        return try {
            val response = api.getUsers()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error del servidor (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- ELIMINAR USUARIO (ADMIN) ----------
    suspend fun deleteUserRemote(id: Long): Result<Unit> {
        return try {
            val response = api.deleteUser(id)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
