
package com.example.bicypower.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bicypower.data.local.user.UserDao
import com.example.bicypower.data.local.user.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 3,                 // 👈 subimos versión para forzar recreación
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "bicy_power.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed de 3 usuarios con roles
                            db.execSQL("""
                                INSERT INTO users (name, email, phone, password, role) VALUES
                                ('Admin',  'admin@bicy.cl',   '11111111', 'Admin123!',  'ADMIN'),
                                ('Staff',  'staff@bicy.cl',   '22222222', 'Staff123!',  'STAFF'),
                                ('Cliente','cliente@bicy.cl', '33333333', 'Cliente123!','CLIENT')
                            """.trimIndent())
                        }
                    })
                    .fallbackToDestructiveMigration() // demo/educativo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

