package com.example.financetracker.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionDayGroup

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val recentTransaction: List<Transaction> = emptyList(),
    val groupedDays: List<TransactionDayGroup> = emptyList(),
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val currentMonthName: String = "",
    val deletedAccounts: List<Account> = emptyList(),
    val errorMessage: String? = null,
    // Дозагрузка истории
    val isLoadingMore: Boolean = false,
    // Есть ли еще что подгружать
    val hasMoreTransactions: Boolean = true,
    val learnedCategoryKeywords: Map<String, String> = emptyMap(),
    val learnedAccountAliases: Map<String, String> = emptyMap(),
)