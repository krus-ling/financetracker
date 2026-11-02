package com.example.financetracker.core.uikit.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.financetracker.core.uikit.theme.TextPrimary
import com.example.financetracker.core.uikit.theme.IncomeGreen
import com.example.financetracker.core.uikit.theme.TransferBlue
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Типы финансовых транзакций для корректного отображения знака и цвета суммы.
 */
enum class TransactionType {
    INCOME,   // Доход (+)
    EXPENSE,  // Расход (−)
    TRANSFER  // Перевод (без знака)
}

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
            TextPrimary // Знак "−" и цвет основного текста TextPrimary по ТЗ
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