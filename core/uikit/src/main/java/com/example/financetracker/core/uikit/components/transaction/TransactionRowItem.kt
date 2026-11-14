package com.example.financetracker.core.uikit.components.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.category.CategoryIcon
import com.example.financetracker.core.uikit.components.text.AmountText
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint

/**
 * Интерактивный элемент строки операции в ленте.
 */
@Composable
fun TransactionRowItem(
    // чтобы строка операции была универсальной, принимает данные извне
    categoryName: String, // текст названия
    amount: Double, // сумма
    type: TransactionType, // тип транзакции
    categoryIcon: ImageVector, // иконка
    onClick: () -> Unit, // функцию обратного вызова при клике
    modifier: Modifier = Modifier,
    categoryDetails: String? = null, // второстепенный текст
    iconBackgroundColor: Color = PrimaryTint,
    iconTint: Color = Primary,
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
        // используем созданный ранее CategoryIcon
        CategoryIcon(
            icon = categoryIcon,
            backgroundColor = iconBackgroundColor,
            iconTint = iconTint,
            size = 44.dp
        )

        // В центре: колонка с названием и второстепенным текстом
        // Column выстраивает элементы друг под другом - вертикально
        Column(
            modifier = Modifier
                .weight(1f) // занять всё свободное пространство между левой и правой колонкой
                .padding(horizontal = 12.dp),
            verticalArrangement = if (!categoryDetails.isNullOrBlank()) {
                Arrangement.spacedBy(4.dp)
            } else {
                Arrangement.Center
            }
        ) {
            Text(
                text = categoryName, // название категории
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!categoryDetails.isNullOrBlank()) {
                Text(
                    text = categoryDetails, // второстепенный текст
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Справа: колонка с суммой
        AmountText(
            amount = amount,
            type = type,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun TransactionPreviewList() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Расход с описанием
        TransactionRowItem(
            categoryName = "Супермаркеты",
            categoryDetails = "Перекрёсток · Основная карта",
            amount = 2450.00,
            type = TransactionType.EXPENSE,
            categoryIcon = Icons.Default.ShoppingCart,
            onClick = {}
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // 2. Перевод между счетами
        TransactionRowItem(
            categoryName = "Перевод",
            categoryDetails = "Основная карта -> Накопительный",
            amount = 15000.00,
            type = TransactionType.TRANSFER,
            categoryIcon = Icons.Default.SwapHoriz,
            onClick = {}
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // 3. Доход с описанием (категория + заметка)
        TransactionRowItem(
            categoryName = "Основная карта",
            categoryDetails = "Зарплата · Аванс за ноябрь",
            amount = 55000.00,
            type = TransactionType.INCOME,
            categoryIcon = Icons.AutoMirrored.Filled.TrendingUp,
            onClick = {}
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // 4. Доход БЕЗ описания (только счёт, строго по центру по высоте)
        TransactionRowItem(
            categoryName = "Тинькофф Black",
            categoryDetails = null,
            amount = 3000.00,
            type = TransactionType.INCOME,
            categoryIcon = Icons.AutoMirrored.Filled.TrendingUp,
            onClick = {}
        )
    }
}

@Preview(name = "Все операции — Светлая тема", showBackground = true)
@Composable
private fun TransactionRowAllLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TransactionPreviewList()
        }
    }
}

@Preview(name = "Все операции — Тёмная тема", showBackground = true, backgroundColor = 0xFF121816)
@Composable
private fun TransactionRowAllDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TransactionPreviewList()
        }
    }
}

@Preview(name = "Доход без описания (по центру) — Светлая", showBackground = true)
@Composable
private fun TransactionRowIncomeNoDetailsLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TransactionRowItem(
                categoryName = "Тинькофф Black",
                categoryDetails = null,
                amount = 3000.00,
                type = TransactionType.INCOME,
                categoryIcon = Icons.AutoMirrored.Filled.TrendingUp,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Доход без описания (по центру) — Тёмная", showBackground = true, backgroundColor = 0xFF121816)
@Composable
private fun TransactionRowIncomeNoDetailsDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TransactionRowItem(
                categoryName = "Тинькофф Black",
                categoryDetails = null,
                amount = 3000.00,
                type = TransactionType.INCOME,
                categoryIcon = Icons.AutoMirrored.Filled.TrendingUp,
                onClick = {}
            )
        }
    }
}