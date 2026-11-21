package com.example.financetracker.feature.home.domain.parser


import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.feature.home.domain.model.ParsedTransactionDraft

object FastTransactionParser {

    // Служебные предлоги и союзы, которые не должны матчиться как категории
    private val STOP_WORDS = setOf("в", "на", "с", "со", "к", "ко", "по", "для", "от", "до", "из", "за", "и", "а")

    // Числительные словами для распознавания голосового ввода
    private val NUMBER_WORDS = mapOf(
        "ноль" to 0.0, "один" to 1.0, "одна" to 1.0, "два" to 2.0, "две" to 2.0,
        "три" to 3.0, "четыре" to 4.0, "пять" to 5.0, "шесть" to 6.0, "семь" to 7.0,
        "восемь" to 8.0, "девять" to 9.0, "десять" to 10.0,
        "сто" to 100.0, "двести" to 200.0, "триста" to 300.0, "четыреста" to 400.0,
        "пятьсот" to 500.0, "шестьсот" to 600.0, "семьсот" to 700.0, "восемьсот" to 800.0,
        "девятьсот" to 900.0, "тысяча" to 1000.0, "тысячи" to 1000.0, "тысяч" to 1000.0,
        "полторы" to 1500.0
    )

    // Слова маркеры дохода
    private val INCOME_KEYWORDS = setOf(
        "зарплата", "зарплату", "аванс", "доход", "стипендия", "премия", "бонус", "кешбэк", "подарок"
    )

    // Слова-синонимы мерчантов и брендов для популярных категорий расходов
    private val CATEGORY_SYNONYMS = mapOf(
        "продукт" to listOf("пятерочка", "магнит", "вкусвилл", "перекресток", "ашан", "лента", "супермаркет", "еда", "магазин"),
        "транспорт" to listOf("яндекс", "убер", "такси", "бензин", "лукойл", "газпром", "заправка", "метро", "автобус", "каршеринг"),
        "кафе" to listOf("кофе", "кофейня", "макдак", "вкусно", "точка", "додо", "бургер", "ресторан", "обед", "шаурма", "пицца"),
        "здоров" to listOf("аптека", "лекарства", "доктор", "врач", "анализы", "стоматолог", "витамины"),
        "жиль" to listOf("жкх", "квартплата", "интернет", "аренда", "свет", "коммуналка"),
        "одежд" to listOf("вайлдберриз", "вб", "озон", "обувь", "куртка", "кроссовки")
    )

    /**
     * Разбирает произвольную фразу пользователя
     */
    fun parse(
        text: String,
        categories: List<Category>,
        accounts: List<Account>,
        defaultAccountId: String? = accounts.firstOrNull()?.id
    ): ParsedTransactionDraft {

        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ParsedTransactionDraft()

        // Склеиваем разделители тысяч (точки от голосового ввода "20.000", "1.500" и пробелы "20 000", "1 500")
        // Точка или пробел вырезается ТОЛЬКО если после неё идут ровно 3 цифры,
        // чтобы копейки вроде "150.50" или "20.5" оставались копейками!
        var preparedText = text.replace(
            Regex(
                "(?<=\\d)[.\\s]+(?=\\d{3}(?!\\d))")
            , ""
        )
        // Склеиваем числа с сокращениями: "1.5 к" -> "1.5к", "2 тыс" -> "2тыс"
        // Склеиваем числа с сокращениями: "1.5 к" -> "1.5к", "2 тыс" -> "2тыс"
        preparedText = preparedText.replace(
            Regex(
                "(\\d+(?:[.,]\\d+)?)\\s+(к|k|тыс(?:яч|ячи)?)\\b",
                RegexOption.IGNORE_CASE
            ), "$1$2"
        )

        val rawTokens = preparedText.split(Regex("\\s+")).filter { it.isNotBlank() }
        val consumedIndices = mutableSetOf<Int>()

        // 1. Поиск суммы
        var extractedAmount: Double? = null
        for (i in rawTokens.indices) {
            val token = rawTokens[i]
                .lowercase()
                .trim()

            // Безопасно убираем знак валюты в конце числа (например, "500р", "300руб", "1000₽")
            val cleanToken = token
                .replace(
                    Regex(
                        "(?<=\\d)(?:₽|руб(?:лей|ля)?|р\\.?)$",
                        RegexOption.IGNORE_CASE
                    ), "")
            val numMatch = Regex(
                "^(\\d+(?:[.,]\\d+)?)(к|k|тыс)?$",
                RegexOption.IGNORE_CASE
            ).find(cleanToken)

            if (numMatch != null) {
                var num = numMatch.groupValues[1].replace(',', '.').toDoubleOrNull()
                if (num != null) {
                    if (numMatch.groupValues[2].isNotEmpty()) num *= 1000
                    extractedAmount = num
                    consumedIndices.add(i)
                    break
                }
            }

            // Проверка числительных словами (полторы тысячи, пятьсот)
            val wordNum = NUMBER_WORDS[token]
            if (wordNum != null) {
                extractedAmount = wordNum
                consumedIndices.add(i)
                break
            }
        }

        // 2. Определение типа операции и поиск ключевых слов дохода
        var targetType = TransactionType.EXPENSE
        var matchedCategory: Category? = null

        val hasIncomeKeyword = rawTokens.any { token ->
            INCOME_KEYWORDS.any { kw -> token.lowercase().startsWith(kw) }
        }

        if (hasIncomeKeyword) {
            targetType = TransactionType.INCOME

            // Пытаемся найти подходящую категорию дохода
            matchedCategory = categories.firstOrNull {
                it.type == CategoryType.INCOME && it.name.lowercase().contains("зарплат")
            } ?: categories.firstOrNull { it.type == CategoryType.INCOME }

            // Получаем токен как использованный
            rawTokens.indices.firstOrNull { i ->
                INCOME_KEYWORDS.any { kw -> rawTokens[i].lowercase().startsWith(kw) }
            }?.let { consumedIndices.add(it) }
        }

        // 3. Поиск категории
        if (matchedCategory == null) {
            for (i in rawTokens.indices) {
                if (i in consumedIndices) continue
                val token = rawTokens[i].lowercase()
                val tokenStem = RussianStemmer.stem(token)

                // 3.1 Прямое совпадение по стеммингу с названием категории пользователя
                if (token in STOP_WORDS) continue
                val foundStem = categories.firstOrNull { cat ->
                    cat.name.split(" ").any { word ->
                        val catStem = RussianStemmer.stem(word)
                        catStem.length > 2 && (tokenStem.startsWith(catStem) || catStem.startsWith(tokenStem))
                    }
                }

                if (foundStem != null) {
                    matchedCategory = foundStem
                    targetType = when (foundStem.type) {
                        CategoryType.INCOME -> TransactionType.INCOME
                        CategoryType.EXPENSE -> TransactionType.EXPENSE
                    }
                    consumedIndices.add(i)
                    break
                }

                // 3.2 Поиск по базе синонимов и брендов
                val synonymCategoryRoot = CATEGORY_SYNONYMS.entries.firstOrNull { (_, synonyms) ->
                    synonyms.any { syn -> token.contains(syn) }
                }?.key
                if (synonymCategoryRoot != null) {
                    val foundBySynonym = categories.firstOrNull { cat ->
                        cat.name.split(" ").any { word ->
                            RussianStemmer.stem(word).startsWith(synonymCategoryRoot)
                        }
                    }
                    if (foundBySynonym != null) {
                        matchedCategory = foundBySynonym
                        targetType = TransactionType.EXPENSE
                        consumedIndices.add(i)
                        break
                    }
                }
            }
        }

        // 4. Если введена только сумма без категории = доход БЕЗ категории
        if (extractedAmount != null && matchedCategory == null && !hasIncomeKeyword) {
            targetType = TransactionType.INCOME
            matchedCategory = null
        }

        // 5. Поиск счета по сленгу и названиям
        var matchedAccountId: String? = null
        for (i in rawTokens.indices) {
            if (i in consumedIndices) continue
            val token = rawTokens[i].lowercase()
            // Сленг наличных
            if (token in setOf("нал", "наличка", "наличные", "кэш", "бумажные")) {
                val cashAccount = accounts.firstOrNull {
                    it.type == AccountType.CASH || it.name.lowercase().contains("налич")
                }
                if (cashAccount != null) {
                    matchedAccountId = cashAccount.id
                    consumedIndices.add(i)
                    break
                }
            }
            // Сленг Тинькофф / Т-Банк
            if (token in setOf("тинек", "тинька", "тинькофф", "тбанк", "т-банк")) {
                val tAccount = accounts.firstOrNull { it.name.lowercase().contains("тиньк") || it.name.lowercase().contains("т-банк") }
                if (tAccount != null) {
                    matchedAccountId = tAccount.id
                    consumedIndices.add(i)
                    break
                }
            }
            // Сленг Сбербанк
            if (token in setOf("сбер", "сбербанк")) {
                val sberAccount = accounts.firstOrNull { it.name.lowercase().contains("сбер") }
                if (sberAccount != null) {
                    matchedAccountId = sberAccount.id
                    consumedIndices.add(i)
                    break
                }
            }
            // Совпадение по имени любого счёта пользователя
            val foundAccount = accounts.firstOrNull { acc ->
                acc.name.lowercase() == token || acc.name.lowercase().startsWith(token)
            }
            if (foundAccount != null) {
                matchedAccountId = foundAccount.id
                consumedIndices.add(i)
                break
            }
        }

        // Если счет не был указан, берем счет по умолчанию
        if (matchedAccountId == null) {
            matchedAccountId = defaultAccountId
        }

        // 6. Поиск даты
        var extractedDate: Long? = null
        for (i in rawTokens.indices) {
            if (i in consumedIndices) continue
            val token = rawTokens[i].lowercase()
            when (token) {
                "сегодня" -> {
                    extractedDate = System.currentTimeMillis()
                    consumedIndices.add(i)
                    break
                }
                "вчера" -> {
                    val cal = java.util.Calendar.getInstance()
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                    extractedDate = cal.timeInMillis
                    consumedIndices.add(i)
                    break
                }
                "позавчера" -> {
                    val cal = java.util.Calendar.getInstance()
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -2)
                    extractedDate = cal.timeInMillis
                    consumedIndices.add(i)
                    break
                }
            }
        }

        // 7. Все, что не было распознано как сумма, категории или счет - заметка
        val remainingWords = rawTokens.filterIndexed { index, _ -> index !in consumedIndices }
        val note = remainingWords.joinToString(" ")

        return ParsedTransactionDraft(
            amount = extractedAmount,
            categoryId = matchedCategory?.id,
            accountId = matchedAccountId,
            type = targetType,
            note = note,
            date = extractedDate,
            rawText = text
        )
    }
}