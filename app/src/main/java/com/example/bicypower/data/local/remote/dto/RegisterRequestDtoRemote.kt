package com.example.bicypower.data.remote.dto

data class RegisterRequestDtoRemote(
    val nombre: String,
    val email: String,
    val telefono: String,
    val password: String,
    val rol: String
)
