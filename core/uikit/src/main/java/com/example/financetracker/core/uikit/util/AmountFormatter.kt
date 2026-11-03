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
}