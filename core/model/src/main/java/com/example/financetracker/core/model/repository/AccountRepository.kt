package com.example.financetracker.core.model.repository

import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun getDeletedAccounts(): Flow<List<Account>> // список счетов в корзине
    suspend fun checkAndSeedDefaultAccount()
    suspend fun getAccountById(id: Long): Account?
    suspend fun getAccountById(id: String): Account?
    suspend fun findDeletedAccountByName(name: String): Account?
    suspend fun createAccount(
        name: String,
        initialBalance: Double,
        currency: String = "RUB",
        isDefault: Boolean = false,
        type: AccountType = AccountType.CARD,
    ): Long
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: Long)
    suspend fun deleteAccount(id: String, deleteTransactions: Boolean = false)
    suspend fun restoreAccount(id: String) // вернуть из корзины
    suspend fun purgeAccount(id: String) // удалить навсегда
    // Установка счета по умолчанию
    suspend fun setDefaultAccount(id: String, isDefault: Boolean = true)
}