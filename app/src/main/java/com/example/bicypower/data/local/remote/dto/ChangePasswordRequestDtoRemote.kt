package com.example.bicypower.data.remote.dto

data class ChangePasswordRequestDtoRemote(
    val email: String,
    val currentPassword: String,
    val newPassword: String
)
