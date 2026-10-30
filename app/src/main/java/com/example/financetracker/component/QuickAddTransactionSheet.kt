package com.example.financetracker.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.component.AmountInputField
import com.example.financetracker.core.uikit.component.AppModalBottomSheet
import com.example.financetracker.core.uikit.component.CategoryIconChip
import com.example.financetracker.core.uikit.component.getCategoryIcon
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddTransactionSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onAddClick: (amount: Double, categoryId: String?, type: TransactionType, note: String) -> Unit
) {
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amountFocusRequester = remember { FocusRequester() }

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    // Автофокус на поле ввода суммы при открытии
    LaunchedEffect(Unit) {
        delay(250.milliseconds)
        amountFocusRequester.requestFocus()
    }

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

    AppModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onDismiss()
        }
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        // Вспомогательная функция жесткого снятия фокуса и скрытия клавиатуры
        fun clearAllFocus() {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }

        // Отслеживаем системное закрытие клавиатуры (например, по кнопке Назад)
        val isImeVisible = WindowInsets.isImeVisible
        var wasImeOpened by remember { mutableStateOf(false) }

        LaunchedEffect(isImeVisible) {
            if (isImeVisible) {
                wasImeOpened = true
            } else if (wasImeOpened) {
                // Клавиатура закрылась по системной кнопке Назад — принудительно снимаем фокус
                focusManager.clearFocus(force = true)
                wasImeOpened = false
            }
        }

        // Скрываем клавиатуру сразу же, как только шторка начинает закрываться
        LaunchedEffect(sheetState) {
            snapshotFlow { sheetState.targetValue }
                .collect { target ->
                    if (target == SheetValue.Hidden) {
                        clearAllFocus()
                    }
                }
        }

        // Отслеживаем сдвиг шторки вниз при свайпе за ручку или шапку
        var settledOffset by remember { mutableStateOf<Float?>(null) }

        LaunchedEffect(sheetState) {
            snapshotFlow {
                if (!sheetState.isAnimationRunning && sheetState.currentValue == SheetValue.Expanded) {
                    runCatching { sheetState.requireOffset() }.getOrNull()
                } else null
            }.collect { offset ->
                if (offset != null) {
                    settledOffset = offset
                }
            }
        }

        LaunchedEffect(sheetState) {
            snapshotFlow {
                runCatching { sheetState.requireOffset() }.getOrNull()
            }.collect { currentOffset ->
                val base = settledOffset
                if (currentOffset != null && base != null && currentOffset > base + 15f) {
                    clearAllFocus()
                }
            }
        }

        // Перехватываем свайп/перетаскивание контента шторки вниз для мгновенного скрытия клавиатуры
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y > 0 && source == NestedScrollSource.UserInput) {
                        clearAllFocus()
                    }
                    return Offset.Zero
                }
            }
        }

        // При удалении шторки гарантированно скрываем клавиатуру
        DisposableEffect(Unit) {
            onDispose {
                clearAllFocus()
            }
        }

        // Плавная анимация подъема контента вровень с клавиатурой
        val density = LocalDensity.current
        val imeBottom = WindowInsets.ime.getBottom(density)
        val imeBottomDp = with(density) { imeBottom.toDp() }
        val animatedImePadding by animateDpAsState(
            targetValue = imeBottomDp,
            animationSpec = if (imeBottomDp > 0.dp) {
                // Подъем шторки при открытии клавиатуры — плавный, мягкий (400 мс) с плавным стартом без резкого рывка
                tween(
                    durationMillis = 400,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                )
            } else {
                // Возврат шторки в исходное положение при закрытии клавиатуры — 280 мс
                tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                )
            },
            label = "sheetImePadding"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(bottom = animatedImePadding)
                // Клик по свободной области шторки жестко снимает фокус
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    clearAllFocus()
                }
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Новая операция",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

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
                onDone = { clearAllFocus() }
            )

            // Сетка категорий
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (selectedType == TransactionType.INCOME) "Категория (необязательно)" else "Категория",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                keyboardActions = KeyboardActions(onDone = { clearAllFocus() }),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Кнопка добавления
            Button(
                onClick = {
                    clearAllFocus()
                    submitted = true
                    val chosenCategory = if (selectedType == TransactionType.INCOME) {
                        selectedCategoryId
                    } else {
                        selectedCategoryId ?: filteredCategories.firstOrNull()?.id
                    }
                    val isCategoryValid = selectedType == TransactionType.INCOME || chosenCategory != null

                    if (isAmountValid && isCategoryValid) {
                        onAddClick(amount!!, chosenCategory, selectedType, note)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить")
            }

            // Кнопка отмены
            TextButton(
                onClick = {
                    clearAllFocus()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена")
            }
        }
    }
}