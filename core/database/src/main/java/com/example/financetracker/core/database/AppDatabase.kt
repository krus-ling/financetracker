package com.example.financetracker.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL
import com.example.financetracker.core.database.converters.DatabaseConverters
import com.example.financetracker.core.database.dao.AccountDao
import com.example.financetracker.core.database.dao.CategoryDao
import com.example.financetracker.core.database.dao.TransactionDao
import com.example.financetracker.core.database.entity.AccountEntity
import com.example.financetracker.core.database.entity.CategoryEntity
import com.example.financetracker.core.database.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DATABASE_NAME = "finance_tracker.db"

        /**
         * Миграция с версии 7 на версию 8:
         * Добавляет колонку `type` в таблицу счетов `accounts` со значением по умолчанию 'CARD'.
         * Все существующие счета и операции сохраняются.
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE accounts ADD COLUMN type TEXT NOT NULL DEFAULT 'CARD';")
            }
        }

        fun buildDatabase(context: Context): AppDatabase {

            val callback = object : Callback() {

                override fun onOpen(connection: SQLiteConnection) {
                    super.onOpen(connection)
                    // Включаем поддержку внешних ключей в SQLite, чтобы работал каскадный onDelete (CASCADE)
                    connection.execSQL("PRAGMA foreign_keys = ON;")
                }
            }

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_7_8)
                .fallbackToDestructiveMigration(true)
                .addCallback(callback)
                .build()
        }
    }
}
