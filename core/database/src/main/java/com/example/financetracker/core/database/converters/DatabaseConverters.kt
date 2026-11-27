package com.example.financetracker.core.database.converters

import androidx.room.TypeConverter
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import kotlin.reflect.typeOf

class DatabaseConverters {

    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(value: String): AccountType {
        return try {
            AccountType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            AccountType.CARD
        }
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            TransactionType.EXPENSE
        }
    }

    @TypeConverter
    fun fromCategoryType(type: CategoryType): String = type.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType {
        return try {
            CategoryType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            CategoryType.EXPENSE
        }
    }
}