package com.example.financetracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.core.model.repository.AccountRepository
import com.example.financetracker.core.model.repository.CategoryRepository
import com.example.financetracker.core.model.repository.TransactionRepository
import com.example.financetracker.core.model.Account
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
        accountRepository.getAllAccounts().catch { emit(emptyList()) },
        transactionRepository.getRecentTransaction(limit = 10).catch { emit(emptyList()) },
        categoryRepository.getAllCategories().catch { emit(emptyList()) },
        accountRepository.getDeletedAccounts().catch { emit(emptyList()) },
        _errorState.asStateFlow()
    ) { accounts, transactions, categories, deletedAccounts, error ->
        val totalBalance = accounts.sumOf { it.balance }
        HomeUiState(
            isLoading = false,
            accounts = accounts,
            categories = categories,
            recentTransaction = transactions,
            deletedAccounts = deletedAccounts, // <-- передаём в стейт
            totalBalance = totalBalance,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        viewModelScope.launch(errorHandler) {
            categoryRepository.checkAndSeedDefaultCategories()
        }
    }

    fun clearError() {
        _errorState.value = null
    }

    fun addTransaction(
        amount: Double,
        categoryId: String?,
        type: TransactionType,
        note: String,
        accountId: String? = null,
        targetAccountId: String? = null,
        dateTime: Long = System.currentTimeMillis()
    ) {
        val currentAccounts = uiState.value.accounts
        val selectedAccountId = accountId ?: currentAccounts.firstOrNull()?.id

        if (selectedAccountId == null) {
            _errorState.update { "Не найден счёт для операции. Сначала создайте счёт." }
            return
        }

        if (type == TransactionType.TRANSFER && targetAccountId == null) {
            _errorState.update { "Не выбран счёт зачисления для перевода." }
            return
        }

        viewModelScope.launch(errorHandler) {
            val transaction = Transaction(
                id = "0",
                amount = amount,
                categoryId = if (type == TransactionType.TRANSFER) "" else (categoryId ?: ""),
                accountId = selectedAccountId,
                targetAccountId = if (type == TransactionType.TRANSFER) targetAccountId else null,
                type = type,
                dateTime = dateTime,
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

    fun updateAccount(account: Account) {
        viewModelScope.launch(errorHandler) {
            accountRepository.updateAccount(account)
        }
    }

    fun createCategory(
        name: String,
        type: CategoryType,
        iconRes: String = "more_horiz",
        budgetLimit: Double? = null
    ) {
        viewModelScope.launch(errorHandler) {
            categoryRepository.createCategory(
                name = name.trim(),
                type = type,
                iconRes = iconRes,
                budgetLimit = budgetLimit
            )
        }
    }

    fun updateCategoryBudget(categoryId: String, budgetLimit: Double?) {
        viewModelScope.launch(errorHandler) {
            categoryRepository.updateBudgetLimit(categoryId, budgetLimit)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch(errorHandler) {
            categoryRepository.deleteCategory(categoryId)
        }
    }

    fun deleteAccount(accountId: String, deleteTransactions: Boolean = false) {
        viewModelScope.launch(errorHandler) {
            accountRepository.deleteAccount(accountId, deleteTransactions)
        }
    }

    suspend fun findDeletedAccountByName(name: String): Account? {
        return accountRepository.findDeletedAccountByName(name)
    }

    fun restoreAccount(accountId: String) {
        viewModelScope.launch(errorHandler) {
            accountRepository.restoreAccount(accountId)
        }
    }

    fun purgeAccount(accountId: String) {
        viewModelScope.launch(errorHandler) {
            accountRepository.purgeAccount(accountId)
        }
    }

    suspend fun getAccountTransactions(accountId: String, limit: Int = 100): List<Transaction> {
        return transactionRepository.getTransactionsByAccountId(accountId, limit)
    }
}
