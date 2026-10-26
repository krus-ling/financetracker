package com.example.financetracker.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.component.CategoryIcon
import com.example.financetracker.core.uikit.component.getCategoryIcon
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTransactionSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onAddClick: (amount: Double, categoryId: String, type: TransactionType, note: String) -> Unit
) {
    // Принудительно пропускаем промежуточное состояние
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    // Дефолтный список, если база ещё не успела отдать категории
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

    // Фильтруем категории под выбранный тип операции (расход/доход)
    val filteredCategories = actualCategories.filter { category ->
        when (selectedType) {
            TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE
            TransactionType.INCOME -> category.type == CategoryType.INCOME
            TransactionType.TRANSFER -> true
        }
    }

    // Автовыбор первой доступной категории при переключении типа
    LaunchedEffect(selectedType, filteredCategories) {
        if (filteredCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = filteredCategories.firstOrNull()?.id
        }
    }

    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val amountError = submitted && (amount == null || !amount.isFinite() || amount <= 0.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Новая операция",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // 1. Переключатель типа операции
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
                        onClick = { selectedType = type },
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

            // 2. Ввод суммы
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                label = { Text("Сумма") },
                placeholder = { Text("0,00 ₽") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                supportingText = if (amountError) ({ Text("Введите сумму больше нуля") }) else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Выбор категории иконками
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Категория",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { category ->
                        val isSelected = category.id == selectedCategoryId
                        val iconVector = getCategoryIcon(category.iconRes)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategoryId = category.id }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                                        } else Modifier
                                    )
                                    .padding(4.dp)
                            ) {
                                CategoryIcon(
                                    icon = iconVector,
                                    backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else PrimaryTint,
                                    iconTint = if (isSelected) MaterialTheme.colorScheme.primary else Primary,
                                    size = 40.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 4. Поле описания / заметки
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (необязательно)") },
                placeholder = { Text("Например, Лента или такси") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Кнопки сохранения и отмены
            Button(
                onClick = {
                    submitted = true
                    val validAmount = amount != null && amount.isFinite() && amount > 0.0
                    val chosenCategory = selectedCategoryId ?: filteredCategories.firstOrNull()?.id

                    if (validAmount && chosenCategory != null) {
                        onAddClick(amount!!, chosenCategory, selectedType, note)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить")
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickAddTransactionSheetPreview() {
    FinancetrackerTheme {
        QuickAddTransactionSheet(
            categories = listOf(
                Category(id = "1", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "shopping_cart", budgetLimit = null),
                Category(id = "2", name = "Кафе", type = CategoryType.EXPENSE, iconRes = "restaurant", budgetLimit = null),
                Category(id = "3", name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "directions_bus", budgetLimit = null),
                Category(id = "4", name = "Зарплата", type = CategoryType.INCOME, iconRes = "work", budgetLimit = null),
                Category(id = "5", name = "Переводы", type = CategoryType.EXPENSE, iconRes = "swap_horiz", budgetLimit = null)
            ),
            onDismiss = {},
            onAddClick = { _, _, _, _ -> }
        )
    }
}