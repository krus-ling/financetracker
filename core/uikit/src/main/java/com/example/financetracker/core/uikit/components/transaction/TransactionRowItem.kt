package com.example.financetracker.core.uikit.components.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.category.CategoryIcon
import com.example.financetracker.core.uikit.components.icon.AppCircleIcon
import com.example.financetracker.core.uikit.components.text.AmountText
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme

/**
 * Интерактивный элемент строки операции в ленте.
 */
@Composable
fun TransactionRowItem(
    // чтобы строка операции была универсальной, принимает данные извне
    categoryName: String, // текст названия
    amount: Double, // сумма
    type: TransactionType, // тип транзакции
    onClick: () -> Unit, // функцию обратного вызова при клике
    modifier: Modifier = Modifier,
    categoryDetails: String? = null, // второстепенный текст
    iconRes: String? = null,
    categoryIcon: ImageVector? = null,
    iconBackgroundColor: Color? = null,
    iconTint: Color? = null,
) {

    Row( // выстаивает все компоненты по горизонтали
        modifier = modifier
            .fillMaxWidth() // растягивает строку на всю ширину экрана
            .clickable(onClick = onClick) // Клик с ripple-эффектом
            .padding(horizontal = 16.dp, vertical = 12.dp), // Внутренние отступы
        verticalAlignment = Alignment.CenterVertically, // выравнивает все внутренние блоки по вертикальной середине
        horizontalArrangement = Arrangement.SpaceBetween // разнести левую часть и правую колонку по краям
    ) {
        // Слева: иконка категории
        if (iconRes != null) {
            CategoryIcon(
                iconRes = iconRes,
                backgroundColor = iconBackgroundColor,
                iconTint = iconTint,
                size = 44.dp
            )
        } else {
            AppCircleIcon(
                icon = categoryIcon ?: Icons.Default.SwapHoriz,
                backgroundColor = iconBackgroundColor ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                iconTint = iconTint ?: MaterialTheme.colorScheme.primary,
                size = 44.dp
            )
        }

        // В центре: название и детали
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = if (!categoryDetails.isNullOrBlank()) {
                Arrangement.spacedBy(4.dp)
            } else {
                Arrangement.Center
            }
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!categoryDetails.isNullOrBlank()) {
                Text(
                    text = categoryDetails,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Справа: сумма операции
        AmountText(
            amount = amount,
            type = type,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun TransactionPreviewList() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Расход с описанием
        TransactionRowItem(
            categoryName = "Продукты",
            categoryDetails = "Перекрёсток · Основная карта",
            amount = 2450.00,
            type = TransactionType.EXPENSE,
            iconRes = "ic_category_groceries",
            onClick = {}
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // 2. Кафе
        TransactionRowItem(
            categoryName = "Кафе и рестораны",
            categoryDetails = "Додо Пицца · Основная карта",
            amount = 890.00,
            type = TransactionType.EXPENSE,
            iconRes = "ic_category_cafe",
            onClick = {}
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // 3. Перевод между счетами
        TransactionRowItem(
            categoryName = "Перевод между счетами",
            categoryDetails = "Основная карта -> Накопительный",
            amount = 15000.00,
            type = TransactionType.TRANSFER,
            categoryIcon = Icons.Default.SwapHoriz,
            onClick = {}
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // 4. Доход с описанием
        TransactionRowItem(
            categoryName = "Зарплата",
            categoryDetails = "Аванс за ноябрь · Основная карта",
            amount = 55000.00,
            type = TransactionType.INCOME,
            iconRes = "ic_category_salary",
            onClick = {}
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // 5. Доход без описания (по центру)
        TransactionRowItem(
            categoryName = "Кешбэк",
            categoryDetails = null,
            amount = 1250.50,
            type = TransactionType.INCOME,
            iconRes = "ic_category_cashback",
            onClick = {}
        )
    }
}

@Preview(name = "Лента операций — Светлая тема", showBackground = true)
@Composable
private fun TransactionRowAllLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TransactionPreviewList()
        }
    }
}

@Preview(name = "Лента операций — Тёмная тема", showBackground = true)
@Composable
private fun TransactionRowAllDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TransactionPreviewList()
        }
    }
}

@Preview(name = "Отдельные варианты — Светлая тема", showBackground = true)
@Composable
private fun TransactionRowSingleItemLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                TransactionRowItem(
                    categoryName = "Транспорт",
                    categoryDetails = "Метро · Такси",
                    amount = 120.00,
                    type = TransactionType.EXPENSE,
                    iconRes = "ic_category_transport",
                    onClick = {}
                )
                TransactionRowItem(
                    categoryName = "Фриланс",
                    categoryDetails = null,
                    amount = 12000.00,
                    type = TransactionType.INCOME,
                    iconRes = "ic_category_freelance",
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Отдельные варианты — Тёмная тема", showBackground = true)
@Composable
private fun TransactionRowSingleItemDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                TransactionRowItem(
                    categoryName = "Транспорт",
                    categoryDetails = "Метро · Такси",
                    amount = 120.00,
                    type = TransactionType.EXPENSE,
                    iconRes = "ic_category_transport",
                    onClick = {}
                )
                TransactionRowItem(
                    categoryName = "Фриланс",
                    categoryDetails = null,
                    amount = 12000.00,
                    type = TransactionType.INCOME,
                    iconRes = "ic_category_freelance",
                    onClick = {}
                )
            }
        }
    }
}