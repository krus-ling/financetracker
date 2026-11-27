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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.MonthlySummary
import com.example.financetracker.core.model.Transaction
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.financetracker.core.uikit.theme.StarGold

/** Горизонтальный отступ контента от краёв экрана */
private val AccountDetailHorizontalPadding = 20.dp

/** Нижний отступ прокручиваемой ленты экрана деталей счёта */
private val ContentBottomClearance = 24.dp

/** Количество операций, запрашиваемых за одну страницу пагинации */
private const val TransactionsPageSize = 30

/** За сколько элементов до конца списка срабатывает предзагрузка следующей страницы */
private const val PaginationPrefetchThreshold = 2

/** Потокобезопасный форматтер даты удаления счёта в корзине */
private val DeletedAccountDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

// =========================================================================
// 1. ОСНОВНЫЕ КОМПОНЕНТЫ ЭКРАНА (ENTRY POINTS)
// =========================================================================
/**
 * Детальный экран счёта.
 *
 * Отображает:
 * 1. Верхнюю панель ([AccountDetailTopBar]) с названием счёта и кнопками редактирования/удаления
 *    (скрыты для счетов в корзине);
 * 2. Карточку баланса ([AccountBalanceCard]) со сводкой доходов/расходов за текущий месяц и
 *    переключателем статуса «по умолчанию» (или плашкой «В корзине» для удалённого счёта);
 * 3. Панель быстрых операций ([AccountQuickActionsRow]) или информационный баннер корзины;
 * 4. Сгруппированную по дням ленту операций по этому счёту с постраничной дозагрузкой;
 * 5. Шторку редактирования ([EditAccountBottomSheet]) и диалог подтверждения удаления
 *    ([DeleteAccountConfirmDialog]).
 *
 * @param account Модель отображаемого счёта (активного или находящегося в корзине).
 * @param onBack Возврат на предыдущий экран.
 * @param onSave Сохранение обновлённых параметров счёта из шторки редактирования.
 * @param onDelete Перемещение счёта в корзину с флагом удаления связанных операций.
 * @param transactions Резервный список операций (используется в Preview/без БД или как триггер
 *   реактивного обновления ленты при добавлении новых операций).
 * @param categories Справочник категорий для отображения иконок и названий операций.
 * @param accounts Полный список счетов (активных и удалённых) для корректного отображения
 *   маршрутов переводов между счетами.
 * @param monthName Название текущего месяца для заголовков сводки в карточке баланса.
 * @param onActionClick Вызывается при нажатии на быстрые действия («Расход», «Доход», «Перевод»).
 * @param onGetAccountTransactions Асинхронный загрузчик операций по счёту с учётом лимита.
 * @param onGetAccountMonthlySummary Реактивный поток сводки доходов/расходов счёта за месяц.
 * @param onToggleDefaultAccount Переключатель статуса счёта по умолчанию.
 * @param onTransactionClick Вызывается при нажатии на строку операции в ленте.
 * @param modifier Модификатор корневого контейнера экрана.
 */
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
    onTransactionClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val isInspectionMode = LocalInspectionMode.current

    var transactionLimit by rememberSaveable(account.id) {
        mutableIntStateOf(TransactionsPageSize)
    }
    var isLoadingMore by remember(account.id) { mutableStateOf(false) }
    var hasMoreTransactions by remember(account.id) {
        mutableStateOf(onGetAccountTransactions != null)
    }
    var loadedTransactions by remember(account.id) {
        mutableStateOf<List<Transaction>?>(null)
    }

    // Первичная загрузка активна, пока БД не вернула первый срез операций для этого счёта
    val isInitialLoading = onGetAccountTransactions != null && loadedTransactions == null

    // Загрузка операций при смене счёта, расширении лимита или обновлении транзакций
    LaunchedEffect(account.id, transactionLimit, transactions,onGetAccountTransactions) {
        val fetcher = onGetAccountTransactions ?: return@LaunchedEffect
        try {
            val list = fetcher(account.id, transactionLimit)
            loadedTransactions = list
            hasMoreTransactions = list.size >= transactionLimit
            isLoadingMore = false
        } catch (e : CancellationException) {
            throw e
        } catch (_: Exception) {
            if (loadedTransactions == null) {
                loadedTransactions = emptyList()
            }
            hasMoreTransactions = false
            isLoadingMore = false
        }
    }

    // При подключённой БД не подмешиваем общую ленту HomeScreen, чтобы избежать моргания при старте
    val effectiveTransactions = if (onGetAccountTransactions != null) {
        loadedTransactions.orEmpty()
    } else {
        transactions
    }

    // Фильтруем операции, относящиеся только к этому счёту (списание, пополнение или перевод)
    val accountTransactions = remember(effectiveTransactions, account.id) {
        effectiveTransactions.filter { it.accountId == account.id || it.targetAccountId == account.id }
    }

    var groupedDays by remember(account.id) {
        mutableStateOf(
            if (isInspectionMode) groupTransactionByDay(accountTransactions) else emptyList()
        )
    }

    // Фоновая группировка операций по дням
    LaunchedEffect(accountTransactions) {
        groupedDays = withContext(Dispatchers.Default) {
            groupTransactionByDay(accountTransactions)
        }
    }


    // Реактивная сводка доходов и расходов за месяц (только для активного счета)
    val monthlySummary by produceState(
        initialValue = MonthlySummary(),
        key1 = account.id,
        key2 = account.isDeleted,
        key3 = onGetAccountMonthlySummary
    ) {
        if (!account.isDeleted) {
            onGetAccountMonthlySummary?.invoke(account.id)?.collect { value = it }
        } else {
            value = MonthlySummary()
        }
    }

    // Пагинация разрешена только при наличии загрузчика, завершенный первой загрузке и непустом списке
    val canLoadMore by rememberUpdatedState(
        onGetAccountTransactions != null &&
        hasMoreTransactions &&
        !isLoadingMore &&
        !isInitialLoading &&
        accountTransactions.isNotEmpty()
    )

    // Триггер дозагрузки за 2 элемента до конца списка без пересоздания snapshotFlow
    LaunchedEffect(listState, account.id) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            totalItems > 0 && lastVisibleItemIndex >= totalItems - PaginationPrefetchThreshold
        }
            .distinctUntilChanged()
            .collect { nearBottom ->
                if (nearBottom && canLoadMore) {
                    isLoadingMore = true
                    transactionLimit += TransactionsPageSize
                }
            }
    }

    val accountsMap = remember(accounts) { accounts.associateBy { it.id } }
    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AccountDetailTopBar(
                accountName = account.name,
                isDeleted = account.isDeleted,
                onBack = onBack,
                onEditClick = { isEditing = true },
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + ContentBottomClearance
            )
        ) {
            // Карточка баланса и месячной сводки
            item(key = "balance_card") {
                AccountBalanceSection(
                    account = account,
                    monthName = monthName,
                    monthlySummary = monthlySummary,
                    onToggleDefault = {
                        onToggleDefaultAccount?.invoke(account.id, !account.isDefault)
                    }
                )
            }

            // Кнопки быстрых действий или баннер архивного счёта
            if (!account.isDeleted) {
                item(key = "quick_actions") {
                    AccountQuickActionsRow(
                        onActionClick = onActionClick,
                        modifier = Modifier
                            .padding(horizontal = AccountDetailHorizontalPadding)
                            .padding(top = 18.dp)
                    )
                }
            } else {
                item(key = "deleted_banner") {
                    DeletedAccountBanner(
                        modifier = Modifier
                            .padding(horizontal = AccountDetailHorizontalPadding)
                            .padding(top = 14.dp)
                    )
                }
            }

            // Заголовок ленты операций
            item(key = "transactions_header") {
                Text(
                    text = "Операции",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(horizontal = AccountDetailHorizontalPadding)
                        .padding(top = 28.dp, bottom = 8.dp)
                )
            }

            // Первичная загрузка, пустое состояние или список операций по дням
            when {
                isInitialLoading -> {
                    item(key = "initial_loader") {
                        AccountPaginationLoader()
                    }
                }

                accountTransactions.isEmpty() -> {
                    item(key = "empty_transactions") {
                        AccountTransactionsEmptyState()
                    }
                }

                else -> {
                    groupedDays.forEach { dayGroup ->
                        // Передаем account = null, чтобы оба участника перевода разрешались accountsMap
                        transactionDayItems(
                            dayGroup = dayGroup,
                            categoriesMap = categoriesMap,
                            accountsMap = accountsMap,
                            account = null,
                            itemHorizontalPadding = AccountDetailHorizontalPadding,
                            onTransactionClick = onTransactionClick,
                        )
                    }
                }
            }

            // Индикатор дозагрузки следующей страницы внизу списка
            if (isLoadingMore) {
                item(key = "pagination_loader") {
                    AccountPaginationLoader()
                }
            }
        }
    }
    if (isEditing) {
        EditAccountBottomSheet(
            account = account,
            onDismiss = { isEditing = false },
            onSave = onSave
        )
    }

    if (showDeleteDialog) {
        DeleteAccountConfirmDialog(
            accountName = account.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = { deleteTransactions ->
                showDeleteDialog = false
                onDelete(account.id, deleteTransactions)
                onBack()
            }
        )
    }
}

// =========================================================================
// 2. ВСПОМОГАТЕЛЬНЫЕ ПОДКОМПОНЕНТЫ (SUB-COMPOSABLES)
// =========================================================================

/**
 * Верхняя панель экрана деталей счёта.
 *
 * @param accountName Отображаемое название счёта.
 * @param isDeleted Если `true`, действия редактирования и удаления скрываются.
 * @param onBack Вызывается при нажатии кнопки «Назад».
 * @param onEditClick Вызывается при нажатии кнопки редактирования счёта.
 * @param onDeleteClick Вызывается при нажатии кнопки перемещения счёта в корзину.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDetailTopBar(
    accountName: String,
    isDeleted: Boolean,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = accountName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            if (!isDeleted) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать"
                    )
                }
                IconButton(onClick = onDeleteClick) {
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

/**
 * Секция карточки баланса с датой удаления (для корзины), статусом и месячной сводкой.
 *
 * @param account Модель отображаемого счёта.
 * @param monthName Название месяца для подписей доходов и расходов.
 * @param monthlySummary Суммарные доходы и расходы по счёту за текущий месяц.
 * @param onToggleDefault Вызывается при нажатии на звёздочку статуса «по умолчанию».
 * @param modifier Модификатор контейнера карточки.
 */
@Composable
private fun AccountBalanceSection(
    account: Account,
    monthName: String,
    monthlySummary: MonthlySummary,
    onToggleDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deletedSubtitle = remember(account.isDeleted, account.deletedAt) {
        if (account.isDeleted) {
            account.deletedAt?.let { millis ->
                val localDate = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                "Удалён: ${localDate.format(DeletedAccountDateFormatter)}"
            }
        } else {
            null
        }
    }
    val resolvedMonth = monthName.ifEmpty { "месяц" }
    AccountBalanceCard(
        title = "Текущий баланс · ${account.type.title}",
        balance = account.balance,
        modifier = modifier.padding(horizontal = AccountDetailHorizontalPadding),
        subtitle = deletedSubtitle,
        trailingContent = {
            if (account.isDeleted) {
                DeletedAccountBadge()
            } else {
                DefaultAccountStarButton(
                    isDefault = account.isDefault,
                    onClick = onToggleDefault
                )
            }
        },
        incomeTitle = if (!account.isDeleted) "Доходы за $resolvedMonth" else null,
        incomeAmount = if (!account.isDeleted) monthlySummary.income else null,
        expenseTitle = if (!account.isDeleted) "Расходы за $resolvedMonth" else null,
        expenseAmount = if (!account.isDeleted) monthlySummary.expense else null
    )
}

/**
 * Бейдж «В корзине» в правом верхнем углу карточки баланса удалённого счёта.
 */
@Composable
private fun DeletedAccountBadge() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
    ) {
        Text(
            text = "В корзине",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Кнопка-звёздочка для переключения статуса счёта по умолчанию.
 *
 * @param isDefault Текущий статус счёта по умолчанию.
 * @param onClick Вызывается при нажатии на иконку.
 */
@Composable
private fun DefaultAccountStarButton(
    isDefault: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            painter = painterResource(
                if (isDefault) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            ),
            contentDescription = if (isDefault) {
                "Снять статус по умолчанию"
            } else {
                "Сделать счётом по умолчанию"
            },
            tint = if (isDefault) {
                StarGold
            } else {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            },
            modifier = Modifier.size(30.dp)
        )
    }
}

/**
 * Ряд кнопок быстрого создания операции по выбранному счёту («Расход», «Доход», «Перевод»).
 *
 * @param onActionClick Вызывается с выбранным [TransactionType].
 * @param modifier Модификатор контейнера строки.
 */
@Composable
private fun AccountQuickActionsRow(
    onActionClick: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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

/**
 * Заглушка пустого состояния, отображаемая при отсутствии операций по счёту.
 *
 * @param modifier Модификатор контейнера заглушки.
 */
@Composable
private fun AccountTransactionsEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = AccountDetailHorizontalPadding),
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

/**
 * Компактный индикатор загрузки/пагинации в ленте операций счёта.
 *
 * @param modifier Модификатор контейнера индикатора.
 */
@Composable
private fun AccountPaginationLoader(
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

/**
 * Диалог подтверждения перемещения счёта в корзину с опцией удаления связанных операций.
 *
 * @param accountName Название удаляемого счёта.
 * @param onDismiss Вызывается при отмене или закрытии диалога.
 * @param onConfirm Вызывается при подтверждении; передаёт флаг `deleteTransactions`.
 */
@Composable
private fun DeleteAccountConfirmDialog(
    accountName: String,
    onDismiss: () -> Unit,
    onConfirm: (deleteTransactions: Boolean) -> Unit
) {
    var deleteTransactions by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
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
                    text = "Счёт «$accountName» будет перемещён в корзину. Вы сможете восстановить его позже в профиле.",
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
                        verticalAlignment = Alignment.CenterVertically
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
                    onClick = onDismiss,
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
                    onClick = { onConfirm(deleteTransactions) },
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

// =========================================================================
// 3. PREVIEWS & ТЕСТОВЫЕ ДАННЫЕ
// =========================================================================
private val PreviewActiveAccount = Account(
    id = "1",
    name = "Основная карта",
    balance = 125000.0,
    currency = "RUB",
    type = AccountType.CARD,
    isDefault = true
)
private val PreviewSavingsAccount = Account(
    id = "2",
    name = "Накопительный",
    balance = 50000.0,
    currency = "RUB",
    type = AccountType.SAVINGS
)
private val PreviewDeletedAccount = Account(
    id = "3",
    name = "Старая карта",
    balance = 4200.0,
    currency = "RUB",
    type = AccountType.CARD,
    isDeleted = true,
    deletedAt = 1_760_000_000_000L
)
private val PreviewCategories = listOf(
    Category(
        id = "cat1",
        name = "Продукты",
        type = CategoryType.EXPENSE,
        iconRes = "ic_category_groceries"
    ),
    Category(
        id = "cat2",
        name = "Зарплата",
        type = CategoryType.INCOME,
        iconRes = "ic_category_salary"
    )
)
private val PreviewTransactions = listOf(
    Transaction(
        id = "tx1",
        amount = 1850.0,
        categoryId = "cat1",
        accountId = "1",
        type = TransactionType.EXPENSE,
        dateTime = System.currentTimeMillis(),
        note = "Супермаркет"
    ),
    Transaction(
        id = "tx2",
        amount = 15000.0,
        categoryId = "",
        accountId = "2",
        targetAccountId = "1",
        type = TransactionType.TRANSFER,
        dateTime = System.currentTimeMillis() - 3_600_000L,
        note = "Пополнение с накопительного"
    )
)
private val PreviewMonthlySummary = MonthlySummary(
    income = 85000.0,
    expense = 32400.0
)
@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun AccountDetailScreenLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AccountDetailScreen(
            account = PreviewActiveAccount,
            onBack = {},
            onSave = {},
            transactions = PreviewTransactions,
            categories = PreviewCategories,
            accounts = listOf(PreviewActiveAccount, PreviewSavingsAccount),
            monthName = "ноябрь",
            onGetAccountMonthlySummary = { flowOf(PreviewMonthlySummary) }
        )
    }
}
@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun AccountDetailScreenDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        AccountDetailScreen(
            account = PreviewActiveAccount.copy(isDefault = false),
            onBack = {},
            onSave = {},
            transactions = PreviewTransactions,
            categories = PreviewCategories,
            accounts = listOf(PreviewActiveAccount, PreviewSavingsAccount),
            monthName = "ноябрь",
            onGetAccountMonthlySummary = { flowOf(PreviewMonthlySummary) }
        )
    }
}
@Preview(name = "Empty State", showBackground = true)
@Composable
private fun AccountDetailScreenEmptyPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AccountDetailScreen(
            account = PreviewActiveAccount,
            onBack = {},
            onSave = {},
            transactions = emptyList(),
            categories = PreviewCategories,
            accounts = listOf(PreviewActiveAccount),
            monthName = "ноябрь"
        )
    }
}
@Preview(name = "Deleted Account", showBackground = true)
@Composable
private fun AccountDetailScreenDeletedPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AccountDetailScreen(
            account = PreviewDeletedAccount,
            onBack = {},
            onSave = {},
            transactions = emptyList(),
            categories = PreviewCategories,
            accounts = listOf(PreviewActiveAccount, PreviewDeletedAccount)
        )
    }
}