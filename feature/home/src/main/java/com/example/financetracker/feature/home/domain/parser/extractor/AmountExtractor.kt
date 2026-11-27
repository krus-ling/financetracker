package com.example.financetracker.feature.home.domain.parser.extractor

/**
 * Результат извлечения суммы из фразы.
 *
 * @param amount Распознанное числовое значение.
 * @param consumedIndices Индексы токенов (число, валюта, предлог цены «за/на»), поглощённых суммой.
 */
data class ExtractedAmount(
    val amount: Double,
    val consumedIndices: Set<Int>
)

/**
 * Модуль поиска и парсинга суммы операции.
 */
object AmountExtractor {

    private val NUMBER_WORDS = mapOf(
        "ноль" to 0.0, "один" to 1.0, "одна" to 1.0, "два" to 2.0, "две" to 2.0,
        "три" to 3.0, "четыре" to 4.0, "пять" to 5.0, "шесть" to 6.0, "семь" to 7.0,
        "восемь" to 8.0, "девять" to 9.0, "десять" to 10.0,
        "двадцать" to 20.0, "тридцать" to 30.0, "сорок" to 40.0, "пятьдесят" to 50.0,
        "сто" to 100.0, "двести" to 200.0, "триста" to 300.0, "четыреста" to 400.0,
        "пятьсот" to 500.0, "шестьсот" to 600.0, "семьсот" to 700.0, "восемьсот" to 800.0,
        "девятьсот" to 900.0, "тысяча" to 1000.0, "тысячи" to 1000.0, "тысяч" to 1000.0,
        "полторы" to 1500.0
    )

    private val PRICE_PREPOSITIONS = setOf("за", "на", "по")

    fun extract(tokens: List<String>, excludedIndices: Set<Int> = emptySet()): ExtractedAmount? {
        val lowerTokens = tokens.map { it.lowercase().trim() }

        // 1. Поиск составных числительных (полторы тысячи, две тысячи)
        for (i in 0 until lowerTokens.size - 1) {
            if (i in excludedIndices || (i + 1) in excludedIndices) continue
            val first = lowerTokens[i]
            val second = lowerTokens[i + 1]

            if (first == "полторы" && second.startsWith("тысяч")) {
                val consumed = mutableSetOf(i, i + 1)
                attachPreposition(i, lowerTokens, excludedIndices, consumed)
                return ExtractedAmount(1500.0, consumed)
            }

            val firstNum = NUMBER_WORDS[first]
            if (firstNum != null && second.startsWith("тысяч")) {
                val consumed = mutableSetOf(i, i + 1)
                attachPreposition(i, lowerTokens, excludedIndices, consumed)
                return ExtractedAmount(firstNum * 1000.0, consumed)
            }
        }

        // 2. Поиск чисел с явным указанием валюты или множителя (1500р 1.5к 500₽)
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]

            // Очищаем символ валюты в конце числа
            val cleanToken = token.replace(Regex(
                """(?<=\d)(?:₽|руб(?:лей|ля)?|р\.?)$""",
                RegexOption.IGNORE_CASE
            ), "")


            val numMatch = Regex(
                """^(\d+(?:[.,]\d+)?)(к|k|тыс(?:яч|ячи)?)?$""",
                RegexOption.IGNORE_CASE
            ).find(cleanToken)
            if (numMatch != null) {
                var num = numMatch.groupValues[1].replace(',', '.').toDoubleOrNull() ?: continue
                if (numMatch.groupValues[2].isNotEmpty()) num *= 1000.0

                val consumed = mutableSetOf(i)
                attachPreposition(i, lowerTokens, excludedIndices, consumed)
                return ExtractedAmount(num, consumed)
            }

            // Одиночные словесные числительные (пятьсот, полторы)
            val wordNum = NUMBER_WORDS[token]
            if (wordNum != null) {
                val consumed = mutableSetOf(i)
                attachPreposition(i, lowerTokens, excludedIndices, consumed)
                return ExtractedAmount(wordNum, consumed)
            }
        }

        return null
    }

    /**
     * Поглощает стоящий перед суммой предлог («за 1500», «на 500»),
     * чтобы он не оставался паразитным мусором в тексте заметки.
     */
    private fun attachPreposition(
        amountIndex: Int,
        tokens: List<String>,
        excludedIndices: Set<Int>,
        consumed: MutableSet<Int>
    ) {
        if (amountIndex > 0 && (amountIndex - 1) !in excludedIndices) {
            if (tokens[amountIndex - 1] in PRICE_PREPOSITIONS) {
                consumed.add(amountIndex - 1)
            }
        }
    }
}