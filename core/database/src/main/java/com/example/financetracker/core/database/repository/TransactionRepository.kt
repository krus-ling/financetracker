package com.example.financetracker.core.database.repository

import androidx.room.withTransaction
import com.example.financetracker.core.database.AppDatabase
import com.example.financetracker.core.database.dao.AccountDao
import com.example.financetracker.core.database.dao.TransactionDao
import com.example.financetracker.core.database.mapper.toDomain
import com.example.financetracker.core.database.mapper.toEntity
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val appDatabase: AppDatabase,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    fun getRecentTransaction(limit: Int = 10): Flow<List<Transaction>> {
        return transactionDao.getRecentTransaction(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addTransaction(transaction: Transaction): Long = appDatabase.withTransaction {
        val entity = transaction.toEntity()

        // 1. Сохраняем операцию
        val insertedId = transactionDao.insertTransaction(entity)

        // 2. Обновляем балансы счетов
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.updateBalance(entity.accountId, -transaction.amount)
            }
            TransactionType.INCOME -> {
                accountDao.updateBalance(entity.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                // Списываем со счёта списания
                accountDao.updateBalance(entity.accountId, -transaction.amount)
                // Зачисляем на целевой счёт
                entity.targetAccountId?.let { targetId ->
                    accountDao.updateBalance(targetId, transaction.amount)
                }
            }
        }

        insertedId
    }
}