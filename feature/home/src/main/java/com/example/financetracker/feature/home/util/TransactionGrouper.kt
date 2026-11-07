package com.example.financetracker.feature.home.util

import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionDayGroup
import com.example.financetracker.core.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RU_LOCALE = Locale.forLanguageTag("ru")
private val DATE_FORMATTED_SAME_YEAR = DateTimeFormatter.ofPattern("d MMMM, EEEE", RU_LOCALE)
private val DATE_FORMATTED_WITH_YEAR = DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", RU_LOCALE)

fun groupTransactionByDay(
    transactions: List<Transaction>,
    zoneId: ZoneId = ZoneId.systemDefault()
): List<TransactionDayGroup> {
    if (transactions.isEmpty()) return emptyList()

    val today = LocalDate.now(zoneId)

    // Группируем транзакции по календарному дню через LocalDate
    val groupedByDay = transactions.groupBy { transaction ->
        Instant.ofEpochMilli(transaction.dateTime).atZone(zoneId).toLocalDate()
    }

    // Сортируем дни от новых к старым и формируем группы
    return groupedByDay.entries
        .sortedByDescending { it.key }
        .map { (date, dayTransactions) ->
            var incomeTotal = 0.0
            var expenseTotal = 0.0

            // Считаем суммы в один проход без создания промежуточных списков
            for ((_, amount, type) in dayTransactions) {
                when (type) {
                    TransactionType.INCOME -> incomeTotal += amount
                    TransactionType.EXPENSE -> expenseTotal += amount
                    TransactionType.TRANSFER -> {} // Переводы не меняю общий баланс
                }
            }

            TransactionDayGroup(
                dateTitle = formatDayTitle(date, today),
                dailyTotal = incomeTotal - expenseTotal,
                // Сортируем операции внутри дня от самых новых к более старым
                transactions = dayTransactions.sortedByDescending { it.dateTime }
            )
        }
}

private fun formatDayTitle(date: LocalDate, today: LocalDate): String {
    return when {
        date.isEqual(today) -> "Сегодня"
        date.isEqual(today.minusDays(1)) -> "Вчера"
        date.year == today.year -> date.format(DATE_FORMATTED_SAME_YEAR)
        else -> date.format(DATE_FORMATTED_WITH_YEAR)
    }
}