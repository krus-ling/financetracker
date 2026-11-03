package com.example.financetracker.core.uikit.components.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Заголовок дня в ленте операций со сводкой доходов/расходов за день.
 *
 * @param title Текст дня («Сегодня», «Вчера», «12 мая, вторник»).
 * @param dailyTotal Чистый итог дня (доходы минус расходы).
 */
@Composable
fun DateHeader(
    title: String,
    dailyTotal: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Если за день были доходы или расходы (итог не 0), выводим красивый AmountText
        if (dailyTotal != 0.0) {
            val type = if (dailyTotal > 0) TransactionType.INCOME else TransactionType.EXPENSE
            AmountText(
                amount = abs(dailyTotal),
                type = type,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun DateHeaderPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface {
            Column {
                DateHeader(
                    title = "Сегодня",
                    dailyTotal = 3740.0
                )
                DateHeader(
                    title = "Вчера",
                    dailyTotal = -1250.50
                )
                DateHeader(
                    title = "12 мая, вторник",
                    dailyTotal = 0.0
                )
            }
        }
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun DateHeaderDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface {
            Column {
                DateHeader(
                    title = "Сегодня",
                    dailyTotal = 3740.0
                )
                DateHeader(
                    title = "Вчера",
                    dailyTotal = -1250.50
                )
                DateHeader(
                    title = "12 мая, вторник",
                    dailyTotal = 0.0
                )
            }
        }
    }
}