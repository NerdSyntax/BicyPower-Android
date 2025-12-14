package com.example.bicypower.data.repository

import com.example.bicypower.data.remote.BicyPowerRemoteModule
import com.example.bicypower.data.remote.dto.*

class UserRepository {

    private val api = BicyPowerRemoteModule.api

    suspend fun loginRemote(email: String, password: String): Result<UsuarioDtoRemote> =
        try {
            val response = api.login(LoginRequestDtoRemote(email, password))
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("Credenciales inválidas"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun registerRemote(name: String, email: String, phone: String, pass: String): Result<UsuarioDtoRemote> =
        try {
            val body = RegisterRequestDtoRemote(nombre = name, email = email, telefono = phone, password = pass, rol = "CLIENT")
            val response = api.register(body)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("Error del servidor (${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun registerStaffRemote(name: String, email: String, phone: String, pass: String): Result<UsuarioDtoRemote> =
        try {
            val body = RegisterRequestDtoRemote(nombre = name, email = email, telefono = phone, password = pass, rol = "STAFF")
            val response = api.register(body)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("Error del servidor (${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun forgotPasswordRemote(email: String): Result<Unit> =
        try {
            val response = api.forgotPassword(ForgotPasswordRequestDtoRemote(email))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("No se pudo enviar el correo (${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun verifyCode(email: String, code: String): Result<Unit> =
        try {
            val response = api.verifyCode(VerifyCodeRequestDtoRemote(email = email, codigo = code))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Código inválido (${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun resetPasswordRemote(email: String, code: String, newPassword: String): Result<Unit> =
        try {
            val response = api.resetPassword(ResetPasswordRequestDtoRemote(email = email, codigo = code, newPassword = newPassword))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("No se pudo cambiar la contraseña (${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun changePasswordRemote(email: String, currentPassword: String, newPassword: String): Result<Unit> =
        try {
            val response = api.changePassword(ChangePasswordRequestDtoRemote(email, currentPassword, newPassword))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("No se pudo actualizar (${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getAllUsersRemote(): Result<List<UsuarioDtoRemote>> =
        try {
            val response = api.getUsers()
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("Error del servidor (${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun deleteUserRemote(id: Long): Result<Unit> =
        try {
            val response = api.deleteUser(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al eliminar (${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
