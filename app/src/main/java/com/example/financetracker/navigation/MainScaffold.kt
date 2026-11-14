package com.example.financetracker.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.financetracker.feature.home.components.sheet.QuickAddTransactionSheet
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.feature.home.AccountDetailScreen
import com.example.financetracker.feature.home.CategoriesScreen
import com.example.financetracker.feature.home.HomeScreenContent
import com.example.financetracker.feature.home.HomeUiState
import com.example.financetracker.feature.home.HomeViewModel
import com.example.financetracker.feature.home.components.sheet.AddAccountBottomSheet
import com.example.financetracker.feature.analytics.AnalyticsScreen
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
        onCreateAccount = { name, initialBalance ->
            homeViewModel.createAccount(name, initialBalance)
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
        onGetAccountTransactions = { accountId ->
            homeViewModel.getAccountTransactions(accountId)
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
    onCreateAccount: (String, Double) -> Unit,
    onUpdateAccount: (Account) -> Unit,
    onSaveCategoryBudget: (categoryId: String, limit: Double?) -> Unit = { _, _ -> },
    onCreateCategory: (name: String, type: CategoryType, iconRes: String, limit: Double?) -> Unit = { _, _, _, _ -> },
    onDeleteCategory: (categoryId: String) -> Unit = {},
    onDeleteAccount: (accountId: String, deleteTransactions: Boolean) -> Unit = { _, _ ->},
    onCheckDeletedAccount: suspend (String) -> Account? = { null },
    onRestoreAccount: (String) -> Unit = {},
    onPurgeAccount: (String) -> Unit = {},
    onGetAccountTransactions: suspend (String) -> List<Transaction> = { emptyList() },
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
                                onAccountClick = { accountId ->
                                    backStack.add(ScreenKey.AccountDetail(accountId))
                                },
                                onAddAccount = onCreateAccount,
                                onCheckDeletedAccount = onCheckDeletedAccount,
                                onRestoreDeletedAccount = onRestoreAccount,
                                onPurgeDeletedAccount = onPurgeAccount,
                                onGetAccountTransactions = onGetAccountTransactions,
                                onClearError = onClearError
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
                    if (account != null) {
                        AccountDetailScreen(
                            account = account,
                            onBack = navigateBackSafely,
                            onSave = onUpdateAccount,
                            onDelete = onDeleteAccount,
                            transactions = currentHomeUiState.recentTransaction,
                            categories = currentHomeUiState.categories,
                            accounts = currentHomeUiState.accounts,
                            onActionClick = { type ->
                                showQuickAdd = true
                                quickAddType = type
                                quickAddAccountId = account.id
                            }
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
                        onGetAccountTransactions = onGetAccountTransactions
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
            onAddClick = { name, initialBalance ->
                onCreateAccount(name, initialBalance)
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
            onGetAccountTransactions = onGetAccountTransactions,
            categories = currentHomeUiState.categories
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
                    Account(id = "1", name = "Основная карта", balance = 43965.72, currency = "RUB")
                )
            ),
            onAddTransaction = { _, _, _, _, _, _, _ -> },
            onCreateAccount = { _, _ -> },
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
                    Account(id = "1", name = "Основная карта", balance = 43965.72, currency = "RUB")
                )
            ),
            onAddTransaction = { _, _, _, _, _, _, _ -> },
            onCreateAccount = { _, _ -> },
            onUpdateAccount = {}
        )
    }
}
