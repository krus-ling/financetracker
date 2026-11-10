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
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            // Удалили категорию - в операции остаётся null
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetAccountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("accountId"),
        Index("categoryId"),
        Index("targetAccountId")
    ]
)
data class TransactionEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long? = null,
    val targetAccountId: Long? = null,
    val categoryId: Long?,
    val amount: Double,
    val type: TransactionType,
    val dateTime: Long = System.currentTimeMillis(),
    val note: String? = null
)