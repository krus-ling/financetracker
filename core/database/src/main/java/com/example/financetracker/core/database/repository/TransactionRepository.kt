package com.example.financetracker.core.database.repository

import com.example.financetracker.core.database.dao.AccountDao
import com.example.financetracker.core.database.dao.TransactionDao
import com.example.financetracker.core.database.mapper.toDomain
import com.example.financetracker.core.database.mapper.toEntity
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    fun getRecentTransaction(limit: Int = 10): Flow<List<Transaction>> {
        return transactionDao.getRecentTransaction(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addTransaction(transaction: Transaction): Long {
        val entity = transaction.toEntity()

        // Корректируем баланс счёта в зависимости от типа операции
        val delta = when(transaction.type) {
            TransactionType.EXPENSE -> -transaction.amount
            TransactionType.INCOME -> transaction.amount
            TransactionType.TRANSFER -> -transaction.amount
        }
        accountDao.updateBalance(entity.accountId, delta)

        return transactionDao.insertTransaction(entity)
    }
}