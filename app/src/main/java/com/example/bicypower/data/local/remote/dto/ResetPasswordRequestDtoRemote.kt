package com.example.bicypower.data.remote.dto

data class ResetPasswordRequestDtoRemote(
    val email: String,
    val codigo: String,
    val newPassword: String
)
