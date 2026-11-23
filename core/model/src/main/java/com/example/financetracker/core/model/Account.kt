package com.example.financetracker.core.model

data class Account(
    val id: String,
    val name: String,
    val balance: Double,
    val currency: String = "RUB",
    val type: AccountType = AccountType.CARD,
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
