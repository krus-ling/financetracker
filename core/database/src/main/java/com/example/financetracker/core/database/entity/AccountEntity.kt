package com.example.financetracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.financetracker.core.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val balance: Double,
    val currency: String = "RUB",
    val type: AccountType = AccountType.CARD,
    val colorHex: String = "#0F5C4A",
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)