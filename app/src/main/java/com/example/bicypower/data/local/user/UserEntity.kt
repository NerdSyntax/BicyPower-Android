package com.example.bicypower.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String = "CLIENT" // ADMIN | STAFF | CLIENT
)
