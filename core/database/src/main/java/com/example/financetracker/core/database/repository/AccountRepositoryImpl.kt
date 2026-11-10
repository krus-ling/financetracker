package com.example.financetracker.core.database.repository

import com.example.financetracker.core.database.dao.AccountDao
import com.example.financetracker.core.database.dao.TransactionDao
import com.example.financetracker.core.database.entity.AccountEntity
import com.example.financetracker.core.database.mapper.toDomain
import com.example.financetracker.core.database.mapper.toEntity
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) : AccountRepository {
    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAccountById(id: Long): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun getAccountById(id: String): Account? {
        return id.toLongOrNull()?.let { getAccountById(it) }
    }

    override suspend fun createAccount(name: String, initialBalance: Double, currency: String): Long {
        return accountDao.insertAccount(
            AccountEntity(
                name = name,
                balance = initialBalance,
                currency = currency
            )
        )
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun deleteAccount(id: Long) {
        accountDao.deleteAccountById(id)
    }

    override suspend fun deleteAccount(id: String, deleteTransactions: Boolean) {
        val accountId = id.toLongOrNull() ?: return
        if (deleteTransactions) {
            transactionDao.softDeleteTransactionsByAccountId(accountId)
        }
        accountDao.softDeleteAccount(accountId)
    }

    override fun getDeletedAccounts(): Flow<List<Account>> {
        return accountDao.getDeletedAccounts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun findDeletedAccountByName(name: String): Account? {
        val trimmedName = name.trim()
        return accountDao.getDeletedAccountsList()
            .firstOrNull { it.name.trim().equals(trimmedName, ignoreCase = true) }
            ?.toDomain()
    }

    override suspend fun restoreAccount(id: String) {
        val accountId = id.toLongOrNull() ?: return
        accountDao.restoreAccount(accountId)
        transactionDao.restoreTransactionsByAccountId(accountId)
    }

    override suspend fun purgeAccount(id: String) {
        val accountId = id.toLongOrNull() ?: return
        transactionDao.deleteTransactionsByAccountId(accountId)
        accountDao.deleteAccountById(accountId)
    }
}
