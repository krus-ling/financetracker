package com.example.financetracker.core.database.repository

import com.example.financetracker.core.database.dao.AccountDao
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

    suspend fun createAccount(account: Account): Long {
        return accountDao.insertAccount(account.toEntity())
    }
}