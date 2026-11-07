package com.example.financetracker.feature.home.components.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.util.AmountFormatter
import com.example.financetracker.core.uikit.util.toAmountOrNull

private val QUICK_LIMITS = listOf(5_000.0, 10_000.0, 25_000.0, 50_000.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetLimitSheet(
    category: Category,
    onDismiss: () -> Unit,
    onSaveLimit: (Double?) -> Unit,
    onDeleteCategory: (() -> Unit)? = null
) {
    var limitText by remember(category.id, category.budgetLimit) {
        mutableStateOf(
            category.budgetLimit?.let { AmountFormatter.formatForInput(it) } ?: ""
        )
    }

    AppFormBottomSheet(
        title = "Лимит: ${category.name}",
        primaryButtonText = "Сохранить",
        onPrimaryClick = {
            val amount = limitText.toAmountOrNull()
            onSaveLimit(amount)
            true
        },
        onDismiss = onDismiss
    ) { clearFocus ->
        Text(
            text = "Задайте максимальный месячный лимит расходов. Приложение предупредит при приближении к нему.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AmountInputField(
            value = limitText,
            onValueChange = { limitText = it },
            label = "Лимит в месяц (₽)",
            placeholder = "0",
            onDone = clearFocus
        )

        // Быстрые кнопки популярных лимитов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QUICK_LIMITS.forEach { limit ->
                FilterChip(
                    selected = limitText == limit.toLong().toString(),
                    onClick = { limitText = limit.toLong().toString() },
                    label = { Text("${limit.toInt() / 1000} тыс ₽") }
                )
            }
        }

        // Кнопка сброса лимита
        if (category.budgetLimit != null) {
            TextButton(
                onClick = { onSaveLimit(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Сбросить лимит",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Кнопка удаления категории
        if (onDeleteCategory != null) {
            TextButton(
                onClick = onDeleteCategory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Удалить категорию",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}