package com.example.financetracker.core.uikit.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Рассчитывает ширину элемента для двух (или более) колонок,
 * чтобы они с учётом промежутков идеально заполнили ширину экрана между боковыми полями.
 *
 * @param horizontalPadding Отступы по краям экрана/шторки (по умолчанию 20.dp)
 * @param spacing Промежуток между карточками (по умолчанию 12.dp)
 * @param columns Количество колонок (по умолчанию 2)
 */
@Composable
fun rememberHalfWidth(
    horizontalPadding: Dp = 20.dp,
    spacing: Dp = 12.dp,
    columns: Int = 2
): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    return remember(screenWidth, horizontalPadding, spacing, columns) {
        val totalPadding = horizontalPadding * 2
        val totalSpacing = spacing * (columns - 1)
        val availableWidth = (screenWidth - totalPadding - totalSpacing).coerceAtLeast(0.dp)
        availableWidth / columns
    }
}