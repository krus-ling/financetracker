package com.example.financetracker.feature.home.domain.parser

import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.feature.home.domain.model.ParsedTransactionDraft
import com.example.financetracker.feature.home.domain.parser.ai.FastTextCharEmbedder
import com.example.financetracker.feature.home.domain.parser.ai.TextEmbedder
import com.example.financetracker.feature.home.domain.parser.extractor.AmountExtractor
import com.example.financetracker.feature.home.domain.parser.extractor.DateExtractor
import com.example.financetracker.feature.home.domain.parser.extractor.NoteExtractor
import com.example.financetracker.feature.home.domain.parser.extractor.TypeExtractor
import com.example.financetracker.feature.home.domain.parser.matcher.AccountMatcher
import com.example.financetracker.feature.home.domain.parser.matcher.CategoryMatcher

/**
 * Оркестратор конвейера интеллектуального разбора текстовых и голосовых фраз.
 *
 * Последовательность стадий (PipeLine):
 * 1. нормализация текста и токенизация;
 * 2. извлечение даты - строго до суммы во избежание коллизий
 * ([com.example.financetracker.feature.home.domain.parser.extractor.DateExtractor]);
 * 3. извлечение суммы
 * ([com.example.financetracker.feature.home.domain.parser.extractor.AmountExtractor]);
 * 4. определение типа операции
 * ([com.example.financetracker.feature.home.domain.parser.extractor.TypeExtractor]);
 * 5. сопоставление счета
 * ([com.example.financetracker.feature.home.domain.parser.matcher.AccountMatcher]);
 * 6. сопоставление категории
 * ([com.example.financetracker.feature.home.domain.parser.matcher.CategoryMatcher]);
 * 7. формирование умной заметки с сохранением мерчантов
 * ([com.example.financetracker.feature.home.domain.parser.extractor.NoteExtractor]).
 */
object FastTransactionParser {

    private val defaultEmbedder: TextEmbedder by lazy { FastTextCharEmbedder() }

    fun parse(
        text: String,
        categories: List<Category>,
        accounts: List<Account>,
        defaultAccountId: String? = accounts.firstOrNull()?.id,
        textEmbedder: TextEmbedder = defaultEmbedder
    ): ParsedTransactionDraft {

        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ParsedTransactionDraft()

        val rawTokens = tokenize(trimmed)
        if (rawTokens.isEmpty()) return ParsedTransactionDraft(rawText = text)

        val consumedIndices = mutableSetOf<Int>()

        // 1. Дата (строго до суммы, чтобы числа вида «24 октября» не попали в сумму)
        val extractedDate = DateExtractor.extract(rawTokens)
        if (extractedDate != null) {
            consumedIndices.addAll(extractedDate.consumedIndices)
        }

        // 2. Сумма
        val extractedAmount = AmountExtractor.extract(rawTokens, excludedIndices = consumedIndices)
        if (extractedAmount != null) {
            consumedIndices.addAll(extractedAmount.consumedIndices)
        }

        // 3. Тип операции
        val extractedType = TypeExtractor.extract(rawTokens, excludedIndices = consumedIndices)
        val isExplicitType = extractedType.consumedIndices.isNotEmpty()
        var targetType = extractedType.type
        consumedIndices.addAll(extractedType.consumedIndices)

        // 4. Счет
        val matchedAccount = AccountMatcher.match(
            tokens = rawTokens,
            accounts = accounts,
            defaultAccountId = defaultAccountId,
            textEmbedder = textEmbedder,
            excludedIndices = consumedIndices
        )
        consumedIndices.addAll(matchedAccount.consumedIndices)

        // 5. Категория
        var matchedCategory = CategoryMatcher.match(
            tokens = rawTokens,
            categories = categories,
            operationType = targetType,
            textEmbedder = textEmbedder,
            excludedIndices = consumedIndices
        )

        // Если тип не был задан явно и среди расходов не нашлось совпадений - проверяем категории доходов
        if (matchedCategory.category == null && !isExplicitType) {
            val incomeMatch = CategoryMatcher.match(
                tokens = rawTokens,
                categories = categories,
                operationType = TransactionType.INCOME,
                textEmbedder = textEmbedder,
                excludedIndices = consumedIndices
            )
            if (incomeMatch.category != null) {
                matchedCategory = incomeMatch
                targetType = TransactionType.INCOME
            }
        }

        // Подбор фоллбэк-категории при явном доходе (зарплата 50_000, аванс 20_000)
        var resolvedCategory = matchedCategory.category
        if (targetType == TransactionType.INCOME && resolvedCategory == null) {
            resolvedCategory = categories.firstOrNull {
                it.type == CategoryType.INCOME && it.name.lowercase().contains("зарплат")
            } ?: categories.firstOrNull { it.type == CategoryType.INCOME }
        } else if (resolvedCategory != null && targetType != TransactionType.TRANSFER) {
            targetType = when (resolvedCategory.type) {
                CategoryType.INCOME -> TransactionType.INCOME
                CategoryType.EXPENSE -> TransactionType.EXPENSE
            }
        }

        // 6. Умная заметка
        val note = NoteExtractor.extract(
            rawTokens = rawTokens,
            baseConsumedIndices = consumedIndices,
            matchedCategory = matchedCategory
        )

        return ParsedTransactionDraft(
            amount = extractedAmount?.amount,
            categoryId = resolvedCategory?.id,
            accountId = matchedAccount.accountId,
            type = targetType,
            note = note,
            date = extractedDate?.timestamp,
            rawText = text
        )
    }

    /**
     * Подготавливает текст: склеивает разделители тысяч (не затрагивая копейки)
     * и суффиксы сокращений («1.5 к» -> «1.5к»), затем разбивает на токены.
     */
    private fun tokenize(text: String): List<String> {
        var prepared = text.replace(Regex("""(?<=\d)[.\s]+(?=\d{3}(?!\d))"""), "")
        prepared = prepared.replace(
            Regex("""(\d+(?:[.,]\d+)?)\s+(к|k|тыс(?:яч|ячи)?)\b""", RegexOption.IGNORE_CASE),
            "$1$2"
        )
        return prepared.split(Regex("""\s+""")).filter { it.isNotBlank() }
    }
}