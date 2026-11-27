package com.example.financetracker.core.uikit.components.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.text.AmountText
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme

/**
 * Универсальная фирменная карточка баланса.
 * Используется:
 * 1. На главном экране (общий баланс + доходы/расходы за месяц).
 * 2. На детальном экране счёта (баланс счёта + звёздочка).
 * 3. На детальном экране удалённого счёта в корзине (баланс + плашка «В корзине»).
 */
@Composable
fun AccountBalanceCard(
    title: String,
    balance: Double,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    incomeTitle: String? = null,
    incomeAmount: Double? = null,
    expenseTitle: String? = null,
    expenseAmount: Double? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Верхняя строка: заголовок и действие/статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleMedium
                )
                trailingContent?.invoke()
            }

            Spacer(Modifier.height(4.dp))

            // Основная сумма
            AmountText(
                amount = balance,
                type = null,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )

            // Подпись под суммой
            if (!subtitle.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Разделитель и доходы/расходы за месяц
            val showStats = incomeAmount != null || expenseAmount != null
            if (showStats) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Доходы за месяц
                    if (incomeAmount != null) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = incomeTitle ?: "Доходы",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(2.dp))
                            AmountText(
                                amount = incomeAmount,
                                type = TransactionType.INCOME,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Расходы за месяц
                    if (expenseAmount != null) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = expenseTitle ?: "Расходы",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(2.dp))
                            AmountText(
                                amount = expenseAmount,
                                type = TransactionType.EXPENSE,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "1. Главный экран (Общий баланс)", showBackground = true)
@Composable
private fun AccountBalanceCardHomePreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            AccountBalanceCard(
                title = "Общий баланс",
                balance = 440293.0,
                subtitle = "Валютные счета пересчитаны по демо-курсу",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "Скрыть баланс",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                },
                incomeTitle = "Доходы за май",
                incomeAmount = 76625.0,
                expenseTitle = "Расходы за май",
                expenseAmount = 69830.0
            )
        }
    }
}
@Preview(name = "2. Экран счёта (Со звёздочкой)", showBackground = true)
@Composable
private fun AccountBalanceCardDetailPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            AccountBalanceCard(
                title = "Текущий баланс · Карта",
                balance = 125000.0,
                trailingContent = {
                    Text("⭐", style = MaterialTheme.typography.titleLarge)
                }
            )
        }
    }
}
@Preview(name = "3. Удалённый счёт (В корзине)", showBackground = true)
@Composable
private fun AccountBalanceCardDeletedPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            AccountBalanceCard(
                title = "Текущий баланс · Карта",
                balance = 4000.0,
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "В корзине",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        }
    }
}