package com.example.financetracker.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.feature.home.components.AddAccountBottomSheet
import com.example.financetracker.core.uikit.components.account.AddAccountCardItem
import com.example.financetracker.core.uikit.components.text.AmountText
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.feature.home.components.HomeAccountCard
import com.example.financetracker.feature.home.components.HomeTransactionRow
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onAddAccount = { name, initialBalance ->
            viewModel.createAccount(name, initialBalance)
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onAddAccount: (name: String, initialBalance: Double) -> Unit = { _, _ -> }
) {
    var showAddAccountSheet by remember { mutableStateOf(false) }

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        uiState.errorMessage != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // Общий баланс
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Общий баланс",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AmountText(
                            amount = uiState.totalBalance,
                            type = null,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
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
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.accounts, key = { it.id }) { account ->
                            HomeAccountCard(account = account, onClick = {})
                        }
                        item {
                            AddAccountCardItem(
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
                        items(uiState.recentTransaction, key = { it.id }) { transaction ->
                            val account = uiState.accounts.find { it.id == transaction.accountId }
                            val category = uiState.categories.find { it.id == transaction.categoryId }

                            HomeTransactionRow(
                                transaction = transaction,
                                category = category,
                                account = account,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddAccountSheet) {
        AddAccountBottomSheet(
            onDismiss = { showAddAccountSheet = false },
            onAddClick = { name, initialBalance ->
                onAddAccount(name, initialBalance)
            }
        )
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
                        currency = "RUB"
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
            )
        )
    }
}