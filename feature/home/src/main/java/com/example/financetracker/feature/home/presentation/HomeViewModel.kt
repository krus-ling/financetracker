package com.example.financetracker.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.core.model.repository.AccountRepository
import com.example.financetracker.core.model.repository.CategoryRepository
import com.example.financetracker.core.model.repository.TransactionRepository
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.MonthlySummary
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.feature.home.util.groupTransactionByDay
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

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

    private data class PagingData(
        val transactions: List<Transaction> = emptyList(),
        val hasMore: Boolean = true,
        val isLoadingMore: Boolean = false
    )

    // Стейты управления лимитом
    private val _transactionLimit = MutableStateFlow(PAGE_SIZE)
    private val _isLoadingMore = MutableStateFlow(false)

    // Временные границы текущего календарного месяца (с 1-го числа 00:00:00 до 1-го числа следующего)
    private val currentMonthRange: Pair<Long, Long>
        get() {
            val zoneId = ZoneId.systemDefault()
            val now = LocalDate.now(zoneId)
            val startOfMonth = now
                .withDayOfMonth(1)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()
            val startOfNextMonth = now
                .plusMonths(1)
                .withDayOfMonth(1)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()
            return Pair(startOfMonth, startOfNextMonth)
        }

    // Название текущего месяца на русском в винительном падеже («за ноябрь», «за май» и т.д.)
    private val currentMonthName: String
        get() {
            val months = listOf(
                "январь", "февраль", "март", "апрель", "май", "июнь",
                "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"
            )
            val index = LocalDate.now().monthValue - 1
            return months.getOrElse(index) { "месяц" }
        }

    // Поток активных и удалённых счетов
    private val accountFlow = combine(
        accountRepository.getAllAccounts().catch { emit(emptyList()) },
        accountRepository.getDeletedAccounts().catch { emit(emptyList()) }
    ) { active, deleted -> Pair(active, deleted) }

    // Реактивный поток доходов и расходов за текущий месяц
    private val monthlySummaryFlow: Flow<MonthlySummary> =
        transactionRepository.getMonthlySummary(currentMonthRange.first, currentMonthRange.second)
            .catch { emit(MonthlySummary()) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val transactionPagingFlow = combine(
        _transactionLimit.flatMapLatest { limit ->
            transactionRepository.getRecentTransaction(limit = limit)
                .catch { emit(emptyList()) }
                .onEach { _isLoadingMore.value = false }
                .map { list ->
                    val hasMore = list.size >= limit
                    Pair(list, hasMore)
                }
        },
        _isLoadingMore
    ) { (list, hasMore), isLoadingMore ->
        PagingData(
            transactions = list,
            hasMore = hasMore,
            isLoadingMore = isLoadingMore
        )
    }

    // Реактивное состояние экрана
    val uiState: StateFlow<HomeUiState> = combine(
        accountFlow,
        transactionPagingFlow,
        categoryRepository.getAllCategories().catch { emit(emptyList()) },
        monthlySummaryFlow,
        _errorState.asStateFlow()
    ) { (accounts, deletedAccounts), paging, categories, summary, error ->
        val totalBalance = accounts.sumOf { it.balance }
        HomeUiState(
            isLoading = false,
            accounts = accounts,
            categories = categories,
            recentTransaction = paging.transactions,
            groupedDays = groupTransactionByDay(paging.transactions),
            deletedAccounts = deletedAccounts,
            totalBalance = totalBalance,
            monthlyIncome = summary.income,
            monthlyExpense = summary.expense,
            currentMonthName = currentMonthName,
            errorMessage = error,
            isLoadingMore = paging.isLoadingMore,
            hasMoreTransactions = paging.hasMore
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        viewModelScope.launch(errorHandler) {
            accountRepository.checkAndSeedDefaultAccount()
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
        currency: String = "RUB",
        isDefault: Boolean = false,
    ) {
        viewModelScope.launch(errorHandler) {
            accountRepository.createAccount(
                name = name,
                initialBalance = initialBalance,
                currency = currency,
                isDefault = isDefault,
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

    fun loadMoreTransactions() {
        val state = uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMoreTransactions) return

        _isLoadingMore.value = true
        _transactionLimit.update { currentLimit -> currentLimit + PAGE_SIZE }
    }

    fun setDefaultAccount(accountId: String, isDefault: Boolean = true) {
        viewModelScope.launch(errorHandler) {
            accountRepository.setDefaultAccount(accountId, isDefault)
        }
    }

    fun getAccountMonthlySummary(accountId: String): Flow<MonthlySummary> {
        val (start, end) = currentMonthRange
        return transactionRepository.getMonthlySummaryByAccountId(accountId, start, end)
    }

    suspend fun getAccountTransactions(accountId: String, limit: Int = 100): List<Transaction> {
        return transactionRepository.getTransactionsByAccountId(accountId, limit)
    }

    companion object {
        private const val PAGE_SIZE = 30
    }
}
