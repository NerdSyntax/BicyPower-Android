package com.example.bicypower.data.remote.dto

import com.example.bicypower.data.remote.BicyPowerRemoteModule.api

data class UsuarioDtoRemote(
    val id: Long?,
    val nombre: String,
    val email: String,
    val telefono: String,
    val rol: String,
    val verificado: Boolean? = null
)
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

        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Error del servidor (${response.code()})"))
        }

    } catch (e: Exception) {
        Result.failure(e)
    }
}