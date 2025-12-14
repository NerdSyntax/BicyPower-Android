package com.example.bicypower.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductDtoRemote(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("descripcion")
    val descripcion: String? = null,

    @SerializedName("precio")
    val precio: Double? = null,

    @SerializedName("stock")
    val stock: Int? = null,

    @SerializedName("activo")
    val activo: Boolean? = null,

    @SerializedName("imagenUrl")
    val imagenUrl: String? = null,

    // si tu backend lo manda, déjalo nullable (puede venir null)
    @SerializedName("bytesImagen")
    val bytesImagen: Any? = null
)
