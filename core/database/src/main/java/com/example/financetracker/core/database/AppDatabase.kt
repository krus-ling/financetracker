package com.example.financetracker.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.example.financetracker.core.database.converters.DatabaseConverters
import com.example.financetracker.core.database.dao.AccountDao
import com.example.financetracker.core.database.dao.CategoryDao
import com.example.financetracker.core.database.dao.TransactionDao
import com.example.financetracker.core.database.entity.AccountEntity
import com.example.financetracker.core.database.entity.CategoryEntity
import com.example.financetracker.core.database.entity.TransactionEntity
import com.example.financetracker.core.model.CategoryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DATABASE_NAME = "finance_tracker.db"

        // Базовые категории расходов и доходов
        val defaultCategories = listOf(
            CategoryEntity(name = "Продукты", type = CategoryType.EXPENSE, iconRes = "shopping_cart"),
            CategoryEntity(name = "Кафе", type = CategoryType.EXPENSE, iconRes = "restaurant"),
            CategoryEntity(name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "directions_bus"),
            CategoryEntity(name = "Зарплата", type = CategoryType.INCOME, iconRes = "work"),
            CategoryEntity(name = "Переводы", type = CategoryType.EXPENSE, iconRes = "swap_horiz"),
            CategoryEntity(name = "Дом", type = CategoryType.EXPENSE, iconRes = "home"),
            CategoryEntity(name = "Развлечения", type = CategoryType.EXPENSE, iconRes = "theater_comedy"),
            CategoryEntity(name = "Здоровье", type = CategoryType.EXPENSE, iconRes = "favorite"),
            CategoryEntity(name = "Другое", type = CategoryType.EXPENSE, iconRes = "more_horiz")
        )

        fun buildDatabase(context: Context): AppDatabase {
            val callback = object : Callback() {
                override fun onOpen(connection: SQLiteConnection) {
                    super.onOpen(connection)
                    // Включаем поддержку внешних ключей в SQLite, чтобы работал каскадный onDelete (CASCADE)
                    connection.execSQL("PRAGMA foreign_keys = ON;")
                }

                override fun onCreate(connection: SQLiteConnection) {
                    super.onCreate(connection)

                    // Прямой SQL без гонки потоков, корутин и опасного lateinit
                    connection.execSQL(
                        "INSERT INTO accounts (name, balance, currency, colorHex) VALUES ('Основная карта', 0.0, 'RUB', '#0F5C4A');"
                    )
                }
            }

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration(true)
                .addCallback(callback)
                .build()
        }
    }
}
