package com.example.financetracker.feature.home.domain.model

import com.example.financetracker.core.model.TransactionType

/**
 * Черновик транзакции, извлеченный из текстовой или голосовой фразы.
 */
data class ParsedTransactionDraft(
    val amount: Double? = null,
    val categoryId: String? = null,
    val accountId: String? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val note: String = "",
    val date: Long? = null,
    val rawText: String = ""
)