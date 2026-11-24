package com.example.financetracker.feature.home.presentation

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.financetracker.feature.home.presentation.components.sheet.AddAccountBottomSheet
import com.example.financetracker.feature.home.presentation.components.transactionDayItems
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAccountClick: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        contentPadding = contentPadding,
        onAddAccount = { name, initialBalance, isDefault, type ->
            viewModel.createAccount(name, initialBalance, isDefault = isDefault, type = type)
        },
        onAccountClick = onAccountClick,
        onClearError = { viewModel.clearError() },
        onLoadMore = { viewModel.loadMoreTransactions() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAddAccount: (name: String, initialBalance: Double, isDefault: Boolean, type: AccountType) -> Unit = { _, _, _, _ -> },
    onAccountClick: (String) -> Unit = {},
    onCheckDeletedAccount: (suspend (String) -> Account?)? = null,
    onRestoreDeletedAccount: ((String) -> Unit)? = null,
    onPurgeDeletedAccount: ((String) -> Unit)? = null,
    onGetAccountTransactions: (suspend (String) -> List<Transaction>)? = null,
    onClearError: () -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    var showAddAccountSheet by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Подгрузка истории
    LaunchedEffect(listState, uiState.hasMoreTransactions, uiState.isLoadingMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // Срабатывает заранее, за 2 элемента до конца списка
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 2
        }
        .distinctUntilChanged()
        .collect { nearBottom ->
            if (nearBottom && uiState.hasMoreTransactions && !uiState.isLoadingMore) {
                onLoadMore()
            }
        }
    }

    val addAccountStateHolder = rememberSaveableStateHolder()

    // Быстрый доступ О(1) к счетам и категориям по их id
    val accountsMap = remember(uiState.accounts) {
        uiState.accounts.associateBy { it.id }
    }
    val categoriesMap = remember(uiState.categories) {
        uiState.categories.associateBy { it.id }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Ширина одной карточки, чтобы 2 штуки заняли ровно ширину шапки
    val horizontalPadding = 20.dp
    val cardSpacing = 12.dp
    val accountCardWidth = rememberHalfWidth(horizontalPadding = horizontalPadding, spacing = cardSpacing)

    // Для локальных ошибок при уже загруженных данных показываем Snackbar, не скрывая контент
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

    BackHandler(enabled = showAddAccountSheet) {
        showAddAccountSheet = false
        addAccountStateHolder.removeState("add_account_sheet")
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Полноэкранную заглушку показываем только при сбое первичной загрузки, когда нет данных
        uiState.errorMessage != null && uiState.accounts.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onClearError
                    ) {
                        Text("Понятно")
                    }
                }
            }
        }

        else -> {
            Box(
                modifier = modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + 88.dp
                    )
                ) {
                    // Шапка профиля (заглушка)
                    item {
                        HomeHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    // Общий баланс
                    item {
                        AccountBalanceCard(
                            title = "Общий баланс",
                            balance = uiState.totalBalance,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
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
                        Text(
                            text = "Мои счета",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = horizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(cardSpacing)
                        ) {
                            items(uiState.accounts, key = { it.id }) { account ->
                                HomeAccountCard(
                                    account = account,
                                    modifier = Modifier.width(accountCardWidth),
                                    onClick = {
                                        onAccountClick(account.id)
                                    }
                                )
                            }
                            item {
                                AddAccountCardItem(
                                    modifier = Modifier.width(accountCardWidth),
                                    onClick = { showAddAccountSheet = true }
                                )
                            }
                        }
                    }

                    // Заголовок ленты операций
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
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
                                modifier = Modifier.clickable {
                                    // TODO переход на полный экран истории
                                }
                            )
                        }
                    }

                    // Секция ленты операций
                    when {
                        uiState.recentTransaction.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp, horizontal = 20.dp),
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
                        }

                        else -> {
                            uiState.groupedDays.forEach { dayGroup ->
                                transactionDayItems(
                                    dayGroup = dayGroup,
                                    categoriesMap = categoriesMap,
                                    accountsMap = accountsMap,
                                    onTransactionClick = { /* TODO клик по операции */ }
                                )
                            }
                        }
                    }

                    if (uiState.isLoadingMore) {
                        item(key = "pagination_loader") {
                            Box(
                                modifier = Modifier
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
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = contentPadding.calculateBottomPadding() + 80.dp)
                )
            }
        }
    }

    if (showAddAccountSheet) {
        addAccountStateHolder.SaveableStateProvider(key = "add_account_sheet") {
            AddAccountBottomSheet(
                onDismiss = {
                    showAddAccountSheet = false
                    addAccountStateHolder.removeState("add_account_sheet")
                },
                onAddClick = { name, initialBalance, isDefault, type ->
                    onAddAccount(
                        name,
                        initialBalance,
                        isDefault,
                        type
                    )
                    showAddAccountSheet = false
                    addAccountStateHolder.removeState("add_account_sheet")
                },
                onCheckDeletedAccount = onCheckDeletedAccount,
                onRestoreDeletedAccount = onRestoreDeletedAccount,
                onPurgeDeletedAccount = onPurgeDeletedAccount,
                onGetAccountTransactions = onGetAccountTransactions,
                categories = uiState.categories,
                deletedAccounts = uiState.deletedAccounts,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FinancetrackerTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                totalBalance = 45200.0,
                accounts = listOf(
                    Account(
                        id = "1",
                        name = "Основная карта",
                        balance = 45200.0,
                        currency = "RUB",
                        isDefault = true
                    )
                ),
                categories = listOf(
                    Category(
                        id = "1",
                        name = "Продукты",
                        type = CategoryType.EXPENSE,
                        iconRes = "shopping_cart",
                        budgetLimit = 0.0
                    )
                ),
                recentTransaction = listOf(
                    Transaction(
                        id = "1",
                        amount = 2070.0,
                        categoryId = "1",
                        accountId = "1",
                        type = TransactionType.EXPENSE,
                        dateTime = System.currentTimeMillis(),
                        note = "Скупил весь магазин"
                    )
                )
            ),
        )
    }
}
