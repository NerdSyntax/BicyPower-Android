package com.example.bicypower.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bicypower.data.local.product.ProductDao
import com.example.bicypower.data.local.product.ProductEntity
import com.example.bicypower.data.local.user.UserDao
import com.example.bicypower.data.local.user.UserEntity

@Database(
    entities = [UserEntity::class, ProductEntity::class],
    version = 12,                 // súbela para recrear la DB
    exportSchema = true
)
abstract class BicyPowerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile private var INSTANCE: BicyPowerDatabase? = null
        private const val DB_NAME = "bicy_power.db"

        fun getInstance(context: Context): BicyPowerDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    BicyPowerDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seed(db)
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Si solo quieres sembrar una vez, comenta la siguiente línea:
                            seed(db)
                        }
                        private fun seed(db: SupportSQLiteDatabase) {
                            // ---- Usuarios ----
                            db.query("SELECT COUNT(*) FROM users").use { c ->
                                if (c.moveToFirst() && c.getInt(0) == 0) {
                                    db.execSQL(
                                        """
                                        INSERT INTO users (name,email,phone,password,role) VALUES
                                        ('Admin','admin@bicy.cl','11111111','Admin12345!','ADMIN'),
                                        ('Staff','staff@bicy.cl','22222222','Staff12345!','STAFF'),
                                        ('Cliente','cliente@bicy.cl','33333333','Cliente12345!','CLIENT')
                                        """.trimIndent()
                                    )
                                }
                            }
                            // ---- Productos ----
                            db.query("SELECT COUNT(*) FROM products").use { c ->
                                if (c.moveToFirst() && c.getInt(0) == 0) {
                                    db.execSQL(
                                        """
                                        INSERT INTO products (name,description,price,imageUrl,active,stock) VALUES
                                        ('Bicicleta Ruta Pro','Cuadro carbono, 11v',1499000,'https://picsum.photos/seed/road/800/600',1,5),
                                        ('MTB Trail 29"','Suspensión delantera, 1x12',899000,'https://picsum.photos/seed/mtb/800/600',1,3),
                                        ('Casco Pro','Certificación CE',79990,'https://picsum.photos/seed/helmet/800/600',1,0)
                                        """.trimIndent()
                                    )
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration() // recrea la DB al subir la versión
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
