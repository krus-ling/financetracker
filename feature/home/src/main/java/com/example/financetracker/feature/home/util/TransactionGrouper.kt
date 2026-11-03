package com.example.financetracker.feature.home.util

import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionDayGroup
import com.example.financetracker.core.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun groupTransactionByDay(transactions: List<Transaction>): List<TransactionDayGroup> {
    if (transactions.isEmpty()) return emptyList()

    // Группируем транзакции по началу дня
    val groupedByDay = transactions.groupBy { transaction ->
        Calendar.getInstance().apply {
            timeInMillis = transaction.dateTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Для каждого дгя форматируем заголовок и считаем сумму расходов
    return groupedByDay.map { (dayStartMillis, dayTransactions) ->
        val incomeTotal = dayTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val expenseTotal = dayTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val dailyTotal = incomeTotal - expenseTotal

        TransactionDayGroup(
            dateTitle = formatDayTitle(dayStartMillis),
            dailyTotal = dailyTotal,
            // Сортируем операции внутри дня от самых новых к более старым
            transactions = dayTransactions.sortedByDescending { it.dateTime }
        )
    }.sortedByDescending { group ->
        // Сортируем сами дни от новых к старым
        group.transactions.firstOrNull()?.dateTime ?: 0L
    }
}

private fun formatDayTitle(timestamp: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    return when {
        now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> {
            "Сегодня"
        }
        now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1 -> {
            "Вчера"
        }
        now.get(Calendar.YEAR) == target.get(Calendar.YEAR) -> {
            // Формат «12 мая, вторник»
            val format = SimpleDateFormat("d MMMM, EEEE", Locale.forLanguageTag("ru"))
            format.format(Date(timestamp))
        }
        else -> {
            val formatWithYear = SimpleDateFormat("d MMMM yyyy, EEEE", Locale.forLanguageTag("ru"))
            formatWithYear.format(Date(timestamp))
        }
    }
}