package com.example.financetracker.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.MonthlySummary
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.navigation.ScreenKey
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.feature.analytics.AnalyticsScreen
import com.example.financetracker.feature.home.domain.model.ParsedTransactionDraft
import com.example.financetracker.feature.home.presentation.AccountDetailScreen
import com.example.financetracker.feature.home.presentation.HomeScreenContent
import com.example.financetracker.feature.home.presentation.HomeUiState
import com.example.financetracker.feature.home.presentation.HomeViewModel
import com.example.financetracker.feature.home.presentation.components.sheet.AddAccount
import com.example.financetracker.feature.home.presentation.components.sheet.QuickAddTransactionSheet
import com.example.financetracker.feature.profile.CategoriesScreen
import com.example.financetracker.feature.profile.ProfileScreen
import com.example.financetracker.feature.profile.TrashScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
        onCreateAccount = { name, initialBalance, isDefault, type ->
            homeViewModel.createAccount(name, initialBalance, isDefault = isDefault, type = type)
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
        onGetAccountMonthlySummary = { accountId ->
            homeViewModel.getAccountMonthlySummary(accountId)
        },
        onLoadMore = {
            homeViewModel.loadMoreTransactions()
        },
        onToggleDefaultAccount = { id, isDefault ->
            homeViewModel.setDefaultAccount(id, isDefault)
        },
        onLearnFromFastInput = { rawText, categoryId, accountId, catChanged, accChanged ->
            homeViewModel.learnFromFastInput(rawText, categoryId, accountId, catChanged, accChanged)
        },
        onParseFastInput = { text, defaultAccId ->
            homeViewModel.parseFastInput(text, defaultAccId)
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
    onCreateAccount: (String, Double, Boolean, AccountType) -> Unit = { _, _, _, _ -> },
    onUpdateAccount: (Account) -> Unit,
    onSaveCategoryBudget: (categoryId: String, limit: Double?) -> Unit = { _, _ -> },
    onCreateCategory: (name: String, type: CategoryType, iconRes: String, limit: Double?) -> Unit = { _, _, _, _ -> },
    onDeleteCategory: (categoryId: String) -> Unit = {},
    onDeleteAccount: (accountId: String, deleteTransactions: Boolean) -> Unit = { _, _ ->},
    onCheckDeletedAccount: suspend (String) -> Account? = { null },
    onRestoreAccount: (String) -> Unit = {},
    onPurgeAccount: (String) -> Unit = {},
    onGetAccountTransactions: suspend (String, Int) -> List<Transaction> = { _, _ -> emptyList() },
    onGetAccountMonthlySummary: (String) -> Flow<MonthlySummary> = { flowOf(MonthlySummary()) },
    onLoadMore: () -> Unit = {},
    onToggleDefaultAccount: (String, Boolean) -> Unit = { _, _ -> },
    onLearnFromFastInput: (String, String?, String?, Boolean, Boolean) -> Unit = { _, _, _, _, _ -> },
    onParseFastInput: (suspend (text: String, defaultAccountId: String?) -> ParsedTransactionDraft)? = null,
    onClearError: () -> Unit = {}
) {

    // Стек навигации Navigation 3 для детальных экранов (автоматически переживает поворот экрана и смерть процесса)
    val backStack = rememberNavBackStack(ScreenKey.Home)

    val navigateTo: (ScreenKey) -> Unit = { targetKey ->
        if (backStack.lastOrNull() != targetKey) {
            backStack.add(targetKey)
        }
    }
    val navigateBackSafely: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
    val popScreen: (ScreenKey) -> Unit = { expectedKey ->
        if (backStack.size > 1 && backStack.lastOrNull() == expectedKey) {
            backStack.removeLastOrNull()
        }
    }

    val currentKey = backStack.lastOrNull() ?: ScreenKey.Home
    val currentHomeUiState by rememberUpdatedState(homeUiState)

    // Скрытый detail-экран отключает локальный BackHandler вкладок во время анимации выхода
    val isDetailOpen = currentKey != ScreenKey.Home
    val currentIsDetailOpen by rememberUpdatedState(isDetailOpen)

    var showQuickAdd by rememberSaveable { mutableStateOf(false) }
    var quickAddType by rememberSaveable { mutableStateOf(TransactionType.EXPENSE) }
    var quickAddAccountId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddAccountSheet by rememberSaveable { mutableStateOf(false) }

    // NavDisplay отрисовывает экраны, задаёт общие переходы и управляет системным "Назад"
    NavDisplay(
        backStack = backStack,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        onBack = navigateBackSafely,
        transitionSpec = NavigationTransitions.horizontalPush(),
        popTransitionSpec = NavigationTransitions.horizontalPop(),
        predictivePopTransitionSpec = NavigationTransitions.horizontalPredictivePop()
    ) { key ->
        when (key) {
            // Основной экран с горизонтальным свайпом вкладок и нижней панелью
            is ScreenKey.Home -> NavEntry(key) {
                MainTabsContent(
                    homeUiState = currentHomeUiState,
                    isBackHandlerEnabled = !currentIsDetailOpen,
                    onCreateAccount = onCreateAccount,
                    onAccountClick = { accountId ->
                        navigateTo(ScreenKey.AccountDetail(accountId))
                    },
                    onCheckDeletedAccount = onCheckDeletedAccount,
                    onRestoreAccount = onRestoreAccount,
                    onPurgeAccount = onPurgeAccount,
                    onGetAccountTransactions = onGetAccountTransactions,
                    onLoadMore = onLoadMore,
                    onClearError = onClearError,
                    onOpenCategories = {
                        navigateTo(ScreenKey.Categories)
                    },
                    onOpenTrash = {
                        navigateTo(ScreenKey.Trash)
                    },
                    onAddTransactionClick = {
                        quickAddType = TransactionType.EXPENSE
                        quickAddAccountId = null
                        showQuickAdd = true
                    }
                )
            }

            is ScreenKey.AccountDetail -> NavEntry(key) {
                val allAccounts = remember(
                    currentHomeUiState.accounts,
                    currentHomeUiState.deletedAccounts
                ) {
                    currentHomeUiState.accounts + currentHomeUiState.deletedAccounts
                }
                val matchedAccount = allAccounts.find { it.id == key.accountId}
                var cachedAccount by remember(key) { mutableStateOf(matchedAccount) }
                val isCurrentEntry = backStack.lastOrNull() == key

                // Фиксируем снимок счета на время анимации выхода, чтобы после удаления
                // интерфейс не перепрыгивал в вид "из корзины" прямо во время сдвига
                if (isCurrentEntry && matchedAccount != null) {
                    cachedAccount = matchedAccount
                }
                val displayAccount = if (isCurrentEntry) {
                    matchedAccount
                } else {
                    cachedAccount ?: matchedAccount
                }

                when {
                    displayAccount != null -> {
                        AccountDetailScreen(
                            account = displayAccount,
                            onBack = { popScreen(key) },
                            onSave = onUpdateAccount,
                            onDelete = onDeleteAccount,
                            onToggleDefaultAccount = onToggleDefaultAccount,
                            transactions = currentHomeUiState.recentTransaction,
                            categories = currentHomeUiState.categories,
                            accounts = allAccounts,
                            monthName = currentHomeUiState.currentMonthName,
                            onActionClick = { type ->
                                showQuickAdd = true
                                quickAddType = type
                                quickAddAccountId = displayAccount.id
                            },
                            onGetAccountTransactions = onGetAccountTransactions,
                            onGetAccountMonthlySummary = onGetAccountMonthlySummary,
                        )
                    }

                    currentHomeUiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {
                        // Если аккаунт отсутствует уже после загрузки — безопасно закрываем экран
                        LaunchedEffect(key, isCurrentEntry) {
                            if (isCurrentEntry) {
                                popScreen(key)
                            }
                        }
                    }
                }
            }

            is ScreenKey.Categories -> NavEntry(key) {
                CategoriesScreen(
                    categories = currentHomeUiState.categories,
                    onBack = { popScreen(key) },
                    onSaveBudgetLimit = onSaveCategoryBudget,
                    onCreateCategory = onCreateCategory,
                    onDeleteCategory = onDeleteCategory
                )
            }

            is ScreenKey.Trash -> NavEntry(key) {
                TrashScreen(
                    deletedAccounts = currentHomeUiState.deletedAccounts,
                    categories = currentHomeUiState.categories,
                    onBack = { popScreen(key) },
                    onRestoreAccount = onRestoreAccount,
                    onPurgeAccount = onPurgeAccount,
                    onGetAccountTransactions = { id -> onGetAccountTransactions(id, 100) },
                    onOpenAccountDetails = { accountId ->
                        navigateTo(ScreenKey.AccountDetail(accountId))
                    }
                )
            }

            else -> NavEntry(key) {}
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
            },
            learnedCategoryKeywords = homeUiState.learnedCategoryKeywords,
            learnedAccountAliases = homeUiState.learnedAccountAliases,
            onLearnFromFastInput = onLearnFromFastInput,
            onParseFastInput = onParseFastInput,
        )
    }

    if (showAddAccountSheet) {
        AddAccount(
            onDismiss = {
                showAddAccountSheet = false
                // При закрытии возвращаем шторку операции назад
                showQuickAdd = true
            },
            onAddClick = { name, initialBalance, isDefault, type ->
                onCreateAccount(name, initialBalance, isDefault, type)
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
            accounts = currentHomeUiState.accounts,
        )
    }
}

@Composable
private fun MainTabsContent(
    homeUiState: HomeUiState,
    isBackHandlerEnabled: Boolean,
    onCreateAccount: (String, Double, Boolean, AccountType) -> Unit,
    onAccountClick: (String) -> Unit,
    onCheckDeletedAccount: suspend (String) -> Account?,
    onRestoreAccount: (String) -> Unit,
    onPurgeAccount: (String) -> Unit,
    onGetAccountTransactions: suspend (String, Int) -> List<Transaction>,
    onLoadMore: () -> Unit,
    onClearError: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenTrash: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
    // Пейджер для горизонтального свайпа между тремя вкладками
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { NavTab.entries.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Активная вкладка синхронизирована с целевой страницей свайпа
    val currentTab = NavTab.entries[pagerState.targetPage]

    // Нажатие Назад на Аналитике или Профиле возвращает на Главную
    BackHandler(enabled = isBackHandlerEnabled && pagerState.targetPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(tab.ordinal)
                    }
                }
            )
        },
        floatingActionButton = {
            // Кнопка + плавно появляется только на Главной странице
            AnimatedVisibility(
                visible = pagerState.targetPage == 0,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onAddTransactionClick,
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // Предварительная загрузка соседних вкладок для 120 fps
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> HomeScreenContent(
                    uiState = homeUiState,
                    contentPadding = innerPadding,
                    onAddAccount = onCreateAccount,
                    onAccountClick = onAccountClick,
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
                    onOpenCategories = onOpenCategories,
                    onOpenTrash = onOpenTrash
                )
            }
        }
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
            onCreateAccount = { _, _, _, _ -> },
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
            onCreateAccount = { _, _, _, _ -> },
            onUpdateAccount = {}
        )
    }
}
