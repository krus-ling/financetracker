package com.example.financetracker.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.core.model.repository.AccountRepository
import com.example.financetracker.core.model.repository.CategoryRepository
import com.example.financetracker.core.model.repository.TransactionRepository
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.MonthlySummary
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionDayGroup
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.feature.home.util.DateTimeUtils
import com.example.financetracker.feature.home.util.groupTransactionByDay
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext

/**
 * Главная ViewModel приложения.
 *
 * Отвечает за:
 * - Управление реактивным состоянием главного экрана [HomeUiState] по архитектуре UDF;
 * - Постраничную подгрузку истории операций с фоновой группировкой по дням;
 * - Реактивный сбор доходов/расходов за текущий месяц;
 * - Операции со счетами, категориями и транзакциями.
 */
class HomeViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // =========================================================================
    // СТЕЙТЫ И ОБРАБОТКА ИСКЛЮЧЕНИЙ
    // =========================================================================

    /** Состояние ошибок одиночных асинхронных операций */
    private val _errorState = MutableStateFlow<String?>(null)

    /** Текущий лимит загрузки истории транзакций */
    private val _transactionLimit = MutableStateFlow(PAGE_SIZE)

    /** Флаг активного процесса дозагрузки страницы истории */
    private val _isLoadingMore = MutableStateFlow(false)

    /** Единый перехватчик исключений корутин */
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        _errorState.update { throwable.localizedMessage ?: "Произошла неизвестная ошибка" }
    }

    /** Внутренняя модель состояния пагинации */
    private data class PagingData(
        val transactions: List<Transaction> = emptyList(),
        val groupedDays: List<TransactionDayGroup> = emptyList(),
        val hasMore: Boolean = true,
        val isLoadingMore: Boolean = false
    )

    // =========================================================================
    // РЕАКТИВНЫЕ ПАЙПЛАЙНЫ ДАННЫХ (UPSTREAM FLOWS)
    // =========================================================================

    /** Поток активных и удалённых счетов */
    private val accountFlow = combine(
        accountRepository.getAllAccounts().catch { emit(emptyList()) },
        accountRepository.getDeletedAccounts().catch { emit(emptyList()) }
    ) { active, deleted -> Pair(active, deleted) }

    /** Реактивная сводка доходов и расходов за текущий календарный месяц */
    private val monthlySummaryFlow: Flow<MonthlySummary> = run {
        val (start, end) = DateTimeUtils.getCurrentMonthRange()
        transactionRepository.getMonthlySummary(start, end)
            .catch { emit(MonthlySummary()) }
    }

    /**
     * Постраничный поток операций с оптимизированной фоновой группировкой по дням на [kotlinx.coroutines.Dispatchers.Default].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val transactionPagingFlow = combine(
        _transactionLimit.flatMapLatest { limit ->
            transactionRepository.getRecentTransaction(limit = limit)
                .catch { emit(emptyList()) }
                .onEach { _isLoadingMore.value = false }
                .map { list ->
                    val hasMore = list.size >= limit
                    val grouped = withContext(Dispatchers.Default) {
                        groupTransactionByDay(list)
                    }
                    Triple(list, grouped, hasMore)
                }
        },
        _isLoadingMore
    ) { (list, grouped, hasMore), isLoadingMore ->
        PagingData(
            transactions = list,
            groupedDays = grouped,
            hasMore = hasMore,
            isLoadingMore = isLoadingMore
        )
    }

    // =========================================================================
    // ЕДИНЫЙ ПУБЛИЧНЫЙ UI-СТАТУС (UDF)
    // =========================================================================

    /** Реактивное состояние экрана */
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
            groupedDays = paging.groupedDays,
            deletedAccounts = deletedAccounts,
            totalBalance = totalBalance,
            monthlyIncome = summary.income,
            monthlyExpense = summary.expense,
            currentMonthName = DateTimeUtils.getCurrentMonthName(),
            errorMessage = error,
            isLoadingMore = paging.isLoadingMore,
            hasMoreTransactions = paging.hasMore
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    // =========================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // =========================================================================

    init {
        viewModelScope.launch(errorHandler) {
            accountRepository.checkAndSeedDefaultAccount()
            categoryRepository.checkAndSeedDefaultCategories()
        }
    }

    // =========================================================================
    // 1. ОПЕРАЦИИ И ИСТОРИЯ (TRANSACTIONS & PAGINATION)
    // =========================================================================

    /**
     * Добавляет новую финансовую операцию (Расход, Доход или Перевод).
     */
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

    /**
     * Запрашивает дозагрузку следующей порции истории транзакций.
     */
    fun loadMoreTransactions() {
        val state = uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMoreTransactions) return

        _isLoadingMore.value = true
        _transactionLimit.update { currentLimit -> currentLimit + PAGE_SIZE }
    }

    /**
     * Возвращает список операций для конкретного счёта с лимитом.
     */
    suspend fun getAccountTransactions(accountId: String, limit: Int = 100): List<Transaction> {
        return transactionRepository.getTransactionsByAccountId(accountId, limit)
    }

    // =========================================================================
    // 2. УПРАВЛЕНИЕ СЧЕТАМИ (ACCOUNTS MANAGEMENT)
    // =========================================================================

    /**
     * Создаёт новый счёт.
     */
    fun createAccount(
        name: String,
        initialBalance: Double,
        currency: String = "RUB",
        isDefault: Boolean = false,
        type: AccountType = AccountType.CARD,
    ) {
        viewModelScope.launch(errorHandler) {
            accountRepository.createAccount(
                name = name,
                initialBalance = initialBalance,
                currency = currency,
                isDefault = isDefault,
                type = type,
            )
        }
    }

    /**
     * Обновляет параметры существующего счёта.
     */
    fun updateAccount(account: Account) {
        viewModelScope.launch(errorHandler) {
            accountRepository.updateAccount(account)
        }
    }

    /**
     * Мягко удаляет счёт в корзину (с опциональным удалением связанных операций).
     */
    fun deleteAccount(accountId: String, deleteTransactions: Boolean = false) {
        viewModelScope.launch(errorHandler) {
            accountRepository.deleteAccount(accountId, deleteTransactions)
        }
    }

    /**
     * Находит удалённый счёт по имени (для проверки на режим дежавю).
     */
    suspend fun findDeletedAccountByName(name: String): Account? {
        return accountRepository.findDeletedAccountByName(name)
    }

    /**
     * Восстанавливает счёт из корзины.
     */
    fun restoreAccount(accountId: String) {
        viewModelScope.launch(errorHandler) {
            accountRepository.restoreAccount(accountId)
        }
    }

    /**
     * Навсегда удаляет счёт из корзины (очистка).
     */
    fun purgeAccount(accountId: String) {
        viewModelScope.launch(errorHandler) {
            accountRepository.purgeAccount(accountId)
        }
    }

    /**
     * Назначает счёт счётом по умолчанию.
     */
    fun setDefaultAccount(accountId: String, isDefault: Boolean = true) {
        viewModelScope.launch(errorHandler) {
            accountRepository.setDefaultAccount(accountId, isDefault)
        }
    }

    /**
     * Возвращает реактивный поток сводки расходов и доходов для конкретного счёта за текущий месяц.
     */
    fun getAccountMonthlySummary(accountId: String): Flow<MonthlySummary> {
        val (start, end) = DateTimeUtils.getCurrentMonthRange()
        return transactionRepository.getMonthlySummaryByAccountId(accountId, start, end)
    }

    // =========================================================================
    // 3. УПРАВЛЕНИЕ КАТЕГОРИЯМИ (CATEGORIES MANAGEMENT)
    // =========================================================================

    /**
     * Создаёт новую категорию.
     */
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

    /**
     * Обновляет бюджетный лимит категории.
     */
    fun updateCategoryBudget(categoryId: String, budgetLimit: Double?) {
        viewModelScope.launch(errorHandler) {
            categoryRepository.updateBudgetLimit(categoryId, budgetLimit)
        }
    }

    /**
     * Удаляет категорию.
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch(errorHandler) {
            categoryRepository.deleteCategory(categoryId)
        }
    }

    // =========================================================================
    // 4. ОБРАБОТКА ОШИБОК (ERROR HANDLING)
    // =========================================================================

    /**
     * Сбрасывает текущую ошибку после её отображения пользователю (например, в Snackbar).
     */
    fun clearError() {
        _errorState.value = null
    }

    // =========================================================================
    // КОНСТАНТЫ
    // =========================================================================

    companion object {
        /** Размер пачки загрузки операций при постраничной пагинации */
        private const val PAGE_SIZE = 30
    }
}
