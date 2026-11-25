package com.example.financetracker.feature.home.domain.parser.extractor

import com.example.financetracker.core.model.TransactionType

/**
 * Результат определения типа операции.
 *
 * @param type Тип операции: EXPENSE, INCOME или TRANSFER.
 * @param consumedIndices Индексы токенов, поглощённых маркерами типа.
 */
data class ExtractedType(
    val type: TransactionType,
    val consumedIndices: Set<Int>
)

/**
 * Модуль распознавания типа транзакции
 */
object TypeExtractor {

    private val TRANSFER_KEYWORDS = setOf(
        "перевод", "переводы", "перевел", "перевела", "переведи",
        "перекинул", "перекинула", "скинул", "скинула", "отправил", "отправила"
    )

    private val INCOME_KEYWORDS = setOf(
        "зарплата", "зарплату", "зарплатой", "аванс", "доход",
        "стипендия", "стипендию", "премия", "премию", "бонус",
        "кешбэк", "кэшбэк", "подарок", "получка", "получил", "получила",
        "начислили", "пришло", "вернули"
    )

    fun extract(
        tokens: List<String>,
        excludedIndices: Set<Int> = emptySet()
    ): ExtractedType {
        val lowerTokens = tokens.map { it.lowercase().trim() }

        // 1. Проверка маркеров перевода
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            if (TRANSFER_KEYWORDS.any { token.startsWith(it) }) {
                return ExtractedType(TransactionType.TRANSFER, setOf(i))
            }
        }

        // 2. Проверка маркеров дохода
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            if (INCOME_KEYWORDS.any { token.startsWith(it) }) {
                return ExtractedType(TransactionType.INCOME, setOf(i))
            }
        }

        // 3. По умолчанию - расход
        return ExtractedType(TransactionType.EXPENSE, emptySet())
    }
}