package com.example.financetracker.core.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.IncomeGreen
import com.example.financetracker.core.uikit.theme.TextPrimary
import com.example.financetracker.core.uikit.theme.TransferBlue
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Универсальный компонент для отображения отформатированной денежной суммы.
 *
 * @ инвариант форматирования: разделение тысяч пробелом, копейки через запятую, валюта «₽».
 */
@Composable
fun AmountText(
    amount: Double,
    type: TransactionType,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    currencySymbol: String = "₽"
) {
    // Настраиваем российскую локаль для формата денег (пробел как разделитель тысяч, запятая для копеек)
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("ru-RU")).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("#,##0.00", symbols)
    val formattedNumber = formatter.format(kotlin.math.abs(amount))

    // Определяем префикс и цвет в соответствии с правилами ТЗ
    val prefix: String
    val color = when (type) {
        TransactionType.INCOME -> {
            prefix = "+ "
            IncomeGreen // Используем цвет доходов из Color.kt
        }
        TransactionType.EXPENSE -> {
            prefix = "− "
            MaterialTheme.colorScheme.onSurface // Знак "−" и цвет основного текста TextPrimary по ТЗ
        }
        TransactionType.TRANSFER -> {
            prefix = ""
            TransferBlue // Используем цвет переводов из Color.kt
        }
    }

    val displayText = "$prefix$formattedNumber $currencySymbol"

    Text(
        text = displayText,
        modifier = modifier,
        style = style,
        color = color
    )
}

@Preview(name = "Light Preview", showBackground = true)
@Composable
fun AmountTextPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AmountText(
                amount = 15000.0,
                type = TransactionType.INCOME
            )
            AmountText(
                amount = 2070.0,
                type = TransactionType.EXPENSE
            )
            AmountText(
                amount = 5000.0,
                type = TransactionType.TRANSFER
            )
        }
    }
}

@Preview(name = "Dark Preview", showBackground = false)
@Composable
fun AmountTextDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AmountText(
                amount = 15000.0,
                type = TransactionType.INCOME
            )
            AmountText(
                amount = 2070.0,
                type = TransactionType.EXPENSE
            )
            AmountText(
                amount = 5000.0,
                type = TransactionType.TRANSFER
            )
        }
    }
}