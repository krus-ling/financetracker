package com.example.financetracker.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.navigation.ScreenKey
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.feature.analytics.AnalyticsScreen
import com.example.financetracker.feature.home.presentation.AccountDetailScreen
import com.example.financetracker.feature.home.presentation.HomeScreenContent
import com.example.financetracker.feature.home.presentation.HomeUiState
import com.example.financetracker.feature.home.presentation.HomeViewModel
import com.example.financetracker.feature.home.presentation.components.sheet.AddAccountBottomSheet
import com.example.financetracker.feature.home.presentation.components.sheet.QuickAddTransactionSheet
import com.example.financetracker.feature.profile.CategoriesScreen
import com.example.financetracker.feature.profile.ProfileScreen
import com.example.financetracker.feature.profile.TrashScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScaffold(
    homeViewModel: HomeViewModel = koinViewModel()
) {

    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    MainScaffoldContent(
        homeUiState = homeUiState,
        onAddTransaction = { amount, categoryId, accountId, targetAccountId, type, note, dateTime ->
            homeViewModel.addTransaction(
                amount = amount,
                categoryId = categoryId,
                accountId = accountId,
                targetAccountId = targetAccountId,
                type = type,
                note = note,
                dateTime = dateTime
            )
        },
        onCreateAccount = { name, initialBalance, isDefault ->
            homeViewModel.createAccount(name, initialBalance, isDefault = isDefault)
        },
        onUpdateAccount = { account ->
            homeViewModel.updateAccount(account)
        },
        onSaveCategoryBudget = { categoryId, limit ->
            homeViewModel.updateCategoryBudget(categoryId, limit)
        },
        onCreateCategory = { name, type, iconRes, limit ->
            homeViewModel.createCategory(name, type, iconRes, limit)
        },
        onDeleteCategory = { categoryId ->
            homeViewModel.deleteCategory(categoryId)
        },
        onDeleteAccount = { accountId, deleteTransactions ->
            homeViewModel.deleteAccount(accountId, deleteTransactions)
        },
        onCheckDeletedAccount = { name ->
            homeViewModel.findDeletedAccountByName(name)
        },
        onRestoreAccount = { accountId ->
            homeViewModel.restoreAccount(accountId)
        },
        onPurgeAccount = {
            homeViewModel.purgeAccount(it)
        },
        onGetAccountTransactions = { accountId, limit ->
            homeViewModel.getAccountTransactions(accountId, limit)
        },
        onLoadMore = {
            homeViewModel.loadMoreTransactions()
        },
        onToggleDefaultAccount = { id, isDefault ->
            homeViewModel.setDefaultAccount(id, isDefault)
        },
        onClearError = {
            homeViewModel.clearError()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldContent(
    homeUiState: HomeUiState,
    onAddTransaction: (Double, String?, String, String?, TransactionType, String, Long) -> Unit,
    onCreateAccount: (String, Double, Boolean) -> Unit = { _, _, _ -> },
    onUpdateAccount: (Account) -> Unit,
    onSaveCategoryBudget: (categoryId: String, limit: Double?) -> Unit = { _, _ -> },
    onCreateCategory: (name: String, type: CategoryType, iconRes: String, limit: Double?) -> Unit = { _, _, _, _ -> },
    onDeleteCategory: (categoryId: String) -> Unit = {},
    onDeleteAccount: (accountId: String, deleteTransactions: Boolean) -> Unit = { _, _ ->},
    onCheckDeletedAccount: suspend (String) -> Account? = { null },
    onRestoreAccount: (String) -> Unit = {},
    onPurgeAccount: (String) -> Unit = {},
    onGetAccountTransactions: suspend (String, Int) -> List<Transaction> = { _, _ -> emptyList() },
    onLoadMore: () -> Unit = {},
    onToggleDefaultAccount: (String, Boolean) -> Unit = { _, _ -> },
    onClearError: () -> Unit = {}
) {
    // Стек навигации Navigation 3 для детальных экранов (автоматически переживает поворот экрана и смерть процесса)
    val backStack = rememberNavBackStack(ScreenKey.Home)
    val navigateBackSafely = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
    val currentKey = backStack.lastOrNull() ?: ScreenKey.Home
    val currentHomeUiState by rememberUpdatedState(homeUiState)

    // Детальный экран скрывает нижний бар и FAB
    val isDetailOpen = currentKey is ScreenKey.AccountDetail || currentKey is ScreenKey.Categories || currentKey is ScreenKey.Trash

    // Пейджер для горизонтального свайпа между тремя вкладками
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { NavTab.entries.size }
    )
    val coroutineScope = rememberCoroutineScope()

    var showQuickAdd by remember { mutableStateOf(false) }
    var quickAddType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var quickAddAccountId by remember { mutableStateOf<String?>(null) }
    var showAddAccountSheet by rememberSaveable { mutableStateOf(false) }

    // Активная вкладка синхронизирована с целевой страницей свайпа
    val currentTab = NavTab.entries[pagerState.targetPage]

    // Нажатие Назад на Аналитике или Профиле возвращает на Главную
    BackHandler(enabled = !isDetailOpen && pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    Scaffold(
        bottomBar = {
            if (!isDetailOpen) {
                BottomNavBar(
                    currentTab,
                    onTabSelected = { tab ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            // Кнопка «+» плавно появляется только на Главной вкладке
            AnimatedVisibility(
                visible = !isDetailOpen && pagerState.currentPage == 0,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        quickAddType = TransactionType.EXPENSE
                        quickAddAccountId = null
                        showQuickAdd = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить операцию")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background

    ) { innerPadding ->
        // NavDisplay отрисовывает экраны и автоматически управляет системным «Назад»
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = navigateBackSafely
        ) { key ->
            when (key) {
                // Основной экран с горизонтальным свайпом вкладок
                is ScreenKey.Home, is ScreenKey.Analytics, is ScreenKey.Profile -> NavEntry(key) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1 // Предварительная загрузка соседних вкладок для 120 fps
                    ) { page ->
                        when (page) {
                            0 -> HomeScreenContent(
                                uiState = currentHomeUiState,
                                contentPadding = innerPadding,
                                onAddAccount = onCreateAccount,
                                onAccountClick = { accountId ->
                                    val targetKey = ScreenKey.AccountDetail(accountId)
                                    if (backStack.lastOrNull() != targetKey) {
                                        backStack.add(targetKey)
                                    }
                                },
                                onCheckDeletedAccount = onCheckDeletedAccount,
                                onRestoreDeletedAccount = onRestoreAccount,
                                onPurgeDeletedAccount = onPurgeAccount,
                                onGetAccountTransactions = { id -> onGetAccountTransactions(id, 100) },
                                onLoadMore = onLoadMore,
                                onClearError = onClearError,
                            )

                            1 -> AnalyticsScreen(
                                Modifier.padding(innerPadding)
                            )

                            2 -> ProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                                onOpenCategories = {
                                    backStack.add(ScreenKey.Categories)
                                },
                                onOpenTrash = {
                                    backStack.add(ScreenKey.Trash)
                                }
                            )
                        }
                    }
                }

                is ScreenKey.AccountDetail -> NavEntry(key) {
                    val account = currentHomeUiState.accounts.find { it.id == key.accountId }
                        ?: currentHomeUiState.deletedAccounts.find { it.id == key.accountId }
                    if (account != null) {
                        AccountDetailScreen(
                            account = account,
                            onBack = navigateBackSafely,
                            onSave = onUpdateAccount,
                            onDelete = onDeleteAccount,
                            onToggleDefaultAccount = onToggleDefaultAccount,
                            transactions = currentHomeUiState.recentTransaction,
                            categories = currentHomeUiState.categories,
                            accounts = currentHomeUiState.accounts + currentHomeUiState.deletedAccounts,
                            onActionClick = { type ->
                                showQuickAdd = true
                                quickAddType = type
                                quickAddAccountId = account.id
                            },
                            onGetAccountTransactions = onGetAccountTransactions
                        )
                    } else {
                        // Если аккаунт не найден (например, удален), корректно возвращаемся назад
                        LaunchedEffect(Unit) {
                            navigateBackSafely()
                        }
                    }
                }

                is ScreenKey.Categories -> NavEntry(key) {
                    CategoriesScreen(
                        categories = currentHomeUiState.categories,
                        onBack = navigateBackSafely,
                        onSaveBudgetLimit = onSaveCategoryBudget,
                        onCreateCategory = onCreateCategory,
                        onDeleteCategory = onDeleteCategory
                    )
                }

                is ScreenKey.Trash -> NavEntry(key) {
                    TrashScreen(
                        deletedAccounts = currentHomeUiState.deletedAccounts,
                        categories = currentHomeUiState.categories,
                        onBack = navigateBackSafely,
                        onRestoreAccount = onRestoreAccount,
                        onPurgeAccount = onPurgeAccount,
                        onGetAccountTransactions = { id -> onGetAccountTransactions(id, 100) },
                        onOpenAccountDetails = { accountId ->
                            backStack.add(ScreenKey.AccountDetail(accountId))
                        }
                    )
                }

                else -> NavEntry(key) {}
            }
        }
    }

    if (showQuickAdd) {
        QuickAddTransactionSheet(
            categories = homeUiState.categories,
            accounts = homeUiState.accounts,
            initialType = quickAddType,
            initialAccountId = quickAddAccountId,
            onDismiss = { showQuickAdd = false },
            onAddNewAccount = {
                showQuickAdd = false
                showAddAccountSheet = true
            },
            onAddClick = { amount, categoryId, accountId, targetAccountId, type, note, dateTime ->
                onAddTransaction(
                    amount,
                    categoryId,
                    accountId,
                    targetAccountId,
                    type,
                    note,
                    dateTime
                )
            }
        )
    }

    if (showAddAccountSheet) {
        AddAccountBottomSheet(
            onDismiss = {
                showAddAccountSheet = false
                // При закрытии возвращаем шторку операции назад
                showQuickAdd = true
            },
            onAddClick = { name, initialBalance, isDefault ->
                onCreateAccount(name, initialBalance, isDefault)
                showAddAccountSheet = false
                // После успешного создания возвращаем шторку операции
                showQuickAdd = true
            },
            onCheckDeletedAccount = onCheckDeletedAccount,
            onRestoreDeletedAccount = { accountId ->
                onRestoreAccount(accountId)
                showAddAccountSheet = false
                showQuickAdd = true
            },
            onPurgeDeletedAccount = { accountId ->
                onPurgeAccount(accountId)
            },
            onGetAccountTransactions = { id -> onGetAccountTransactions(id, 100) },
            categories = currentHomeUiState.categories,
            deletedAccounts = currentHomeUiState.deletedAccounts,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScaffoldLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        MainScaffoldContent(
            homeUiState = HomeUiState(
                isLoading = false,
                totalBalance = 43965.72,
                accounts = listOf(
                    Account(id = "1", name = "Основная карта", balance = 43965.72, currency = "RUB", isDefault = true)
                )
            ),
            onAddTransaction = { _, _, _, _, _, _, _ -> },
            onCreateAccount = { _, _, _ -> },
            onUpdateAccount = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun MainScaffoldDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        MainScaffoldContent(
            homeUiState = HomeUiState(
                isLoading = false,
                totalBalance = 43965.72,
                accounts = listOf(
                    Account(id = "1", name = "Основная карта", balance = 43965.72, currency = "RUB", isDefault = true)
                )
            ),
            onAddTransaction = { _, _, _, _, _, _, _ -> },
            onCreateAccount = { _, _, _ -> },
            onUpdateAccount = {}
        )
    }
}
