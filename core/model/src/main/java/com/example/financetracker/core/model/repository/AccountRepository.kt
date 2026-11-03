package com.example.financetracker.core.model.repository

import com.example.financetracker.core.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    suspend fun getAccountById(id: Long): Account?
    suspend fun getAccountById(id: String): Account?
    suspend fun createAccount(name: String, initialBalance: Double, currency: String = "RUB"): Long
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: Long)
    suspend fun deleteAccount(id: String)
}