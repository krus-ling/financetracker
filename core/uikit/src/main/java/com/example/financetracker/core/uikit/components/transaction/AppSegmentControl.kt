package com.example.financetracker.core.uikit.components.transaction

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.PrimarySoft

/**
 * Универсальный сегментированный переключатель со скользящей пружинной анимацией.
 *
 * @param items Список вариантов любого типа [T].
 * @param selectedItem Текущий выбранный элемент.
 * @param onItemSelected Колбэк при клике на сегмент.
 * @param label Функция маппинга элемента в текстовую подпись.
 */
@Composable
fun <T> AppSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() }
) {
    if (items.isEmpty()) return

    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(PrimarySoft)
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / items.size

        // Анимация движения плашки
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "indicatorOffset"
        )

        // Скользящая плашка
        Box(
            modifier = Modifier
                .width(tabWidth)
                .fillMaxHeight()
                .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Текстовые метки и кликабельные зоны поверх плашки
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index

                // Плавное перетекание цвета текста
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 200),
                    label = "textColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            onItemSelected(item)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(item),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * Сегментированный переключатель типа финансовой операции (Расход / Доход / Перевод).
 * Специализированная обёртка для создания операций (обратная совместимость).
 */
@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = remember {
        listOf(
            TransactionType.EXPENSE to "Расход",
            TransactionType.INCOME to "Доход",
            TransactionType.TRANSFER to "Перевод"
        )
    }
    AppSegmentedControl(
        items = remember { types.map { it.first } },
        selectedItem = selectedType,
        onItemSelected = onTypeSelected,
        label = { type -> types.firstOrNull { it.first == type }?.second.orEmpty() },
        modifier = modifier
    )
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun TransactionTypeSelectorLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        TransactionTypeSelector(
            selectedType = TransactionType.EXPENSE,
            onTypeSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun TransactionTypeSelectorDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        TransactionTypeSelector(
            selectedType = TransactionType.EXPENSE,
            onTypeSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}