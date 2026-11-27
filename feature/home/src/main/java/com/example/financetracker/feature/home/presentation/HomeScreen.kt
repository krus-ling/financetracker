package com.example.financetracker.feature.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.account.AccountBalanceCard
import com.example.financetracker.core.uikit.components.account.AddAccountCardItem
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.util.rememberHalfWidth
import com.example.financetracker.feature.home.presentation.components.HomeAccountCard
import com.example.financetracker.feature.home.presentation.components.HomeHeader
import com.example.financetracker.feature.home.presentation.components.sheet.AddAccount
import com.example.financetracker.feature.home.presentation.components.transactionDayItems
import org.koin.androidx.compose.koinViewModel

/** Горизонтальный отступ контента от краёв экрана */
private val HomeScreenHorizontalPadding = 20.dp

/** Расстояние между соседними карточками в карусели счетов */
private val AccountCardSpacing = 12.dp

/** Дополнительный нижний отступ контента под системный бар и плавающий BottomBar */
private val ContentBottomClearance = 88.dp

/** Нижний отступ всплывающего Snackbar над BottomBar */
private val SnackbarBottomClearance = 80.dp

// =========================================================================
// 1. ОСНОВНЫЕ КОМПОНЕНТЫ ЭКРАНА (ENTRY POINTS)
// =========================================================================

/**
 * Корневой экран «Главная» (Stateful).
 *
 * Подключает [HomeViewModel], собирает [HomeUiState] с учётом жизненного цикла
 * и связывает действия пользователя (создание счёта, клик по счёту, пагинация истории,
 * проверка и восстановление аккаунтов из корзины) с бизнес-логикой.
 *
 * @param onAccountClick Вызывается при нажатии на карточку счёта для перехода к экрану деталей.
 * @param modifier Модификатор контейнера.
 * @param contentPadding Внешние отступы (например, от системных баров или Scaffold).
 * @param onViewAllTransactions Переход на полный экран истории операций.
 * @param onTransactionsClick Вызывается при нажатии на строку операции в ленте.
 * @param viewModel Экземпляр [HomeViewModel], по умолчанию предоставляется через Koin.
 */
@Composable
fun HomeScreen(
    onAccountClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onViewAllTransactions: () -> Unit = {},
    onTransactionsClick: (Transaction) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        modifier = modifier,
        contentPadding = contentPadding,
        onAddAccount = { name, initialBalance, isDefault, type ->
            viewModel.createAccount(name, initialBalance, isDefault = isDefault, type = type)
        },
        onAccountClick = onAccountClick,
        onViewAllTransactions = onViewAllTransactions,
        onTransactionsClick = onTransactionsClick,
        onCheckDeletedAccount = { viewModel.findDeletedAccountByName(it) },
        onRestoreDeletedAccount = { viewModel.restoreAccount(it) },
        onPurgeDeletedAccount = { viewModel.purgeAccount(it) },
        onGetAccountTransactions = { id -> viewModel.getAccountTransactions(id, 100) },
        onClearError = { viewModel.clearError() },
        onLoadMore = { viewModel.loadMoreTransactions() },
    )
}

/**
 * Презентационный компонент экрана «Главная» (Stateless).
 *
 * Отображает:
 * 1. Шапку с приветствием профиля ([HomeHeader]);
 * 2. Карточку общего баланса с доходами и расходами ([AccountBalanceCard]);
 * 3. Горизонтальную карусель счетов пользователя с кнопкой добавления счёта ([HomeAccountsSection]);
 * 4. Сгруппированную по дням ленту последних операций с поддержкой бесконечной пагинации;
 * 5. Всплывающую шторку создания счёта ([AddAccount]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAddAccount: (name: String, initialBalance: Double, isDefault: Boolean, type: AccountType) -> Unit = { _, _, _, _ -> },
    onAccountClick: (String) -> Unit = {},
    onViewAllTransactions: () -> Unit = {},
    onTransactionsClick: (Transaction) -> Unit = {},
    onCheckDeletedAccount: (suspend (String) -> Account?)? = null,
    onRestoreDeletedAccount: ((String) -> Unit)? = null,
    onPurgeDeletedAccount: ((String) -> Unit)? = null,
    onGetAccountTransactions: (suspend (String) -> List<Transaction>)? = null,
    onClearError: () -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    /** Флаг отображения шторки создания нового счёта */
    var showAddAccountSheet by rememberSaveable { mutableStateOf(false) }

    /** Состояние прокрутки основного вертикального списка экрана */
    val listState = rememberLazyListState()

    /** Флаг необходимости подгрузки следующей страницы (срабатывает за 2 элемента до конца списка) */
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 && lastVisibleItemIndex >= totalItems - 2 &&
            uiState.hasMoreTransactions && !uiState.isLoadingMore
        }
    }

    /** Хост для показа всплывающих уведомлений об ошибках */
    val snackbarHostState = remember { SnackbarHostState() }

    /** Быстрый O(1) доступ к счетам и категориям по их уникальному id */
    val accountsMap = remember(uiState.accounts) {
        uiState.accounts.associateBy { it.id }
    }
    val categoriesMap = remember(uiState.categories) {
        uiState.categories.associateBy { it.id }
    }

    /** Вычисленная адаптивная ширина карточки счёта (2 карточки ровно занимают видимую ширину шапки) */
    val accountCardWidth = rememberHalfWidth(
        horizontalPadding = HomeScreenHorizontalPadding,
        spacing = AccountCardSpacing
    )

    // Триггер фоновой дозагрузки истории при достижении конца списка
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    // Показ локальных ошибок через Snackbar без скрытия уже загруженного контента
    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (error != null && uiState.accounts.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            onClearError()
        }
    }

    when {
        uiState.isLoading -> {
            HomeLoadingView(modifier = modifier)
        }

        // Полноэкранную заглушку показываем только при сбое первичной загрузки, когда нет данных
        uiState.errorMessage != null && uiState.accounts.isEmpty() -> {
            HomeErrorView(
                message = uiState.errorMessage,
                onDismiss = onClearError,
                modifier = modifier
            )
        }

        else -> {
            Box(modifier = modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + ContentBottomClearance
                    )
                ) {
                    // Шапка профиля (заглушка)
                    item {
                        HomeHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = HomeScreenHorizontalPadding, vertical = 8.dp)
                        )
                    }

                    // Общий баланс
                    item {
                        AccountBalanceCard(
                            title = "Общий баланс",
                            balance = uiState.totalBalance,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = HomeScreenHorizontalPadding, vertical = 8.dp),
                            trailingContent = {
                                IconButton(
                                    onClick = { /* TODO скрытие баланса */},
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Visibility,
                                        contentDescription = "Скрыть баланс",
                                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                }
                            },
                            incomeTitle = "Доходы за ${uiState.currentMonthName.ifEmpty { "месяц" }}",
                            incomeAmount = uiState.monthlyIncome,
                            expenseTitle = "Расходы за ${uiState.currentMonthName.ifEmpty { "месяц" }}",

                            expenseAmount = uiState.monthlyExpense
                        )
                    }

                    // Карусель счетов
                    item {
                        HomeAccountsSection(
                            accounts = uiState.accounts,
                            cardWidth = accountCardWidth,
                            cardSpacing = AccountCardSpacing,
                            horizontalPadding = HomeScreenHorizontalPadding,
                            onAccountClick = onAccountClick,
                            onAddAccountClick = { showAddAccountSheet = true }
                        )
                    }

                    // Заголовок ленты операций
                    item {
                        HomeTransactionsHeader(
                            onViewAllClick = onViewAllTransactions,
                            horizontalPadding = HomeScreenHorizontalPadding
                        )
                    }

                    // Секция ленты операций
                    when {
                        uiState.recentTransaction.isEmpty() -> {
                            item {
                                HomeEmptyTransactionsView(horizontalPadding = HomeScreenHorizontalPadding)
                            }
                        }

                        else -> {
                            uiState.groupedDays.forEach { dayGroup ->
                                transactionDayItems(
                                    dayGroup = dayGroup,
                                    categoriesMap = categoriesMap,
                                    accountsMap = accountsMap,
                                    onTransactionClick = onTransactionsClick,
                                    itemHorizontalPadding = HomeScreenHorizontalPadding
                                )
                            }
                        }
                    }

                    if (uiState.isLoadingMore) {
                        item(key = "pagination_loader") {
                            HomePaginationLoader()
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = contentPadding.calculateBottomPadding() + SnackbarBottomClearance)
                )
            }
        }
    }

    if (showAddAccountSheet) {
        AddAccount(
            onDismiss = { showAddAccountSheet = false },
            onAddClick = { name, initialBalance, isDefault, type ->
                onAddAccount(
                    name,
                    initialBalance,
                    isDefault,
                    type
                )
                showAddAccountSheet = false
            },
            onCheckDeletedAccount = onCheckDeletedAccount,
            onRestoreDeletedAccount = onRestoreDeletedAccount,
            onPurgeDeletedAccount = onPurgeDeletedAccount,
            onGetAccountTransactions = onGetAccountTransactions,
            categories = uiState.categories,
            deletedAccounts = uiState.deletedAccounts,
            accounts = uiState.accounts,
        )
    }
}

// =========================================================================
// 2. ВСПОМОГАТЕЛЬНЫЕ ПОДКОМПОНЕНТЫ (SUB-COMPOSABLES)
// =========================================================================

/**
 * Полноэкранный индикатор начальной загрузки данных.
 *
 * @param modifier Модификатор контейнера.
 */
@Composable
private fun HomeLoadingView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Полноэкранный экран фатальной ошибки при отсутствии данных.
 *
 * @param message Текст сообщения об ошибке.
 * @param onDismiss Колбэк нажатия на кнопку подтверждения («Понятно»).
 * @param modifier Модификатор контейнера.
 */
@Composable
private fun HomeErrorView(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Button(onClick = onDismiss) {
                Text("Понятно")
            }
        }
    }
}

/**
 * Секция счетов пользователя: заголовок и горизонтальная карусель карточек с кнопкой добавления счёта.
 *
 * @param accounts Список активных счетов пользователя.
 * @param cardWidth Рассчитанная ширина одной карточки для адаптивной сетки.
 * @param cardSpacing Расстояние между соседними карточками.
 * @param horizontalPadding Отступ карусели от краев экрана.
 * @param onAccountClick Вызывается при нажатии на карточку счёта.
 * @param onAddAccountClick Вызывается при нажатии на карточку «+».
 * @param modifier Модификатор контейнера секции.
 */
@Composable
private fun HomeAccountsSection(
    accounts: List<Account>,
    cardWidth: Dp,
    cardSpacing: Dp,
    horizontalPadding: Dp,
    onAccountClick: (String) -> Unit,
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Мои счета",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing)
        ) {
            items(accounts, key = { it.id }) { account ->
                HomeAccountCard(
                    account = account,
                    modifier = Modifier.width(cardWidth),
                    onClick = { onAccountClick(account.id) }
                )
            }
            item {
                AddAccountCardItem(
                    modifier = Modifier.width(cardWidth),
                    onClick = onAddAccountClick
                )
            }
        }
    }
}

/**
 * Заголовок ленты последних операций со ссылкой перехода ко всей истории.
 *
 * @param onViewAllClick Колбэк нажатия на кнопку «Все операции».
 * @param horizontalPadding Горизонтальный отступ заголовка.
 * @param modifier Модификатор контейнера строки.
 */
@Composable
private fun HomeTransactionsHeader(
    onViewAllClick: () -> Unit,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding, end = horizontalPadding, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Последние операции",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Все операции",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onViewAllClick)
        )
    }
}

/**
 * Заглушка, отображаемая при отсутствии операций в истории.
 *
 * @param horizontalPadding Горизонтальный отступ блока.
 * @param modifier Модификатор контейнера.
 */
@Composable
private fun HomeEmptyTransactionsView(
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Операций пока нет.\nНажмите +, чтобы добавить первую.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Индикатор дополнительной постраничной загрузки внизу списка операций.
 *
 * @param modifier Модификатор контейнера индикатора.
 */
@Composable
private fun HomePaginationLoader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// =========================================================================
// 3. PREVIEWS & ТЕСТОВЫЕ ДАННЫЕ
// =========================================================================

private val PreviewAccounts = listOf(
    Account(
        id = "1",
        name = "Основная карта",
        balance = 45200.0,
        currency = "RUB",
        type = AccountType.CARD,
        isDefault = true
    ),
    Account(
        id = "2",
        name = "Накопительный",
        balance = 120000.0,
        currency = "RUB",
        type = AccountType.SAVINGS
    )
)
private val PreviewCategories = listOf(
    Category(
        id = "1",
        name = "Продукты",
        type = CategoryType.EXPENSE,
        iconRes = "ic_shopping_cart",
        budgetLimit = 0.0
    )
)
private val PreviewTransactions = listOf(
    Transaction(
        id = "1",
        amount = 2070.0,
        categoryId = "1",
        accountId = "1",
        type = TransactionType.EXPENSE,
        dateTime = System.currentTimeMillis(),
        note = "Покупка продуктов"
    )
)
@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                totalBalance = 165200.0,
                monthlyIncome = 85000.0,
                monthlyExpense = 32000.0,
                currentMonthName = "ноябрь",
                accounts = PreviewAccounts,
                categories = PreviewCategories,
                recentTransaction = PreviewTransactions
            )
        )
    }
}
@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun HomeScreenDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                totalBalance = 165200.0,
                monthlyIncome = 85000.0,
                monthlyExpense = 32000.0,
                currentMonthName = "ноябрь",
                accounts = PreviewAccounts,
                categories = PreviewCategories,
                recentTransaction = PreviewTransactions
            )
        )
    }
}
@Preview(name = "Empty State", showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    FinancetrackerTheme(darkTheme = false) {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                totalBalance = 0.0,
                accounts = PreviewAccounts,
                categories = PreviewCategories,
                recentTransaction = emptyList()
            )
        )
    }
}
@Preview(name = "Loading State", showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    FinancetrackerTheme(darkTheme = false) {
        HomeScreenContent(
            uiState = HomeUiState(isLoading = true)
        )
    }
}