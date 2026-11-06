package com.example.financetracker.feature.home.components.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.uikit.components.category.CategoryIcon
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(
    initialType: CategoryType = CategoryType.EXPENSE,
    onDismiss: () -> Unit,
    onAddCategory: (name: String, type: CategoryType, iconRes: String, budgetLimit: Double?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedIcon by remember { mutableStateOf("shopping_cart") }
    var limitText by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    val availableIcons = listOf(
        "shopping_cart", "restaurant", "directions_bus", "work",
        "home", "theater_comedy", "favorite", "more_horiz"
    )

    val isNameValid = name.trim().isNotBlank()

    AppFormBottomSheet(
        title = "Новая категория",
        primaryButtonText = "Создать",
        onPrimaryClick = {
            isSubmitted = true
            if (isNameValid) {
                val limit = if (selectedType == CategoryType.EXPENSE) {
                    limitText.replace(',', '.').toDoubleOrNull()
                } else null
                onAddCategory(name.trim(), selectedType, selectedIcon, limit)
                true
            } else {
                false
            }
        },
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Переключатель типа: Расход / Доход
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == CategoryType.EXPENSE,
                    onClick = { selectedType = CategoryType.EXPENSE },
                    label = { Text("Расход") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == CategoryType.INCOME,
                    onClick = { selectedType = CategoryType.INCOME },
                    label = { Text("Доход") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Название
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                placeholder = { Text("Например, Хобби или Спорт") },
                isError = isSubmitted && !isNameValid,
                supportingText = if (isSubmitted && !isNameValid) {
                    { Text("Введите название категории") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Выбор иконки
            Text(
                text = "Иконка",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(availableIcons) { iconKey ->
                    val isSelected = iconKey == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedIcon = iconKey },
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryIcon(
                            iconRes = iconKey,
                            backgroundColor = Color.Transparent,
                            iconTint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 36.dp
                        )
                    }
                }
            }

            // Опциональный лимит для расхода
            if (selectedType == CategoryType.EXPENSE) {
                AmountInputField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = "Месячный лимит (необязательно)",
                    placeholder = "0"
                )
            }
        }
    }
}