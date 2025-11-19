package com.example.bicypower.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE lower(email)=lower(:email) LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 👉 NUEVO: cambiar contraseña por email
    @Query("UPDATE users SET password = :newPassword WHERE lower(email)=lower(:email)")
    suspend fun updatePasswordByEmail(email: String, newPassword: String): Int
}
