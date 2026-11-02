package com.example.financetracker.core.database.repository

import com.example.financetracker.core.database.dao.AccountDao
import com.example.financetracker.core.database.entity.AccountEntity
import com.example.financetracker.core.database.mapper.toDomain
import com.example.financetracker.core.database.mapper.toEntity
import com.example.financetracker.core.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository(
    private val accountDao: AccountDao
) {
    fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAccountById(id: Long): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    suspend fun getAccountById(id: String): Account? {
        return id.toLongOrNull()?.let { getAccountById(it) }
    }

    suspend fun createAccount(name: String, initialBalance: Double, currency: String = "RUB"): Long {
        return accountDao.insertAccount(
            AccountEntity(
                name = name,
                balance = initialBalance,
                currency = currency
            )
        )
    }

    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    suspend fun deleteAccount(id: Long) {
        accountDao.deleteAccountById(id)
    }

    suspend fun deleteAccount(id: String) {
        id.toLongOrNull()?.let { deleteAccount(it) }
    }
}
