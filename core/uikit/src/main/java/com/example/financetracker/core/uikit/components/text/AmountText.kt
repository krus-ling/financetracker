package com.example.financetracker.core.uikit.components.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.IncomeGreen
import com.example.financetracker.core.uikit.theme.TransferBlue
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

private val RuSymbols = DecimalFormatSymbols(Locale.forLanguageTag("ru-RU")).apply {
    groupingSeparator = ' '
    decimalSeparator = ','
}

/**
 * Универсальный компонент для отображения отформатированной денежной суммы.
 *
 * @param type Тип операции. Если равен null, сумма отображается без префиксов (`+`/`−`) с цветом текста по умолчанию.
 */
@Composable
fun AmountText(
    amount: Double,
    type: TransactionType?,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    currencySymbol: String = "₽",
    color: Color = Color.Unspecified
) {
    // Настраиваем российскую локаль для формата денег:
    // если после запятой "00", скрываем их и отображаем только целое число
    val formattedNumber = remember(amount) {
        val formatter = DecimalFormat("#,##0.00", RuSymbols)
        val formatted = formatter.format(abs(amount))
        if (formatted.endsWith(",00")) {
            formatted.removeSuffix(",00")
        } else {
            formatted
        }
    }

    val prefix: String
    val textColor = if (color != Color.Unspecified) {
        prefix = when (type) {
            TransactionType.INCOME -> "+ "
            TransactionType.EXPENSE -> "− "
            TransactionType.TRANSFER -> ""
            null -> ""
        }
        color
    } else {
        when (type) {
            TransactionType.INCOME -> {
                prefix = "+ "
                IncomeGreen
            }
            TransactionType.EXPENSE -> {
                prefix = "− "
                MaterialTheme.colorScheme.onSurface
            }
            TransactionType.TRANSFER -> {
                prefix = ""
                TransferBlue
            }
            null -> {
                prefix = ""
                MaterialTheme.colorScheme.onSurface
            }
        }
    }

    val displayText = "$prefix$formattedNumber $currencySymbol"

    Text(
        text = displayText,
        modifier = modifier,
        style = style,
        color = textColor
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
                amount = 2070.50,
                type = TransactionType.EXPENSE
            )
            AmountText(
                amount = 5000.00,
                type = TransactionType.TRANSFER
            )
            AmountText(
                amount = 45200.0,
                type = null
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
                amount = 2070.50,
                type = TransactionType.EXPENSE
            )
            AmountText(
                amount = 5000.0,
                type = TransactionType.TRANSFER
            )
            AmountText(
                amount = 45200.0,
                type = null
            )
        }
    }
}
