package com.example.financetracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.core.database.repository.AccountRepository
import com.example.financetracker.core.database.repository.CategoryRepository
import com.example.financetracker.core.database.repository.TransactionRepository
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Ошибки одиночных асинхронных операций
    private val _errorState = MutableStateFlow<String?>(null)

    // Единый перехватчик исключений для coroutines без runCatching
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        _errorState.update { throwable.localizedMessage ?: "Произошла неизвестная ошибка" }
    }

    // Реактивное состояние экрана
    val uiState: StateFlow<HomeUiState> = combine(
        accountRepository.getAllAccounts(),
        transactionRepository.getRecentTransaction(limit = 10),
        categoryRepository.getAllCategories(),
        _errorState.asStateFlow()
    ) { accounts, transactions, categories, error ->
        val totalBalance = accounts.sumOf { it.balance }
        HomeUiState(
            isLoading = false,
            accounts = accounts,
            categories = categories,
            recentTransaction = transactions,
            totalBalance = totalBalance,
            errorMessage = error
        )
    }.catch { throwable ->
        emit(
            HomeUiState(
                isLoading = false,
                errorMessage = throwable.localizedMessage ?: "Неизвестная ошибка"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun clearError() {
        _errorState.value = null
    }

    fun addTransaction(
        amount: Double,
        categoryId: String?,
        type: TransactionType,
        note: String
    ) {
        val currentAccounts = uiState.value.accounts
        val primaryAccountId = currentAccounts.firstOrNull()?.id ?: "1"

        viewModelScope.launch(errorHandler) {
            val transaction = Transaction(
                id = "0",
                amount = amount,
                categoryId = categoryId ?: "",
                accountId = primaryAccountId,
                type = type,
                dateTime = System.currentTimeMillis(),
                note = note.trim()
            )
            transactionRepository.addTransaction(transaction)
        }
    }

    fun createAccount(
        name: String,
        initialBalance: Double,
        currency: String = "RUB"
    ) {
        viewModelScope.launch(errorHandler) {
            accountRepository.createAccount(
                name = name,
                initialBalance = initialBalance,
                currency = currency
            )
        }
    }
}