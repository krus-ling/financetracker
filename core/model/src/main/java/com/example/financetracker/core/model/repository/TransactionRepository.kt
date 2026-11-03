package com.example.financetracker.core.model.repository

import com.example.financetracker.core.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getRecentTransaction(limit: Int = 10): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Long
}