package com.example.bicypower.data.remote.dto

import com.example.bicypower.data.local.product.ProductEntity

data class ProductDtoRemote(
    val id: Long?,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String,
    val activo: Boolean,
    val stock: Int
)

// ---- Mappers ----

fun ProductDtoRemote.toEntity(): ProductEntity =
    ProductEntity(
        id = id ?: 0L,
        name = nombre,
        description = descripcion,
        price = precio,
        imageUrl = imagenUrl,
        active = activo,
        stock = stock
    )

fun ProductEntity.toDtoRemote(): ProductDtoRemote =
    ProductDtoRemote(
        id = if (id == 0L) null else id,
        nombre = name,
        descripcion = description,
        precio = price,
        imagenUrl = imageUrl,
        activo = active,
        stock = stock
    )
