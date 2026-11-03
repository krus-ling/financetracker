package com.example.financetracker.feature.home.components.sheet


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.account.AccountCardItem
import com.example.financetracker.core.uikit.components.account.AddAccountCardItem
import com.example.financetracker.core.uikit.components.category.CategoryIconChip
import com.example.financetracker.core.uikit.components.category.getCategoryIcon
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.input.DateTimePickerRow
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.components.transaction.TransactionTypeSelector
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.PrimaryTint
import java.util.Calendar

@ExperimentalMaterial3Api
@Composable
fun QuickAddTransactionSheet(
    categories: List<Category>,
    accounts: List<Account> = emptyList(),
    onDismiss: () -> Unit,
    onAddNewAccount: () -> Unit,
    onAddClick: (
        amount: Double,
        categoryId: String?,
        accountId: String,
        targetAccountId: String?,
        type: TransactionType,
        note: String,
        dateTime: Long
    ) -> Unit
) {
    val amountFocusRequester = remember { FocusRequester() }

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateTime,
    )
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    var selectedAccountId by remember { mutableStateOf<String?>(accounts.firstOrNull()?.id) }
    var selectedTargetAccountId by remember { mutableStateOf<String?>(accounts.getOrNull(1)?.id) }

    // Список счетов для зачисления: исключая счет выбранный в "откуда"
    val targetAccounts = remember(accounts, selectedAccountId) {
        accounts.filter { it.id != selectedAccountId }
    }

    // Инициализация при изменении списка счетов
    LaunchedEffect(accounts) {
        if (selectedAccountId == null || accounts.none { it.id == selectedAccountId }) {
            selectedAccountId = accounts.firstOrNull()?.id
        }
        if (selectedTargetAccountId == null || targetAccounts.none { it.id == selectedTargetAccountId }) {
            selectedTargetAccountId = targetAccounts.firstOrNull()?.id
        }
    }

    // Если изменился счёт списания и совпал с целевым — сбрасываем целевой на первый доступный из отфильтрованных
    LaunchedEffect(selectedAccountId) {
        if (selectedTargetAccountId == selectedAccountId || targetAccounts.none { it.id == selectedTargetAccountId }) {
            selectedTargetAccountId = targetAccounts.firstOrNull()?.id
        }
    }

    val filteredCategories = categories.filter { category ->
        val isTransfer = category.iconRes == "swap_horiz" || category.name.contains("перевод", ignoreCase = true)
        when (selectedType) {
            TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE || isTransfer
            TransactionType.INCOME -> category.type == CategoryType.INCOME || isTransfer
            TransactionType.TRANSFER -> true
        }
    }

    LaunchedEffect(categories, selectedType) {
        if (selectedType == TransactionType.INCOME || selectedType == TransactionType.TRANSFER) {
            selectedCategoryId = null
        } else {
            if (filteredCategories.none { it.id == selectedCategoryId }) {
                selectedCategoryId = filteredCategories.firstOrNull()?.id
            }
        }
    }

    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val isAmountValid = amount != null && amount.isFinite() && amount > 0.0
    val amountError = submitted && !isAmountValid

    AppFormBottomSheet(
        title = "Новая операция",
        primaryButtonText = "Добавить",
        cancelButtonText = "Назад",
        onPrimaryClick = {
            submitted = true
            val chosenCategory = when (selectedType) {
                TransactionType.TRANSFER -> null
                TransactionType.INCOME -> selectedCategoryId
                TransactionType.EXPENSE -> selectedCategoryId ?: filteredCategories.firstOrNull()?.id
            }
            val chosenAccount = selectedAccountId ?: accounts.firstOrNull()?.id
            val chosenTargetAccount = if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else null
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
                true
            } else {
                false
            }
        },
        onDismiss = onDismiss,
        onSheetOpened = { amountFocusRequester.requestFocus() }
    ) { clearFocus ->
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
            onDone = { clearFocus() }
        )

        // 3. Блок категорий ИЛИ Счёт списания (Откуда)
        if (selectedType == TransactionType.TRANSFER && accounts.isNotEmpty()) {
            // Если перевод и есть хотя бы 1 счёт — показываем «Откуда»
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Откуда",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(accounts, key = { it.id }) { account ->
                        val isSelected = account.id == selectedAccountId
                        AccountCardItem(
                            title = account.name,
                            balance = account.balance,
                            accountIcon = Icons.Default.CreditCard,
                            isSelected = isSelected,
                            containerColor = if (isSelected) PrimaryTint else MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            ),
                            contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                            onClick = { selectedAccountId = account.id }
                        )
                    }
                }
            }
        } else if (selectedType != TransactionType.TRANSFER) {
            // Для Расхода и Дохода — стандартная сетка категорий
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (selectedType == TransactionType.INCOME) "Категория (необязательно)" else "Категория",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(filteredCategories, key = { it.id }) { category ->
                        val isSelected = category.id == selectedCategoryId
                        CategoryIconChip(
                            icon = getCategoryIcon(category.iconRes),
                            title = category.name,
                            isSelected = isSelected,
                            onClick = {
                                selectedCategoryId =
                                    if (isSelected && selectedType == TransactionType.INCOME) {
                                        null
                                    } else {
                                        category.id
                                    }
                            }
                        )
                    }
                }
            }
        }

        // 4. Блок выбора счёта: «Куда» (для перевода) или «Счёт» (для остальных)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val title = when {
                accounts.isEmpty() -> "Счёт"
                selectedType == TransactionType.TRANSFER -> "Куда"
                else -> "Счёт"
            }

            Text(
                text = if (selectedType == TransactionType.TRANSFER) "Куда" else "Счёт",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (accounts.isEmpty()) {
                // Вообще нет счетов -> предлагаем создать первый счёт
                LazyRow {
                    item {
                        AddAccountCardItem(
                            title = "Создать счёт",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            onClick = onAddNewAccount
                        )
                    }
                }
            } else if (selectedType == TransactionType.TRANSFER && targetAccounts.isEmpty()) {
                // Перевод, но второго счёта нет -> предлагаем создать второй счёт
                LazyRow {
                    item {
                        AddAccountCardItem(
                            title = "Добавить счёт",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            onClick = onAddNewAccount
                        )
                    }
                }
            } else {
                // Стандартный список счетов
                val displayedAccounts =
                    if (selectedType == TransactionType.TRANSFER) targetAccounts else accounts

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedAccounts, key = { it.id }) { account ->
                        val isSelected = if (selectedType == TransactionType.TRANSFER) {
                            account.id == selectedTargetAccountId
                        } else {
                            account.id == selectedAccountId
                        }
                        AccountCardItem(
                            title = account.name,
                            balance = account.balance,
                            accountIcon = Icons.Default.CreditCard,
                            isSelected = isSelected,
                            containerColor = if (isSelected) PrimaryTint else MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            ),
                            // Управляем цветом текста/иконки: если выбрана — тёмный цвет (например, Color.Black или OnPrimaryTint), иначе стандартный цвет темы
                            contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                if (selectedType == TransactionType.TRANSFER) {
                                    selectedTargetAccountId = account.id
                                } else {
                                    selectedAccountId = account.id
                                }
                            }
                        )
                    }
                    item {
                        AddAccountCardItem(
                            title = "Добавить счёт",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            onClick = onAddNewAccount
                        )
                    }
                }
            }
        }

        // 5. Дата и время
        DateTimePickerRow(
            dateTime = selectedDateTime,
            onClick = { showDatePicker = true }
        )

        // 6. Поле заметки
        OutlinedTextField(
            value = note,
            onValueChange = { if (it.length <= 100) note = it },
            label = { Text("Заметка (необязательно)") },
            placeholder = { Text("Например, Лента или такси") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { clearFocus() }
            ),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickerDateMills ->
                            // Сохраняем выбранную дату, но оставляем текущее время (часы и минуты)
                            val calendarPicked = Calendar.getInstance().apply { timeInMillis = pickerDateMills }
                            val currentCalendar = Calendar.getInstance().apply { timeInMillis = selectedDateTime }

                            currentCalendar.set(Calendar.YEAR, calendarPicked.get(Calendar.YEAR))
                            currentCalendar.set(Calendar.MONTH, calendarPicked.get(Calendar.MONTH))
                            currentCalendar.set(Calendar.DAY_OF_MONTH, calendarPicked.get(Calendar.DAY_OF_MONTH))

                            selectedDateTime = currentCalendar.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Quick Add Sheet - Light", showBackground = true)
@Composable
private fun QuickAddTransactionSheetLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = emptyList(),
            accounts = emptyList(),
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Quick Add Sheet - Dark",
    showBackground = true,
    backgroundColor = 0xFF121816
)
@Composable
private fun QuickAddTransactionSheetDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        QuickAddTransactionSheet(
            categories = emptyList(),
            accounts = emptyList(),
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}