package com.example.financetracker.feature.home.domain.parser.extractor

import java.util.Calendar

/**
 * Результат извлечения даты из фразы.
 *
 * @param timestamp время в миллисекундах.
 * @param consumedIndices индексы токенов, поглощенных датой (число, месяц, предлоги).
 */
data class ExtractedDate(
    val timestamp: Long,
    val consumedIndices: Set<Int>
)

/**
 * Модуль извлечения даты из списка токенов фразы.
 */
object DateExtractor {

    private val MONTHS = mapOf(
        "января" to Calendar.JANUARY, "январе" to Calendar.JANUARY, "январь" to Calendar.JANUARY,
        "февраля" to Calendar.FEBRUARY, "феврале" to Calendar.FEBRUARY, "февраль" to Calendar.FEBRUARY,
        "марта" to Calendar.MARCH, "марте" to Calendar.MARCH, "март" to Calendar.MARCH,
        "апреля" to Calendar.APRIL, "апреле" to Calendar.APRIL, "апрель" to Calendar.APRIL,
        "мая" to Calendar.MAY, "мае" to Calendar.MAY, "май" to Calendar.MAY,
        "июня" to Calendar.JUNE, "июне" to Calendar.JUNE, "июнь" to Calendar.JUNE,
        "июля" to Calendar.JULY, "июле" to Calendar.JULY, "июль" to Calendar.JULY,
        "августа" to Calendar.AUGUST, "августе" to Calendar.AUGUST, "август" to Calendar.AUGUST,
        "сентября" to Calendar.SEPTEMBER, "сентябре" to Calendar.SEPTEMBER, "сентябрь" to Calendar.SEPTEMBER,
        "октября" to Calendar.OCTOBER, "октябре" to Calendar.OCTOBER, "октябрь" to Calendar.OCTOBER,
        "ноября" to Calendar.NOVEMBER, "ноябре" to Calendar.NOVEMBER, "ноябрь" to Calendar.NOVEMBER,
        "декабря" to Calendar.DECEMBER, "декабре" to Calendar.DECEMBER, "декабрь" to Calendar.DECEMBER
    )

    private val WEEKDAYS = mapOf(
        "понедельник" to Calendar.MONDAY,
        "вторник" to Calendar.TUESDAY,
        "среду" to Calendar.WEDNESDAY, "среда" to Calendar.WEDNESDAY,
        "четверг" to Calendar.THURSDAY,
        "пятницу" to Calendar.FRIDAY, "пятница" to Calendar.FRIDAY,
        "субботу" to Calendar.SATURDAY, "суббота" to Calendar.SATURDAY,
        "воскресенье" to Calendar.SUNDAY
    )

    fun extract(tokens: List<String>): ExtractedDate? {
        val lowerTokens = tokens.map { it.lowercase().trim() }

        // 1. Календарная дата: число + месяц (24 октября или 24 октября 2024)
        for (i in 0 until lowerTokens.size - 1) {
            val day = lowerTokens[i].toIntOrNull()
            val month = MONTHS[lowerTokens[i + 1]]

            if (day != null && day in 1..31 && month != null) {
                val cal = Calendar.getInstance()
                var year = cal.get(Calendar.YEAR)
                val consumed = mutableSetOf(i, i + 1)

                // Проверяем наличие опционального года третьим словом (24 октября 2024)
                if (i + 2 < lowerTokens.size) {
                    val yearParsed = lowerTokens[i + 2].toIntOrNull()
                    if (yearParsed != null && yearParsed in 1950..2100) {
                        year = yearParsed
                        consumed.add(i + 2)
                    }
                }

                cal.set(year, month, day, 12, 0, 0)
                return ExtractedDate(cal.timeInMillis, consumed)
            }
        }

        // 2. Числовой формат даты (24.10, 24.10.2024 или 24/10)
        for (i in lowerTokens.indices) {
            val match = Regex("""^(\d{1,2})[./](\d{1,2})(?:[./](\d{2,4}))?$""").find(lowerTokens[i])
            if (match != null) {
                val day = match.groupValues[1].toIntOrNull() ?: continue
                val month = (match.groupValues[2].toIntOrNull() ?: continue) - 1
                var year = match.groupValues[3].toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
                if (year < 100) year += 2000

                if (day in 1..31 && month in 0..11) {
                    val cal = Calendar.getInstance()
                    cal.set(year, month, day, 12, 0, 0)
                    return ExtractedDate(cal.timeInMillis, setOf(i))
                }
            }
        }

        // 3. Относительно недели (в прошлую пятницу, в среду, во вторник)
        for (i in lowerTokens.indices) {
            val targetWeekday = WEEKDAYS[lowerTokens[i]]
            if (targetWeekday != null) {
                val consumed = mutableSetOf(i)
                var isPreviousWeek = false

                // Проверяем предшествующие токены (в прошлую, во)
                if (i > 0 && lowerTokens[i - 1].startsWith("прошл")) {
                    isPreviousWeek = true
                    consumed.add(i - 1)
                    if (i > 1 && lowerTokens[i - 2] in setOf("в", "во")) {
                        consumed.add(i - 2)
                    }
                } else if (i > 0 && lowerTokens[i - 1] in setOf("в", "во")) {
                    consumed.add(i - 1)
                }

                val cal = Calendar.getInstance()
                val currentWeekday = cal.get(Calendar.DAY_OF_WEEK)
                var daysBack = currentWeekday - targetWeekday
                if (daysBack < 0) daysBack += 7
                if (isPreviousWeek) daysBack += 7
                if (daysBack == 0 && isPreviousWeek) daysBack = 7

                cal.add(Calendar.DAY_OF_YEAR, -daysBack)
                return ExtractedDate(cal.timeInMillis, consumed)
            }
        }

        // 4. Относительные дни (сегодня, вчера, позавчера, 3 дня назад)
        for (i in lowerTokens.indices) {
            when (lowerTokens[i]) {
                "сегодня" -> return ExtractedDate(System.currentTimeMillis(), setOf(i))
                "вчера" -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    return ExtractedDate(cal.timeInMillis, setOf(i))
                }
                "позавчера" -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -2)
                    return ExtractedDate(cal.timeInMillis, setOf(i))
                }
            }

            // N-дней назад
            if (lowerTokens[i] in setOf("дней", "дня", "день") && i > 0 && i < lowerTokens.size - 1) {
                if (lowerTokens[i + 1] == "назад") {
                    val days = lowerTokens[i - 1].toIntOrNull()
                    if (days != null && days in 1..365) {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -days)
                        return ExtractedDate(cal.timeInMillis, setOf(i - 1, i, i + 1))
                    }
                }
            }
        }

        return null
    }
}