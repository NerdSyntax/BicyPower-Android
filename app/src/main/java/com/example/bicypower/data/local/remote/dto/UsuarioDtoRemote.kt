package com.example.bicypower.data.remote.dto

data class UsuarioDtoRemote(
    val id: Long?,
    val nombre: String,
    val email: String,
    val telefono: String,
    val rol: String,
    val verificado: Boolean? = null
)
