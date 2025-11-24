package com.example.bicypower.data.local.product

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Query("UPDATE products SET price = :price WHERE id = :id")
    suspend fun updatePrice(id: Long, price: Double)

    @Query("UPDATE products SET imageUrl = :url WHERE id = :id")
    suspend fun updateImage(id: Long, url: String)

    @Query("UPDATE products SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)
}
