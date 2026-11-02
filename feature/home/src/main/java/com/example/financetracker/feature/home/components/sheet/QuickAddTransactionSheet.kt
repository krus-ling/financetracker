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
    onAddClick: (
        amount: Double,
        categoryId: String?,
        accountId: String, type:
        TransactionType,
        note: String,
        dateTime: Long
    ) -> Unit
) {
    val amountFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateTime,
    )
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedAccountId by remember { mutableStateOf<String?>(accounts.firstOrNull()?.id) }
    var submitted by remember { mutableStateOf(false) }

    // Резервный список счетов
    val fallbackAccounts = remember {
        listOf(
            Account(id = "1", name = "Основная карта", balance = 43965.72, currency = "RUB"),
            Account(id = "2", name = "наличные", balance = 5000.0, currency = "RUB")
        )
    }
    val actualAccounts = accounts.ifEmpty { fallbackAccounts }

    LaunchedEffect(actualAccounts) {
        if (selectedAccountId == null) {
            selectedAccountId = actualAccounts.firstOrNull()?.id
        }
    }

    val fallbackCategories = remember {
        listOf(
            Category(id = "1", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "shopping_cart", budgetLimit = null),
            Category(id = "2", name = "Кафе", type = CategoryType.EXPENSE, iconRes = "restaurant", budgetLimit = null),
            Category(id = "3", name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "directions_bus", budgetLimit = null),
            Category(id = "4", name = "Переводы", type = CategoryType.EXPENSE, iconRes = "swap_horiz", budgetLimit = null),
            Category(id = "5", name = "Дом", type = CategoryType.EXPENSE, iconRes = "home", budgetLimit = null),
            Category(id = "6", name = "Досуг", type = CategoryType.EXPENSE, iconRes = "theater_comedy", budgetLimit = null),
            Category(id = "7", name = "Здоровье", type = CategoryType.EXPENSE, iconRes = "favorite", budgetLimit = null),
            Category(id = "8", name = "Другое", type = CategoryType.EXPENSE, iconRes = "more_horiz", budgetLimit = null)
        )
    }

    val actualCategories = categories.ifEmpty { fallbackCategories }

    val filteredCategories = actualCategories.filter { category ->
        val isTransfer = category.iconRes == "swap_horiz" || category.name.contains("перевод", ignoreCase = true)
        when (selectedType) {
            TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE || isTransfer
            TransactionType.INCOME -> category.type == CategoryType.INCOME || isTransfer
            TransactionType.TRANSFER -> true
        }
    }

    LaunchedEffect(selectedType) {
        if (selectedType == TransactionType.INCOME) {
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
            val chosenCategory = if (selectedType == TransactionType.INCOME) {
                selectedCategoryId
            } else {
                selectedCategoryId ?: filteredCategories.firstOrNull()?.id
            }
            val chosenAccount = selectedAccountId ?: actualAccounts.firstOrNull()?.id
            val isCategoryValid = selectedType == TransactionType.INCOME || chosenCategory != null

            if (isAmountValid && isCategoryValid && chosenAccount != null) {
                onAddClick(
                    amount,
                    chosenCategory,
                    chosenAccount,
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

        // 3. Сетка категорий (2 ряда по 4 иконки)
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
                            selectedCategoryId = if (isSelected && selectedType == TransactionType.INCOME) {
                                null
                            } else {
                                category.id
                            }
                        }
                    )
                }
            }
        }

        // 4. Выбор счёта
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Счёт",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(actualAccounts, key = { it.id }) { account ->
                    val isSelected = account.id == selectedAccountId
                    AccountCardItem(
                        title = account.name,
                        balance = account.balance,
                        accountIcon = Icons.Default.CreditCard,
                        isSelected = isSelected,
                        containerColor = if (isSelected) PrimaryTint else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        // Управляем цветом текста/иконки: если выбрана — тёмный цвет (например, Color.Black или OnPrimaryTint), иначе стандартный цвет темы
                        contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                        onClick = { selectedAccountId = account.id }
                    )
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
            onAddClick = { _, _, _, _, _, _ -> }
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
            onAddClick = { _, _, _, _, _, _ -> }
        )
    }
}