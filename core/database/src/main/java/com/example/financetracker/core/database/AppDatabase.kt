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
import com.example.financetracker.core.database.dao.KeywordDao
import com.example.financetracker.core.database.dao.TransactionDao
import com.example.financetracker.core.database.entity.AccountAliasEntity
import com.example.financetracker.core.database.entity.AccountEntity
import com.example.financetracker.core.database.entity.CategoryEntity
import com.example.financetracker.core.database.entity.CategoryKeywordEntity
import com.example.financetracker.core.database.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        CategoryKeywordEntity::class,
        AccountAliasEntity::class,
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun keywordDao(): KeywordDao

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

        /**
         * Миграция с версии 8 на версию 9:
         * Создаёт таблицы самообучения `category_keywords` и `account_aliases`
         * с каскадным удалением и индексами для быстрого поиска.
         */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                 CREATE TABLE IF NOT EXISTS `category_keywords` (
                     `keywordStem` TEXT NOT NULL,
                     `categoryId` INTEGER NOT NULL,
                     `usageCount` INTEGER NOT NULL,
                     `lastUsedAt` INTEGER NOT NULL,
                     PRIMARY KEY(`keywordStem`, `categoryId`),
                     FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                 );
                 """.trimIndent()
                )
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_category_keywords_categoryId` ON `category_keywords` (`categoryId`);")
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_category_keywords_keywordStem` ON `category_keywords` (`keywordStem`);")

                connection.execSQL(
                    """
                 CREATE TABLE IF NOT EXISTS `account_aliases` (
                     `aliasStem` TEXT NOT NULL,
                     `accountId` INTEGER NOT NULL,
                     `usageCount` INTEGER NOT NULL,
                     `lastUsedAt` INTEGER NOT NULL,
                     PRIMARY KEY(`aliasStem`, `accountId`),
                     FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                 );
                 """.trimIndent()
                )
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_account_aliases_accountId` ON `account_aliases` (`accountId`);")
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_account_aliases_aliasStem` ON `account_aliases` (`aliasStem`);")
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
                .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                .fallbackToDestructiveMigration(true)
                .addCallback(callback)
                .build()
        }
    }
}
