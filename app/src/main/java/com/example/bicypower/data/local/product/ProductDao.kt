package com.example.bicypower.data.local.product

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(p: ProductEntity): Long

    @Update
    suspend fun update(p: ProductEntity)

    @Query("UPDATE products SET price = :price WHERE id = :id")
    suspend fun updatePrice(id: Long, price: Double)

    // 👇 NUEVO: actualizar solo la imagen
    @Query("UPDATE products SET imageUrl = :url WHERE id = :id")
    suspend fun updateImage(id: Long, url: String)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id DESC")
    suspend fun getAll(): List<ProductEntity>
}
