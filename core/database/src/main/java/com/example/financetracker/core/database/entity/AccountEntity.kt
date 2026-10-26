package com.example.financetracker.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val balance: Double,
    val currency: String = "RUB",
    val colorHex: String = "#0F5C4A"
)