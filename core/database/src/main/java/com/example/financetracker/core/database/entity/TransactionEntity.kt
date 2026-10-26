package com.example.financetracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.financetracker.core.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            // Удалили счет - удалились все его операции
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            // Удалили категорию - в операции остаётся null
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("accountId"),
        Index("categoryId")
    ]
)
data class TransactionEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val categoryId: Long?,
    val amount: Double,
    val type: TransactionType,
    val dateTime: Long = System.currentTimeMillis(),
    val note: String? = null
)