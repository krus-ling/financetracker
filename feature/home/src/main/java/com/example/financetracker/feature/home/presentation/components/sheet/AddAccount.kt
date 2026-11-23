package com.example.financetracker.feature.home.presentation.components.sheet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.uikit.components.account.DeletedAccountCard
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.input.AppTextField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.components.transaction.AppSegmentedControl
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.util.AmountFormatter
import com.example.financetracker.core.uikit.util.toAmountOrNull
import com.example.financetracker.feature.home.presentation.components.HomeTransactionRow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.IconButton
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.account.DeletedAccountBanner
import com.example.financetracker.feature.home.presentation.components.TransactionDayGroupView
import com.example.financetracker.feature.home.util.groupTransactionByDay

@ExperimentalMaterial3Api
@Composable
fun AddAccountBottomSheet(
    onDismiss: () -> Unit,
    onAddClick: (name: String, initialBalance: Double, isDefault: Boolean) -> Unit,
    onCheckDeletedAccount: (suspend (String) -> Account?)? = null,
    onRestoreDeletedAccount: ((String) -> Unit)? = null,
    onPurgeDeletedAccount: ((String) -> Unit)? = null,
    onGetAccountTransactions: (suspend (String) -> List<Transaction>)? = null,
    categories: List<Category> = emptyList(),
    deletedAccounts: List<Account> = emptyList(),
    initialDeletedAccount: Account? = null, // для превью
    initialIsViewingAllTransactions: Boolean = false, // для превью
) {
    val coroutineScope = rememberCoroutineScope()
    val nameFocusRequester = remember { FocusRequester() }

    var accountName by rememberSaveable { mutableStateOf("") }
    var balanceText by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    // Запоминаем имя найденного в корзине счёта:
    var deletedAccountName by rememberSaveable { mutableStateOf<String?>(null) }
    var isViewingAllTransactions by rememberSaveable { mutableStateOf(initialIsViewingAllTransactions) }

    BackHandler(enabled = isViewingAllTransactions) {
        isViewingAllTransactions = false
    }

    // Найденный в корзине счёт и выбранный режим
    var deletedAccount by remember { mutableStateOf<Account?>(initialDeletedAccount) }
    var deletedTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isRestoreSelected by rememberSaveable { mutableStateOf(true) }

    val currentDeleted = deletedAccount ?: deletedAccounts.find {
        it.name.trim().equals(deletedAccountName?.trim(), ignoreCase = true)
    }

    val isNameValid = accountName.trim().isNotEmpty()
    val nameError = submitted && !isNameValid

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    var isDefault by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(deletedAccountName) {
        val name = deletedAccountName
        if (name != null && deletedAccount == null) {
            val found = onCheckDeletedAccount?.invoke(name)
            if (found != null) {
                deletedAccount = found
                deletedTransactions = onGetAccountTransactions?.invoke(found.id) ?: emptyList()
            }
        }
    }

    LaunchedEffect(currentDeleted?.id) {
        val id = currentDeleted?.id
        if (id != null && deletedTransactions.isEmpty()) {
            deletedTransactions = onGetAccountTransactions?.invoke(id) ?: emptyList()
        }
    }

    AppFormBottomSheet(
        title = when {
            isViewingAllTransactions -> "Все операции"
            currentDeleted != null -> "Кажется, такой счёт уже был 🤔"
            else -> "Новый счёт"
        },
        titleLeadingContent = if (isViewingAllTransactions) {
            {
                IconButton(
                    onClick = { isViewingAllTransactions = false },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        titleTrailingContent = null,
        containerColor = if (isViewingAllTransactions) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surface
        },
        primaryButtonText = when {
            isViewingAllTransactions -> "Назад к счёту"
            currentDeleted == null -> "Создать счёт"
            else -> "Готово"
        },
        cancelButtonText = if (isViewingAllTransactions) null else "Отмена",
        isPrimaryEnabled = when {
            isViewingAllTransactions -> true
            currentDeleted != null && !isRestoreSelected -> isNameValid
            else -> true
        },
        onPrimaryClick = {
            if (isViewingAllTransactions) {
                isViewingAllTransactions = false
                false // не закрываем шторку, просто переключаем режим
            } else if (currentDeleted  == null) {
                // Обычный режим: проверяем имя в корзине
                submitted = true
                if (isNameValid) {
                    val balance = balanceText.toAmountOrNull() ?: 0.0
                    val trimmedName = accountName.trim()

                    coroutineScope.launch {
                        val found = onCheckDeletedAccount?.invoke(trimmedName)
                        if (found != null) {
                            // Нашли в корзине - показываем диалог
                            val txs = onGetAccountTransactions?.invoke(found.id) ?: emptyList()
                            deletedTransactions = txs
                            deletedAccount = found
                            deletedAccountName = found.name
                            isRestoreSelected = true
                        } else {
                            // Не нашли - создаем счет как обычно
                            onAddClick(trimmedName, balance, isDefault)
                            onDismiss()
                        }
                    }
                }
                false
            } else {
                // Режим найденного в корзине счёта: обрабатываем выбор
                if (isRestoreSelected) {
                    onRestoreDeletedAccount?.invoke(currentDeleted.id)
                } else {
                    val balance = balanceText.toAmountOrNull() ?: 0.0
                    onPurgeDeletedAccount?.invoke(currentDeleted.id)
                    onAddClick(accountName.trim(), balance, isDefault)
                }
                deletedAccount = null
                true // плавно закрываем шторку
            }
        },
        onDismiss = {
            if (isViewingAllTransactions) {
                // Если мы в режиме «Все операции», системный «Назад»
                // просто возвращает нас к выбору дежавю, не закрывая шторку:
                isViewingAllTransactions = false
            } else {
                // Если мы на первом шаге — закрываем шторку и сбрасываем поля:
                accountName = ""
                balanceText = ""
                submitted = false
                isDefault = false
                deletedAccount = null
                deletedAccountName = null
                deletedTransactions = emptyList()
                isRestoreSelected = true
                onDismiss()
            }
        },
        onSheetOpened = {
            if (currentDeleted == null) nameFocusRequester.requestFocus()
        },
        modifier = if (currentDeleted != null) Modifier.fillMaxHeight() else Modifier,
    ) { clearFocus ->

        if (isViewingAllTransactions && currentDeleted != null) {

            // Карточка счета
            DeletedAccountCard(
                account = currentDeleted,
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "В корзине",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            )

            // Инфо баннер корзины
            DeletedAccountBanner()

            // Заголовок ленты
            Text(
                text = "Операции",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            val groupedDays = remember(deletedTransactions) {
                groupTransactionByDay(deletedTransactions)
            }

            // Группировка операций по дням
            if (deletedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "По этому счёту пока нет операций",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                groupedDays.forEach { dayGroup ->
                    TransactionDayGroupView(
                        dayGroup = dayGroup,
                        categoriesMap = categoriesMap,
                        account = currentDeleted,
                        headerHorizontalPadding = 0.dp
                    )
                }
            }
        } else {
            // 1. Чипы переключения (только если счёт найден в корзине)
            if (currentDeleted != null) {
                AppSegmentedControl(
                    items = remember { listOf(true, false) },
                    selectedItem = isRestoreSelected,
                    onItemSelected = { isRestoreSelected = it },
                    label = { if (it) "Восстановить старый" else "Создать новый" }
                )
            }

            when {
                // РЕЖИМ 1: Восстановление счёта из корзины
                currentDeleted != null && isRestoreSelected -> {
                    // Заблокированное название
                    AppTextField(
                        value = currentDeleted.name,
                        onValueChange = {},
                        label = "Название счёта",
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Заблокировано",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    // Заблокированный баланс из корзины
                    AmountInputField(
                        value = AmountFormatter.formatForInput(currentDeleted.balance),
                        onValueChange = {},
                        label = "Баланс из корзины",
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Заблокировано",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // История операций
                    Text(
                        text = if (deletedTransactions.isNotEmpty()) {
                            "Последние операции:"
                        } else {
                            "Операций по счёту не найдено"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (deletedTransactions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column {
                                deletedTransactions.take(5).forEach { tx ->
                                    val cat = categoriesMap[tx.categoryId]
                                    HomeTransactionRow(
                                        transaction = tx,
                                        category = cat,
                                        account = currentDeleted,
                                        onClick = {}
                                    )
                                }

                                if (deletedTransactions.isNotEmpty()) {

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(
                                            alpha = 0.4f
                                        )
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                isViewingAllTransactions = true
                                            }
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Все операции (${deletedTransactions.size})",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(Modifier.width(4.dp))

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // РЕЖИМ 2: Обычное создание или ввод нового счёта
                else -> {
                    AppTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = "Название счёта",
                        placeholder = "Например, Наличные",
                        isError = nameError,
                        errorMessage = "Введите название счёта",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        focusRequester = nameFocusRequester
                    )

                    AmountInputField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = "Начальный баланс",
                        onDone = clearFocus
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDefault = !isDefault }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Счёт по умолчанию",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Будет автоматически выбираться для новых операций",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Switch(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                            )
                        )
                    }

                    // Если перезаписываем старый счёт — предупреждаем пользователя
                    if (currentDeleted != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Старый счёт «${currentDeleted.name}» будет безвозвратно удалён из корзины",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ТЕСТОВЫЕ ДАННЫЕ ДЛЯ ПРЕВЬЮ ---

private val previewCategories = listOf(
    Category(id = "cat1", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "ic_category_groceries"),
    Category(id = "cat2", name = "Одежда", type = CategoryType.EXPENSE, iconRes = "ic_category_clothing"),
    Category(id = "cat3", name = "Зарплата", type = CategoryType.INCOME, iconRes = "ic_category_salary")
)

private val previewDeletedAccount = Account(
    id = "del_1",
    name = "наличка",
    balance = 4000.0,
    type = AccountType.CASH,
    isDeleted = true,
    deletedAt = System.currentTimeMillis() - 86400000L * 3
)

private val previewTransactions = listOf(
    Transaction(
        id = "tx1",
        amount = 500.0,
        type = TransactionType.EXPENSE,
        categoryId = "cat2",
        accountId = "del_1",
        dateTime = System.currentTimeMillis(),
        note = "Футболка"
    ),
    Transaction(
        id = "tx2",
        amount = 500.0,
        type = TransactionType.EXPENSE,
        categoryId = "cat1",
        accountId = "del_1",
        dateTime = System.currentTimeMillis() - 86400000L,
        note = "Магазин"
    )
)

// 1. Обычное создание нового счёта
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "1. Новый счёт", showBackground = true)
@Composable
private fun AddAccountNewPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountBottomSheet(
            onDismiss = {},
            onAddClick = { _, _, _ -> }
        )
    }
}

// 2. Режим Дежавю (найден счёт в корзине)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "2. Дежавю (Счёт в корзине)", showBackground = true)
@Composable
private fun AddAccountDejaVuPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountBottomSheet(
            onDismiss = {},
            onAddClick = { _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            categories = previewCategories,
            onGetAccountTransactions = { previewTransactions }
        )
    }
}

// 3. Режим «Все операции» (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "3. Все операции - Светлая", showBackground = true)
@Composable
private fun AddAccountAllTransactionsPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountBottomSheet(
            onDismiss = {},
            onAddClick = { _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            initialIsViewingAllTransactions = true,
            categories = previewCategories,
            onGetAccountTransactions = { previewTransactions }
        )
    }
}

// 4. Режим «Все операции» (Тёмная тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "4. Все операции - Тёмная", showBackground = true)
@Composable
private fun AddAccountAllTransactionsDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        AddAccountBottomSheet(
            onDismiss = {},
            onAddClick = { _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            initialIsViewingAllTransactions = true,
            categories = previewCategories,
            onGetAccountTransactions = { previewTransactions }
        )
    }
}