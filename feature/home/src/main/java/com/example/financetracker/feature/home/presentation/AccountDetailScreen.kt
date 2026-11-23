package com.example.financetracker.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionDayGroup
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.R
import com.example.financetracker.core.uikit.components.account.AccountActionButton
import com.example.financetracker.core.uikit.components.account.AccountBalanceCard
import com.example.financetracker.core.uikit.components.account.DeletedAccountBanner
import com.example.financetracker.core.uikit.theme.Expense
import com.example.financetracker.core.uikit.theme.ExpenseSoft
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.feature.home.presentation.components.sheet.EditAccountBottomSheet
import com.example.financetracker.feature.home.presentation.components.transactionDayItems
import com.example.financetracker.feature.home.util.groupTransactionByDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import com.example.financetracker.core.model.MonthlySummary
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    account: Account,
    onBack: () -> Unit,
    onSave: (Account) -> Unit,
    onDelete: (String, Boolean) -> Unit = {_, _ ->},
    transactions: List<Transaction> = emptyList(),
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    monthName: String = "",
    onActionClick: (TransactionType) -> Unit = {},
    onGetAccountTransactions: (suspend (accountId: String, limit: Int) -> List<Transaction>)? = null,
    onGetAccountMonthlySummary: ((accountId: String) -> Flow<MonthlySummary>)? = null,
    onToggleDefaultAccount: ((accountId: String, isDefault: Boolean) -> Unit)? = null,
) {
    var editing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    var transactionLimit by rememberSaveable(account.id) { mutableIntStateOf(30) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMoreTransactions by remember { mutableStateOf(true) }
    var loadedTransactions by remember(account.id) { mutableStateOf<List<Transaction>?>(null) }
    var groupedDays by remember(account.id) { mutableStateOf<List<TransactionDayGroup>>(emptyList()) }

    // // Загрузка операций при смене счёта, лимита или поступлении новых транзакций
    LaunchedEffect(account.id, transactionLimit, transactions) {
        if (onGetAccountTransactions != null) {
            val list = onGetAccountTransactions(account.id, transactionLimit)
            loadedTransactions = list
            hasMoreTransactions = list.size >= transactionLimit
            isLoadingMore = false
        }
    }

    val effectiveTransactions = loadedTransactions ?: transactions
    // Фильтруем операции, относящиеся только к этому счёту (списание, пополнение или перевод)
    val accountTransactions = remember(effectiveTransactions, account.id) {
        effectiveTransactions.filter { it.accountId == account.id || it.targetAccountId == account.id }
    }

    // Реактивная сводка доходов и расходов по данному счёту за текущий месяц
    val monthlySummary by produceState(initialValue = MonthlySummary(), key1 = account.id) {
        onGetAccountMonthlySummary?.invoke(account.id)?.collect {
            value = it
        }
    }

    // Фоновая группировка по дням
    LaunchedEffect(accountTransactions) {
        groupedDays = withContext(Dispatchers.Default) {
            groupTransactionByDay(accountTransactions)
        }
    }

    // Триггер дозагрузки за 2 элемента до конца списка
    LaunchedEffect(listState, hasMoreTransactions, isLoadingMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 2
        }
        .distinctUntilChanged()
        .collect { nearBottom ->
            if (nearBottom && hasMoreTransactions && !isLoadingMore) {
                isLoadingMore = true
                transactionLimit += 30
            }
        }
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
                        text = if (account.isDeleted) "Все операции" else account.name,
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
                    if (!account.isDeleted) {
                        IconButton(
                            onClick = { editing = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать"
                            )
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Удалить"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            )
        ) {
            // Карточка баланса
            item {
                AccountBalanceCard(
                    title = "Текущий баланс · ${account.type.title}",
                    balance = account.balance,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    subtitle = if (account.isDeleted) {
                        account.deletedAt?.let {
                            val dateFormat = SimpleDateFormat("dd.MM.yyyy", LocalLocale.current.platformLocale)
                            "Удалён: ${dateFormat.format(Date(it))}"
                        }
                    } else null,
                    trailingContent = {
                        if (account.isDeleted) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "В корзине",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    onToggleDefaultAccount?.invoke(account.id, !account.isDefault)
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (account.isDefault) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                                    ),
                                    contentDescription = if (account.isDefault) {
                                        "Снять статус по умолчанию"
                                    } else "Сделать счётом по умолчанию",
                                    tint = if (account.isDefault) Color(0xFFFFD700) else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    },
                    incomeTitle = if (!account.isDeleted) "Доходы за ${monthName.ifEmpty { "месяц" }}" else null,
                    incomeAmount = if (!account.isDeleted) monthlySummary.income else null,
                    expenseTitle = if (!account.isDeleted) "Расходы за ${monthName.ifEmpty { "месяц" }}" else null,
                    expenseAmount = if (!account.isDeleted) monthlySummary.expense else null
                )
            }

            // Кнопки быстрых действий
            if (!account.isDeleted) {
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
                            onClick = { onActionClick(TransactionType.EXPENSE) },
                            modifier = Modifier.weight(1f)
                        )
                        AccountActionButton(
                            title = "Доход",
                            icon = Icons.Default.ArrowUpward,
                            onClick = { onActionClick(TransactionType.INCOME) },
                            modifier = Modifier.weight(1f)
                        )
                        AccountActionButton(
                            title = "Перевод",
                            icon = Icons.Default.SwapHoriz,
                            onClick = { onActionClick(TransactionType.TRANSFER) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                item {
                    DeletedAccountBanner(modifier = Modifier.padding(horizontal = 20.dp))
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
            if (accountTransactions.isEmpty() && !isLoadingMore) {
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
                    transactionDayItems(
                        dayGroup = dayGroup,
                        categoriesMap = categoriesMap,
                        accountsMap = accountsMap,
                        account = account,
                        itemHorizontalPadding = 20.dp,
                        onTransactionClick = {}
                    )
                }
            }

            // Индикатор дозагрузки внизу списка
            if (isLoadingMore) {
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
    }
    if (editing) {
        EditAccountBottomSheet(
            account = account,
            onDismiss = { editing = false },
            onSave = onSave
        )
    }

    if (showDeleteDialog) {
        var deleteTransactions by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ExpenseSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Expense,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Удалить счёт?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Счёт «${account.name}» будет перемещён в корзину. Вы сможете восстановить его позже в профиле.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { deleteTransactions = !deleteTransactions }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = deleteTransactions,
                                onCheckedChange = { deleteTransactions = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Expense,
                                    checkmarkColor = Color.White
                                )
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = "Удалить связанные операции",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Отмена — 1/3 ширины
                    TextButton(
                        onClick = { showDeleteDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Отмена",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    // В корзину — 2/3 ширины
                    Button(
                        onClick = {
                            onBack()
                            onDelete(account.id, deleteTransactions)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Expense,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text(
                            text = "В корзину",
                            maxLines = 1
                        )
                    }
                }
            },
            dismissButton = null
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
                type = AccountType.CARD,
                isDefault = true
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