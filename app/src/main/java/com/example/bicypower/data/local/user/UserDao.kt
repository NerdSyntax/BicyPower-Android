package com.example.bicypower.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // Inserta un usuario nuevo en SQLite local
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    // Busca un usuario por email (login local)
    @Query("SELECT * FROM users WHERE lower(email)=lower(:email) LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    // Devuelve cuántos usuarios existen
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    // Obtiene todos los usuarios (solo local)
    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAll(): List<UserEntity>

    // Flujo en tiempo real de usuarios
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun observeAll(): Flow<List<UserEntity>>

    // Eliminar usuario por ID
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Cambiar contraseña por email
    @Query("UPDATE users SET password = :newPassword WHERE lower(email)=lower(:email)")
    suspend fun updatePasswordByEmail(email: String, newPassword: String): Int
}
