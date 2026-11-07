package com.example.financetracker.core.uikit.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object AmountFormatter {
    private val symbols = DecimalFormatSymbols(Locale.forLanguageTag("ru-RU")).apply {
        groupingSeparator = ' ' // разделять тысячи пробелом
        decimalSeparator = ',' // в качестве разделителя целой и дробной части использовать ","
    }

    fun format(amount: Double): String {
        val formatter = DecimalFormat("#,##0.00", symbols)
        val formatted = formatter.format(amount)
        return if (formatted.endsWith(",00")) {
            formatted.removeSuffix(",00")
        } else {
            formatted
        }
    }

    /**
     * Форматирует число для поля ввода без лишнего хвоста ".0" для целых чисел
     */
    fun formatForInput(amount: Double): String =
        if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()

    /**
     * Безопасно парсит введенную строку с точкой или запятой в Double
     */
    fun parseToDoubleOrNull(input: String): Double? =
        input.replace(',', '.').toDoubleOrNull()
}

/**
 * Удобное расширение для парсинга сумм в любом месте приложения
 */
fun String.toAmountOrNull(): Double? =
    AmountFormatter.parseToDoubleOrNull(this)