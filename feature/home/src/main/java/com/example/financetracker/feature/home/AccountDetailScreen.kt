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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.account.AccountActionButton
import com.example.financetracker.core.uikit.components.text.AmountText
import com.example.financetracker.core.uikit.components.text.DateHeader
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.feature.home.components.HomeTransactionRow
import com.example.financetracker.feature.home.components.sheet.EditAccountBottomSheet
import com.example.financetracker.feature.home.util.groupTransactionByDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    account: Account,
    onBack: () -> Unit,
    onSave: (Account) -> Unit,
    transactions: List<Transaction> = emptyList(),
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    onActionClick: (TransactionType) -> Unit = {}
) {
    var editing by remember { mutableStateOf(false) }

    // Фильтруем операции, относящиеся только к этому счёту (списание, пополнение или перевод)
    val accountTransactions = remember(transactions, account.id) {
        transactions.filter { it.accountId == account.id || it.targetAccountId == account.id }
    }

    val groupedDays = remember(accountTransactions) {
        groupTransactionByDay(accountTransactions)
    }

    val accountsMap = remember(accounts) { accounts.associateBy { it.id } }
    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { editing = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            )
        ) {
            // Карточка баланса
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Текущий баланс · ${account.type.title}",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        AmountText(
                            amount = account.balance,
                            type = null,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Кнопки быстрых действий
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AccountActionButton(
                        title = "Расход",
                        icon = Icons.Default.ArrowDownward,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    AccountActionButton(
                        title = "Доход",
                        icon = Icons.Default.ArrowUpward,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    AccountActionButton(
                        title = "Перевод",
                        icon = Icons.Default.SwapHoriz,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            // Заголовок ленты операций
            item {
                Text(
                    text = "Операции",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 28.dp, bottom = 8.dp)
                )
            }

            // Список операций по дням или заглушка пустого состояния
            if (accountTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "По этому счёту пока нет операций",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                groupedDays.forEach { dayGroup ->
                    item(key = "header_${dayGroup.dateTitle}") {
                        DateHeader(
                            title = dayGroup.dateTitle,
                            dailyTotal = dayGroup.dailyTotal
                        )
                    }

                    item(key = "card_${dayGroup.dateTitle}") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column {
                                dayGroup.transactions.forEach { transaction ->
                                    val transactionAccount = accountsMap[transaction.accountId]
                                    val category = categoriesMap[transaction.categoryId]
                                    val targetAccount = accountsMap[transaction.targetAccountId]

                                    HomeTransactionRow(
                                        transaction = transaction,
                                        category = category,
                                        account = transactionAccount,
                                        targetAccount = targetAccount,
                                        onClick = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (editing) {
        EditAccountBottomSheet(
            account = account,
            onDismiss = { editing = false },
            onSave = onSave
        )
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun AccountDetailScreenLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AccountDetailScreen(
            account = Account(
                id = "1",
                name = "Основная карта",
                balance = 125000.0,
                currency = "RUB",
                type = AccountType.CARD
            ),
            onBack = {},
            onSave = {}
        )
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun AccountDetailScreenDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        AccountDetailScreen(
            account = Account(
                id = "1",
                name = "Основная карта",
                balance = 125000.0,
                currency = "RUB",
                type = AccountType.CARD
            ),
            onBack = {},
            onSave = {}
        )
    }
}