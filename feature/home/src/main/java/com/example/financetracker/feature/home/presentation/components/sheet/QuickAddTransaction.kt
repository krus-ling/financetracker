package com.example.financetracker.feature.home.presentation.components.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.account.AccountCardItem
import com.example.financetracker.core.uikit.components.account.AddAccountCardItem
import com.example.financetracker.core.uikit.components.account.getIconRes
import com.example.financetracker.core.uikit.components.button.AppMiniActionButton
import com.example.financetracker.core.uikit.components.category.CategoryIconChip
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.input.AppTextField
import com.example.financetracker.core.uikit.components.input.DateTimePickerRow
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.components.transaction.TransactionTypeSelector
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint
import com.example.financetracker.core.uikit.util.rememberHalfWidth
import com.example.financetracker.core.uikit.util.toAmountOrNull
import com.example.financetracker.feature.home.domain.model.ParsedTransactionDraft
import com.example.financetracker.feature.home.presentation.components.fastinput.FastInputSection
import com.example.financetracker.feature.home.presentation.components.fastinput.FastInputTriggerChip
import java.util.Calendar

@ExperimentalMaterial3Api
@Composable
fun QuickAddTransactionSheet(
    categories: List<Category>,
    accounts: List<Account> = emptyList(),
    onDismiss: () -> Unit,
    onAddNewAccount: () -> Unit,
    initialType: TransactionType = TransactionType.EXPENSE,
    initialAccountId: String? = null,
    onAddClick: (
        amount: Double,
        categoryId: String?,
        accountId: String,
        targetAccountId: String?,
        type: TransactionType,
        note: String,
        dateTime: Long
    ) -> Unit,
    learnedCategoryKeywords: Map<String, String> = emptyMap(),
    learnedAccountAliases: Map<String, String> = emptyMap(),
    onLearnFromFastInput: (
        rawText: String,
        categoryId: String?,
        accountId: String?,
        wasCategoryChanged: Boolean,
        wasAccountChanged: Boolean
    ) -> Unit = { _, _, _, _, _ -> },
    onParseFastInput: (suspend (text: String, defaultAccountId: String?) -> ParsedTransactionDraft)? = null,
) {
    val amountFocusRequester = remember { FocusRequester() }

    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateTime,
    )
    var selectedExpenseCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedIncomeCategoryId by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    var selectedAccountId by remember(accounts, initialAccountId) {
        mutableStateOf(initialAccountId ?: accounts.find { it.isDefault }?.id ?:
        accounts.firstOrNull()?.id)
    }
    var selectedTargetAccountId by remember { mutableStateOf<String?>(accounts.getOrNull(1)?.id) }

    // Раздельные состояния скролла для обычного счёта и целевого счёта перевода
    val accountListState = rememberLazyListState()
    val targetAccountListState = rememberLazyListState()

    // Список счетов для зачисления: исключая счет выбранный в "откуда"
    val targetAccounts = remember(accounts, selectedAccountId) {
        accounts.filter { it.id != selectedAccountId }
    }

    val halfWidth = rememberHalfWidth(horizontalPadding = 20.dp, spacing = 10.dp)

    // Инициализация при изменении списка счетов
    LaunchedEffect(accounts) {
        if (selectedAccountId == null || accounts.none { it.id == selectedAccountId }) {
            selectedAccountId = accounts.firstOrNull()?.id
        }
        if (selectedTargetAccountId == null || targetAccounts.none { it.id == selectedTargetAccountId }) {
            selectedTargetAccountId = targetAccounts.firstOrNull()?.id
        }
    }

    val filteredCategories = remember (categories, selectedType) {
        when (selectedType) {
            TransactionType.EXPENSE -> categories.filter { it.type == CategoryType.EXPENSE }
            TransactionType.INCOME -> categories.filter { it.type == CategoryType.INCOME }
            TransactionType.TRANSFER -> emptyList()
        }
    }

    LaunchedEffect(categories) {
        val expenseList = categories.filter { it.type == CategoryType.EXPENSE }
        if (selectedExpenseCategoryId == null || expenseList.none { it.id == selectedExpenseCategoryId }) {
            selectedExpenseCategoryId = expenseList.firstOrNull()?.id
        }
    }

    val amount = amountText.toAmountOrNull()
    val isAmountValid = amount != null && amount.isFinite() && amount > 0.0
    val amountError = submitted && !isAmountValid

    var isNoteFocused by remember { mutableStateOf(false) }
    var isFastInputExpanded by rememberSaveable { mutableStateOf(false) }
    var lastFastInputText by remember { mutableStateOf("") }
    var userChangedCategory by remember { mutableStateOf(false) }
    var userChangedAccount by remember { mutableStateOf(false) }

    val trySubmitTransaction: () -> Boolean = {
        submitted = true
        val chosenCategory = when (selectedType) {
            TransactionType.TRANSFER -> null
            TransactionType.INCOME -> selectedIncomeCategoryId
            TransactionType.EXPENSE -> selectedExpenseCategoryId
                ?: filteredCategories.firstOrNull()?.id
        }
        val chosenAccount = selectedAccountId ?: accounts.firstOrNull()?.id
        val chosenTargetAccount =
            if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else null
        val isCategoryValid = selectedType == TransactionType.INCOME ||
                selectedType == TransactionType.TRANSFER ||
                chosenCategory != null
        val isTransferValid = selectedType != TransactionType.TRANSFER ||
                (chosenAccount != null && chosenTargetAccount != null && chosenAccount != chosenTargetAccount)

        if (isAmountValid && isCategoryValid && chosenAccount != null && isTransferValid) {
            onAddClick(
                amount,
                chosenCategory,
                chosenAccount,
                chosenTargetAccount,
                selectedType,
                note,
                selectedDateTime
            )
            if (lastFastInputText.isNotBlank()) {
                onLearnFromFastInput(
                    lastFastInputText,
                    chosenCategory,
                    chosenAccount,
                    userChangedCategory,
                    userChangedAccount
                )
            }
            true
        } else {
            false
        }
    }

    AppFormBottomSheet(
        title = "Новая операция",
        titleTrailingContent = {
            AnimatedVisibility(
                visible = !isFastInputExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FastInputTriggerChip(onClick = { isFastInputExpanded = true })
            }
        },
        primaryButtonText = "Добавить",
        cancelButtonText = "Назад",
        onPrimaryClick = trySubmitTransaction,
        onDismiss = onDismiss,
        onSheetOpened = { amountFocusRequester.requestFocus() },
        modifier = Modifier.fillMaxHeight()
    ) { clearFocus ->

        // 0. Быстрый ввод
        FastInputSection(
            isExpanded = isFastInputExpanded,
            onExpandedChange = { isFastInputExpanded = it },
            categories = categories,
            accounts = accounts,
            defaultAccountId = selectedAccountId ?: accounts.firstOrNull()?.id,
            onDraftParsed = { draft ->
                lastFastInputText = draft.rawText
                userChangedCategory = false
                userChangedAccount = false
                if (draft.rawText.isNotBlank()) {
                    draft.amount?.let { amt ->
                        amountText = if (amt % 1.0 == 0.0) amt.toLong().toString() else amt.toString()
                    }
                    selectedType = draft.type
                    if (draft.type == TransactionType.INCOME) {
                        selectedIncomeCategoryId = draft.categoryId
                    } else {
                        draft.categoryId?.let { selectedExpenseCategoryId = it }
                    }
                    draft.accountId?.let { selectedAccountId = it }
                    draft.date?.let { selectedDateTime = it }
                    note = draft.note
                }
            },
            learnedCategoryKeywords = learnedCategoryKeywords,
            learnedAccountAliases = learnedAccountAliases,
            onParseFastInput = onParseFastInput,
        )

        // 1. Селектор типа операции
        TransactionTypeSelector(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it }
        )

        // 2. Поле ввода суммы
        AmountInputField(
            value = amountText,
            onValueChange = { amountText = it },
            isError = amountError,
            errorMessage = "Введите сумму больше нуля",
            focusRequester = amountFocusRequester,
            onDone = { clearFocus() },
            trailingIcon = {
                AnimatedVisibility(
                    visible = isAmountValid,
                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
                ) {
                    AppMiniActionButton(
                        onClick = {
                            if (trySubmitTransaction()) {
                                clearFocus()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        )

        // 3. Блок категорий ИЛИ Счёт списания (Откуда)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedType == TransactionType.TRANSFER && accounts.isNotEmpty()) {
                // Если перевод и есть хотя бы 1 счёт — показываем «Откуда»
                AccountSelectorSection(
                    title = "Откуда",
                    accounts = accounts,
                    selectedAccountId = selectedAccountId,
                    onAccountSelected = { newAccountId ->
                        selectedAccountId = newAccountId
                        if (selectedTargetAccountId == newAccountId) {
                            selectedTargetAccountId = accounts.firstOrNull { it.id != newAccountId }?.id
                        }
                    },
                    onAddNewAccount = onAddNewAccount,
                    showAddAccountButton = false
                )
            } else if (selectedType != TransactionType.TRANSFER) {
                // Для Расхода и Дохода — динамическая адаптивная сетка категорий
                CategoryGridSection(
                    categories = filteredCategories,
                    selectedCategoryId = if (selectedType == TransactionType.INCOME) {
                        selectedIncomeCategoryId
                    } else selectedExpenseCategoryId,
                    isIncome = selectedType == TransactionType.INCOME,
                    onCategoryClick = { categoryId ->
                        userChangedCategory = true
                        if (selectedType == TransactionType.INCOME) {
                            selectedIncomeCategoryId = categoryId
                        } else {
                            selectedExpenseCategoryId = categoryId
                        }
                    }
                )
            }
        }

        // 4. Блок выбора счёта: «Куда» (для перевода) или «Счёт» (для остальных)
        AccountSelectorSection(
            title = if (selectedType == TransactionType.TRANSFER) "Куда" else "Счёт",
            accounts = if (selectedType == TransactionType.TRANSFER) targetAccounts else accounts,
            selectedAccountId = if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else selectedAccountId,
            onAccountSelected = { id ->
                userChangedAccount = true
                if (selectedType == TransactionType.TRANSFER) {
                    selectedTargetAccountId = id
                } else {
                    selectedAccountId = id
                }
            },
            onAddNewAccount = onAddNewAccount,
            lazyListState = if (selectedType == TransactionType.TRANSFER) targetAccountListState else accountListState
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (!isNoteFocused) 10.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 5. Дата
            AnimatedVisibility(
                visible = !isNoteFocused,
                enter = expandHorizontally(animationSpec = tween(200)) +
                        fadeIn(animationSpec = tween(200)),
                exit = shrinkHorizontally(animationSpec = tween(200)) +
                        fadeOut(animationSpec = tween(50))
            ) {
                DateTimePickerRow(
                    dateTime = selectedDateTime,
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .width(halfWidth)
                        .padding(top = 8.dp)
                )
            }

            // 6. Поле заметки
            AppTextField(
                value = note,
                onValueChange = { if (it.length <= 100) note = it },
                label = "Заметка",
                placeholder = "Необязательно",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { clearFocus() }),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isNoteFocused = it.isFocused }
            )
        }
    }

    if (showDatePicker) {
        TransactionDatePickerDialog(
            datePickerState = datePickerState,
            currentDateTime = selectedDateTime,
            onDateSelected = { selectedDateTime = it },
            onDismiss = { showDatePicker = false }
        )
    }
}

// Сетка категорий
@Composable
private fun CategoryGridSection(
    categories: List<Category>,
    selectedCategoryId: String?,
    isIncome: Boolean,
    onCategoryClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isIncome) "Категория (необязательно)" else "Категория",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Разбиваем категории на страницы ровно по 8 штук (2 ряда x 4 колонки)
        val pages = remember(categories) {
            categories.chunked(8)
        }

        if (pages.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { pages.size })

            // Автоматический скролл к странице, где находится выбранная категория
            val selectedPageIndex = remember(categories, selectedCategoryId) {
                val index = categories.indexOfFirst { it.id == selectedCategoryId }
                if (index >= 0) index / 8 else 0
            }

            LaunchedEffect(selectedPageIndex) {
                if (selectedPageIndex in pages.indices && pagerState.currentPage != selectedPageIndex) {
                    pagerState.animateScrollToPage(selectedPageIndex)
                }
            }

            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                val pageCategories = pages[pageIndex]

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Всегда ровно 2 ряда
                    for (rowIndex in 0..1) {
                        val rowCategories = pageCategories.drop(rowIndex * 4).take(4)

                        if (rowCategories.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    val isSelected = category.id == selectedCategoryId
                                    Box(modifier = Modifier.weight(1f)) {
                                        CategoryIconChip(
                                            iconRes = category.iconRes,
                                            title = category.name,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (isIncome) {
                                                    onCategoryClick(if (isSelected) null else category.id)
                                                } else {
                                                    onCategoryClick(category.id)
                                                }
                                            }
                                        )
                                    }
                                }
                                repeat(4 - rowCategories.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        } else {
                            // Если второго ряда нет — рендерим невидимый ряд с такой же высотой
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .alpha(0f)
                                ) {
                                    CategoryIconChip(
                                        iconRes = null,
                                        title = " ",
                                        isSelected = false,
                                        onClick = {}
                                    )
                                }
                                repeat(3) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Точки-индикаторы страниц (показываются только если страниц > 1)
            if (pages.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { iteration ->
                        val isCurrentPage = pagerState.currentPage == iteration
                        val color = if (isCurrentPage) {
                            Primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                        val width = if (isCurrentPage) 16.dp else 6.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(6.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}

// Универсальная карусель счетов
@Composable
private fun AccountSelectorSection(
    title: String,
    accounts: List<Account>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    onAddNewAccount: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    showAddAccountButton: Boolean = true
) {
    val cardSpacing = 10.dp
    val cardWidth = rememberHalfWidth(horizontalPadding = 20.dp, spacing = cardSpacing)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (accounts.isEmpty()) {
            LazyRow {
                item {
                    AddAccountCardItem(
                        title = if (title == "Счёт") "Создать счёт" else "Добавить счёт",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.width(cardWidth),
                        onClick = onAddNewAccount
                    )
                }
            }
        } else {
            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                items(accounts, key = { it.id }) { account ->
                    val isSelected = account.id == selectedAccountId
                    AccountCardItem(
                        title = account.name,
                        balance = account.balance,
                        accountIcon = account.type.getIconRes(),
                        isSelected = isSelected,
                        isDefault = account.isDefault,
                        containerColor = if (isSelected) PrimaryTint else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(cardWidth),
                        onClick = { onAccountSelected(account.id) }
                    )
                }
                if (showAddAccountButton) {
                    item {
                        AddAccountCardItem(
                            title = "Добавить счёт",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.width(cardWidth),
                            onClick = onAddNewAccount
                        )
                    }
                }
            }
        }
    }
}

// Диалог даты
@Composable
private fun TransactionDatePickerDialog(
    datePickerState: DatePickerState,
    currentDateTime: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { pickerDateMills ->
                        val calendarPicked = Calendar.getInstance().apply { timeInMillis = pickerDateMills }
                        val currentCalendar = Calendar.getInstance().apply { timeInMillis = currentDateTime }

                        currentCalendar.set(Calendar.YEAR, calendarPicked.get(Calendar.YEAR))
                        currentCalendar.set(Calendar.MONTH, calendarPicked.get(Calendar.MONTH))
                        currentCalendar.set(Calendar.DAY_OF_MONTH, calendarPicked.get(Calendar.DAY_OF_MONTH))

                        onDateSelected(currentCalendar.timeInMillis)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// --- ТЕСТОВЫЕ ДАННЫЕ ДЛЯ ПРЕВЬЮ ---
private val previewAccounts = listOf(
    Account(id = "1", name = "Основная карта", balance = 86700.0, isDefault = true),
    Account(id = "2", name = "Наличные", balance = 12500.0),
    Account(id = "3", name = "Накопления", balance = 250000.0)
)
private val previewCategories = listOf(
    Category(id = "cat1", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "ic_category_groceries"),
    Category(id = "cat2", name = "Кафе", type = CategoryType.EXPENSE, iconRes = "ic_category_cafe"),
    Category(id = "cat3", name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "ic_category_transport"),
    Category(id = "cat4", name = "Одежда", type = CategoryType.EXPENSE, iconRes = "ic_category_clothing"),
    Category(id = "cat5", name = "Зарплата", type = CategoryType.INCOME, iconRes = "ic_category_salary"),
    Category(id = "cat6", name = "Фриланс", type = CategoryType.INCOME, iconRes = "ic_category_freelance"),
    Category(id = "cat7", name = "Pljhjdmt", type = CategoryType.EXPENSE, iconRes = "ic_category_health")
)
// 1. Режим «Расход» (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "1. Расход - Светлая", showBackground = true)
@Composable
private fun QuickAddExpenseLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = previewAccounts,
            initialType = TransactionType.EXPENSE,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}
// 2. Режим «Доход» (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "2. Доход - Светлая", showBackground = true)
@Composable
private fun QuickAddIncomeLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = previewAccounts,
            initialType = TransactionType.INCOME,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}
// 3. Режим «Перевод между счетами» (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "3. Перевод - Светлая", showBackground = true)
@Composable
private fun QuickAddTransferLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = previewAccounts,
            initialType = TransactionType.TRANSFER,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}
// 4. Режим «Расход» (Тёмная тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "4. Расход - Тёмная", showBackground = true)
@Composable
private fun QuickAddExpenseDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = previewAccounts,
            initialType = TransactionType.EXPENSE,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}