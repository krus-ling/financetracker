package com.example.financetracker.core.uikit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.core.uikit.theme.InkPrimary
import com.example.financetracker.core.uikit.theme.InkTertiary
import com.example.financetracker.core.uikit.theme.Primary
import androidx.compose.ui.text.font.FontWeight
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.PrimaryTint

/**
 * Интерактивный элемент строки операции в ленте.
 */
@Composable
fun TransactionRowItem(
    // чтобы строка операции была универсальной, принимает данные извне
    categoryName: String, // текст названия
    categoryDetails: String, // второстепенный текст
    amount: Double, // сумма
    transactionType: TransactionType, // тип транзакции
    time: String, // время
    categoryIcon: androidx.compose.ui.graphics.vector.ImageVector, // иконка
    onClick: () -> Unit, // функцию обратного вызова при клике
    modifier: Modifier = Modifier,
    iconBackgroundColor: Color = PrimaryTint,
    iconTint: Color = Primary
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
        //Column выстраивает элементы друг под другом - вертикально
        Column(
            modifier = Modifier
                .weight(1f) //занять всё свободное пространство между левой и правой колонкой
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = categoryName, //название категории
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = categoryDetails, // второстепенный текст
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Справа: колонка с суммой и временем
        Column(
            horizontalAlignment = Alignment.End, //выравнивание по горизонтали вправо
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AmountText(
                amount = amount,
                type = transactionType,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = time,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(name = "Transaction Row Light", showBackground = true)
@Composable
fun TransactionRowItemLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        TransactionRowItem(
            categoryName = "Продукты",
            categoryDetails = "Лента · Основная карта",
            amount = -2070.00,
            transactionType = TransactionType.EXPENSE,
            time = "11:20",
            categoryIcon = Icons.Default.ShoppingCart,
            onClick = {}
        )
    }
}

@Preview(name = "Transaction Row Dark", showBackground = true, backgroundColor = 0xFF121816)
@Composable
fun TransactionRowItemDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        TransactionRowItem(
            categoryName = "Продукты",
            categoryDetails = "Лента · Основная карта",
            amount = -2070.00,
            transactionType = TransactionType.EXPENSE,
            time = "11:20",
            categoryIcon = Icons.Default.ShoppingCart,
            onClick = {}
        )
    }
}