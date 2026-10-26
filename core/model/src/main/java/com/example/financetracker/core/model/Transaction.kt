package com.example.financetracker.core.model

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val accountId: String,
    val targetAccountId: String? = null,
    val dateTime: Long, // timestamp в миллисекундах или Instant
    val note: String? = null
)