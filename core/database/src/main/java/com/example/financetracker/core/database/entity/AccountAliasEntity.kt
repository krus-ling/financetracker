package com.example.financetracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Таблица самообучения счетов/банков: хранит связь сленгового названия или алиаса (стема) со счетом.
 * Пример: совок -> Совкомбанк, заначка -> Копилка
 */
@Entity(
    tableName = "account_aliases",
    primaryKeys = ["aliasStem", "accountId"],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("accountId"),
        Index("aliasStem")
    ]
)
data class AccountAliasEntity(
    val aliasStem: String,
    val accountId: Long,
    val usageCount: Int = 1,
    val lastUsedAt: Long = System.currentTimeMillis()
)