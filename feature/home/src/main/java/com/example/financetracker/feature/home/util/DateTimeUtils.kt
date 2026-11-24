package com.example.financetracker.feature.home.util

import java.time.LocalDate
import java.time.ZoneId

/**
 * Утилиты для работы с календарными периодами и датами главного экрана.
 */
object DateTimeUtils {

    private val RUSSIAN_MONTH_NAMES_ACCUSATIVE = listOf(
        "январь", "февраль", "март", "апрель", "май", "июнь",
        "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"
    )

    /**
     * Возвращает временные границы текущего календарного месяца в миллисекундах:
     * - first: 1-е число месяца в 00:00:00.000
     * - second: 1-е число следующего месяца в 00:00:00.000
     */
    fun getCurrentMonthRange(zoneId: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
        val now = LocalDate.now(zoneId)
        val startOfMonth = now
            .withDayOfMonth(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val startOfNextMonth = now
            .plusMonths(1)
            .withDayOfMonth(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        return Pair(startOfMonth, startOfNextMonth)
    }

    /**
     * Возвращает название текущего месяца на русском языке в винительном падеже
     * («за ноябрь», «за май», «за январь»).
     */
    fun getCurrentMonthName(now: LocalDate = LocalDate.now()): String {
        val index = now.monthValue - 1
        return RUSSIAN_MONTH_NAMES_ACCUSATIVE.getOrElse(index) { "месяц" }
    }
}