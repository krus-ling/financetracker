package com.example.financetracker.core.model.repository

import com.example.financetracker.core.model.MonthlySummary
import com.example.financetracker.core.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getRecentTransaction(limit: Int = 10): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Long
    suspend fun getTransactionsByAccountId(accountId: String, limit: Int = 10): List<Transaction>

    // Подсчет доходов/расходов за период (все счета)
    fun getMonthlySummary(startTime: Long, endTime: Long): Flow<MonthlySummary>

    // Подсчет доходов/расходов за период для конкретного счета
    fun getMonthlySummaryByAccountId(accountId: String, startTime: Long, endTime: Long): Flow<MonthlySummary>
}