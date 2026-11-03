package com.example.financetracker.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.feature.home.components.sheet.QuickAddTransactionSheet
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.feature.home.AccountDetailScreen
import com.example.financetracker.feature.home.HomeScreenContent
import com.example.financetracker.feature.home.HomeUiState
import com.example.financetracker.feature.home.HomeViewModel
import com.example.financetracker.feature.home.components.sheet.AddAccountBottomSheet
import com.example.financetracker.placeholder.AnalyticsPlaceholderScreen
import com.example.financetracker.placeholder.ProfilePlaceholderScreen
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
    onClearError: () -> Unit = {}
) {
    // 1. Стек навигации Navigation 3 (автоматически переживает поворот экрана и смерть процесса)
    val backStack = rememberNavBackStack(ScreenKey.Home)
    val currentKey = backStack.lastOrNull() ?: ScreenKey.Home
    val currentHomeUiState by rememberUpdatedState(homeUiState)

    // 2. Детальный экран скрывает нижний бар и FAB
    val isDetailOpen = currentKey is ScreenKey.AccountDetail

    var showQuickAdd by remember { mutableStateOf(false) }
    var showAddAccountSheet by rememberSaveable { mutableStateOf(false) }

    // 3. Вычисляем текущую выбранную вкладку из стека
    val currentTab = when (currentKey) {
        is ScreenKey.Analytics -> NavTab.ANALYTICS
        is ScreenKey.Profile -> NavTab.PROFILE
        else -> NavTab.HOME
    }

    Scaffold(
        bottomBar = {
            if (!isDetailOpen) {
                BottomNavBar(
                    currentTab,
                    onTabSelected = { tab ->
                        val targetKey = when (tab) {
                            NavTab.HOME -> ScreenKey.Home
                            NavTab.ANALYTICS -> ScreenKey.Analytics
                            NavTab.PROFILE -> ScreenKey.Profile
                        }
                        if (currentKey != targetKey) {
                            if (targetKey == ScreenKey.Home) {
                                backStack.clear()
                                backStack.add(ScreenKey.Home)
                            } else {
                                backStack.clear()
                                backStack.add(ScreenKey.Home)
                                backStack.add(targetKey)
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isDetailOpen && currentKey is ScreenKey.Home) {
                FloatingActionButton(
                    onClick = { showQuickAdd = true },
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
        // 4. NavDisplay отрисовывает экраны и автоматически управляет системным «Назад»
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { backStack.removeLastOrNull() }
        ) { key ->
            when (key) {
                is ScreenKey.Home -> NavEntry(key) {
                    HomeScreenContent(
                        uiState = currentHomeUiState,
                        contentPadding = innerPadding,
                        onAccountClick = { accountId ->
                            backStack.add(ScreenKey.AccountDetail(accountId))
                        },
                        onAddAccount = onCreateAccount,
                        onClearError = onClearError
                    )
                }

                is ScreenKey.Analytics -> NavEntry(key) {
                    AnalyticsPlaceholderScreen(Modifier.padding(innerPadding))
                }

                is ScreenKey.Profile -> NavEntry(key) {
                    ProfilePlaceholderScreen(Modifier.padding(innerPadding))
                }

                is ScreenKey.AccountDetail -> NavEntry(key) {
                    val account = currentHomeUiState.accounts.find { it.id == key.accountId }
                    if (account != null) {
                        AccountDetailScreen(
                            account = account,
                            onBack = { backStack.removeLastOrNull() },
                            onSave = onUpdateAccount
                        )
                    } else {
                        // Если аккаунт не найден (например, удален), корректно возвращаемся назад
                        LaunchedEffect(Unit) {
                            backStack.removeLastOrNull()
                        }
                    }
                }

                else -> NavEntry(key) {}
            }
        }
    }

    if (showQuickAdd) {
        QuickAddTransactionSheet(
            categories = homeUiState.categories,
            accounts = homeUiState.accounts,
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
            }
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