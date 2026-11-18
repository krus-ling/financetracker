package com.example.financetracker.core.model

/**
 * Группа операций за один календарный день для отображения в ленте.
 *
 * @param dateTitle Заголовок дня: «Сегодня», «Вчера» или «12 мая, вторник».
 * @param dailyTotal Итог дня (доходы минус расходы). Если > 0 — день в плюсе, если < 0 — в минусе.
 * @param transactions Список операций за этот день.
 */
data class TransactionDayGroup(
    val dateTitle: String,
    val dailyTotal: Double,
    val transactions: List<Transaction>
)
