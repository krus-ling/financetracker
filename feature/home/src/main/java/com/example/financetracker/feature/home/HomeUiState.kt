package com.example.financetracker.feature.home

import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction

data class HomeUiState(
    val isLoading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val recentTransaction: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val errorMessage: String? = null
)