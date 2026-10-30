package com.example.financetracker.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.category.CategoryIconChip
import com.example.financetracker.core.uikit.components.category.getCategoryIcon
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint

@ExperimentalMaterial3Api
@Composable
fun QuickAddTransactionSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onAddClick: (amount: Double, categoryId: String?, type: TransactionType, note: String) -> Unit
) {
    val amountFocusRequester = remember { FocusRequester() }

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    val fallbackCategories = remember {
        listOf(
            Category(id = "1", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "shopping_cart", budgetLimit = null),
            Category(id = "2", name = "Кафе", type = CategoryType.EXPENSE, iconRes = "restaurant", budgetLimit = null),
            Category(id = "3", name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "directions_bus", budgetLimit = null),
            Category(id = "4", name = "Зарплата", type = CategoryType.INCOME, iconRes = "work", budgetLimit = null),
            Category(id = "5", name = "Переводы", type = CategoryType.EXPENSE, iconRes = "swap_horiz", budgetLimit = null)
        )
    }

    val actualCategories = if (categories.isNotEmpty()) categories else fallbackCategories

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
        onPrimaryClick = {
            submitted = true
            val chosenCategory = if (selectedType == TransactionType.INCOME) {
                selectedCategoryId
            } else {
                selectedCategoryId ?: filteredCategories.firstOrNull()?.id
            }
            val isCategoryValid = selectedType == TransactionType.INCOME || chosenCategory != null

            if (isAmountValid && isCategoryValid) {
                onAddClick(amount!!, chosenCategory, selectedType, note)
                true
            } else {
                false
            }
        },
        onDismiss = onDismiss,
        onSheetOpened = { amountFocusRequester.requestFocus() }
    ) { clearFocus ->
        // Селектор типа операции
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                TransactionType.EXPENSE to "Расход",
                TransactionType.INCOME to "Доход",
                TransactionType.TRANSFER to "Перевод"
            ).forEach { (type, label) ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = {
                        selectedType = type
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryTint,
                        selectedLabelColor = Primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedType == type,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        selectedBorderColor = Primary
                    ),
                    label = {
                        Text(
                            text = label,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Поле ввода суммы
        AmountInputField(
            value = amountText,
            onValueChange = { amountText = it },
            isError = amountError,
            errorMessage = "Введите сумму больше нуля",
            focusRequester = amountFocusRequester,
            onDone = clearFocus
        )

        // Сетка категорий
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (selectedType == TransactionType.INCOME) "Категория (необязательно)" else "Категория",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                contentPadding = PaddingValues(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

        // Поле заметки
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Заметка (необязательно)") },
            placeholder = { Text("Например, Лента или такси") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { clearFocus() }),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
