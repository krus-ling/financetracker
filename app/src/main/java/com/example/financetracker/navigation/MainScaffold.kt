package com.example.financetracker.navigation

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var currentTab by rememberSaveable { mutableStateOf(NavTab.HOME) }
    var showQuickAdd by remember { mutableStateOf(false) }
    var showAddAccountSheet by rememberSaveable { mutableStateOf(false) }
    var selectedAccountId by rememberSaveable { mutableStateOf<String?>(null) }
    val account = homeUiState.accounts.find { it.id == selectedAccountId }

    // Представь редкую ситуацию: пользователь открыл счёт, и в этот момент счёт удалился
    // (например, удалили из фонового синхронизатора или базы). В этом случае account станет null,
    // и в теле экрана отобразится HomeScreen, но selectedAccountId всё ещё не null,
    // поэтому BottomNavBar будет скрыт.
    val isDetailOpen = account != null

    BackHandler(enabled = isDetailOpen) {
        selectedAccountId = null
    }

    Scaffold(
        bottomBar = {
            if (!isDetailOpen) {
                BottomNavBar(
                    currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        },
        floatingActionButton = {
            if (!isDetailOpen) {
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
        if (account != null) {
            AccountDetailScreen(
                account = account,
                onBack = { selectedAccountId = null },
                onSave = onUpdateAccount,
            )
        } else {
            when (currentTab) {
                NavTab.HOME -> HomeScreenContent(
                    uiState = homeUiState,
                    contentPadding = innerPadding,
                    onAccountClick = { accountId ->
                        selectedAccountId = accountId
                    },
                    onAddAccount = onCreateAccount,
                    onClearError = onClearError
                )

                NavTab.ANALYTICS -> AnalyticsPlaceholderScreen(Modifier.padding(innerPadding))
                NavTab.PROFILE -> ProfilePlaceholderScreen(Modifier.padding(innerPadding))
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