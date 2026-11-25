package com.example.financetracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Таблица самообучения категорий: хранит связь нормализованного слова (стема) с категорией пользователя.
 *
 * Поля [usageCount] и [lastUsedAt] позволяют разрешить конфликты: если слово раньше встречалось
 * в одной категории, но пользователь стал чаще использовать его к другой, побеждает
 * наиболее частотная и свежая связка.
 */
@Entity(
    tableName = "category_keywords",
    primaryKeys = ["keywordStem", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("keywordStem")
    ]
)
data class CategoryKeywordEntity(
    val keywordStem: String,
    val categoryId: Long,
    val usageCount: Int = 1,
    val lastUsedAt: Long = System.currentTimeMillis()
)