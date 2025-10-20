package com.example.bicypower.data.repository

import com.example.bicypower.data.local.user.UserDao
import com.example.bicypower.data.local.user.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val u = userDao.getByEmail(email.trim())
        return if (u != null && u.password == password) Result.success(u)
        else Result.failure(IllegalArgumentException("Credenciales inválidas"))
    }

    suspend fun register(name: String, email: String, phone: String, password: String): Result<Long> {
        val exists = userDao.getByEmail(email.trim()) != null
        if (exists) return Result.failure(IllegalStateException("El correo ya está registrado"))
        val id = userDao.insert(
            UserEntity(name = name.trim(), email = email.trim(), phone = phone.trim(), password = password)
        )
        return Result.success(id)
    }
}
