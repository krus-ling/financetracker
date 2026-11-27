package com.example.financetracker.feature.home.domain.parser.extractor

import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.feature.home.domain.parser.RussianStemmer
import com.example.financetracker.feature.home.domain.parser.ai.FastTextCharEmbedder
import com.example.financetracker.feature.home.domain.parser.ai.TextEmbedder
import com.example.financetracker.feature.home.domain.parser.matcher.AccountMatcher
import com.example.financetracker.feature.home.domain.parser.matcher.CategoryMatcher

/**
 * Результат извлечения слов-триггеров для самообучения.
 *
 * @param categoryKeywordStems Стеммы слов для привязки к категории.
 * @param accountAliasStems Стеммы слов для привязки к счёту.
 */
data class LearnedKeywordsResult(
    val categoryKeywordStems: List<String>,
    val accountAliasStems: List<String>
)

/**
 * Извлекает чистые ключевые слова из фразы быстрого ввода при подтверждении транзакции.
 * Отсеивает даты, суммы, стоп-слова и корректно разделяет триггеры счетов и категорий
 * (в том числе без предлогов вида «такси карман»).
 */
object KeywordExtractor {

    private val NOISE_WORDS = setOf(
        "в", "во", "на", "с", "со", "к", "ко", "по", "для", "от", "до", "из", "за", "и", "а", "но", "или", "у",
        "купил", "купила", "потратил", "потратила", "оплатил", "оплатила", "заплатил", "заплатила",
        "взял", "взяла", "сходил", "сходила", "заказал", "заказала", "отдал", "отдала",
        "рублей", "рубля", "руб", "тысяч", "тысячи", "тысяча", "штук", "штуки"
    )

    private val GENERIC_ACCOUNT_WORDS = setOf(
        "карта", "карты", "карте", "карту", "картой", "карточка", "карточки", "карточкой",
        "нал", "нала", "налом", "наличка", "наличные", "кэш", "кэша", "кэшем", "кеш"
    )

    private val ACCOUNT_SOURCE_PREPOSITIONS = setOf("с", "со", "из")

    fun extractCandidates(
        rawText: String,
        categories: List<Category>,
        accounts: List<Account>,
        finalCategory: Category?,
        finalAccount: Account?,
        wasCategoryManuallyChanged: Boolean = false,
        wasAccountManuallyChanged: Boolean = false,
        learnedCategoryKeywords: Map<String, String> = emptyMap(),
        learnedAccountAliases: Map<String, String> = emptyMap(),
        textEmbedder: TextEmbedder = FastTextCharEmbedder()
    ): LearnedKeywordsResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return LearnedKeywordsResult(emptyList(), emptyList())

        var prepared = trimmed
            .replace(Regex("""(?<=\d)[.\s]+(?=\d{3}(?!\d))"""), "")
            .replace(
                Regex("""(\d+(?:[.,]\d+)?)\s+(к|k|тыс(?:яч|ячи)?)\b""", RegexOption.IGNORE_CASE),
                "$1$2"
            )
        val tokens = prepared.split(Regex("""\s+""")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return LearnedKeywordsResult(emptyList(), emptyList())

        val lowerTokens = tokens.map { it.lowercase().replace('ё', 'е').trim() }
        val ignoredIndices = mutableSetOf<Int>()

        DateExtractor.extract(tokens)?.let { ignoredIndices.addAll(it.consumedIndices) }
        AmountExtractor.extract(tokens, ignoredIndices)?.let { ignoredIndices.addAll(it.consumedIndices) }
        val extractedType = TypeExtractor.extract(tokens, ignoredIndices)
        ignoredIndices.addAll(extractedType.consumedIndices)

        val accountIndices = mutableSetOf<Int>()
        val categoryMatchedIndices = mutableSetOf<Int>()

        // 1. Слова, распознанные AccountMatcher
        val matchedAccount = AccountMatcher.match(
            tokens = tokens,
            accounts = accounts,
            defaultAccountId = null,
            textEmbedder = textEmbedder,
            learnedAliases = learnedAccountAliases,
            excludedIndices = ignoredIndices
        )
        accountIndices.addAll(matchedAccount.consumedIndices)

        // 2. Слова после предлогов источника счёта («с совка», «из заначки»)
        for (i in 1 until lowerTokens.size) {
            if (i in ignoredIndices || (i - 1) in ignoredIndices) continue
            if (lowerTokens[i - 1] in ACCOUNT_SOURCE_PREPOSITIONS) {
                accountIndices.add(i - 1)
                accountIndices.add(i)
            }
        }

        // 3. Слова, уже распознанные CategoryMatcher (например, «такси» в «такси карман»)
        val typeToMatch = if (finalCategory?.type == CategoryType.INCOME) TransactionType.INCOME else extractedType.type
        val matchedCategory = CategoryMatcher.match(
            tokens = tokens,
            categories = categories,
            operationType = typeToMatch,
            textEmbedder = textEmbedder,
            learnedKeywords = learnedCategoryKeywords,
            excludedIndices = ignoredIndices + accountIndices
        )
        if (matchedCategory.category?.id == finalCategory?.id) {
            categoryMatchedIndices.addAll(matchedCategory.matchedTokenIndices)
        }

        // 4. Нераспознанные значимые слова
        val unrecognizedIndices = lowerTokens.indices.filter { i ->
            i !in ignoredIndices &&
                    i !in accountIndices &&
                    i !in categoryMatchedIndices &&
                    lowerTokens[i] !in NOISE_WORDS &&
                    lowerTokens[i] !in GENERIC_ACCOUNT_WORDS &&
                    lowerTokens[i].length > 2 &&
                    lowerTokens[i].toDoubleOrNull() == null
        }

        // Если пользователь вручную переключил счёт, но не менял категорию
        // (или категория уже сматчилась другими словами), относим оставшиеся слова к алиасу счёта
        if (wasAccountManuallyChanged && (!wasCategoryManuallyChanged || categoryMatchedIndices.isNotEmpty())) {
            accountIndices.addAll(unrecognizedIndices)
        }

        val accountAliasStems = mutableListOf<String>()
        if (finalAccount != null) {
            for (idx in accountIndices) {
                val word = lowerTokens[idx]
                if (word in NOISE_WORDS || word in GENERIC_ACCOUNT_WORDS || word.length <= 2) continue
                accountAliasStems.add(RussianStemmer.stem(word))
            }
        }

        val categoryKeywordStems = mutableListOf<String>()
        if (finalCategory != null && (wasCategoryManuallyChanged || categoryMatchedIndices.isNotEmpty())) {
            val categoryNameStems = finalCategory.name.split(" ")
                .map { RussianStemmer.stem(it.lowercase().replace('ё', 'е')) }
                .toSet()

            for (i in lowerTokens.indices) {
                if (i in ignoredIndices || i in accountIndices) continue
                val word = lowerTokens[i]
                if (word in NOISE_WORDS || word in GENERIC_ACCOUNT_WORDS || word.length <= 2) continue
                if (word.toDoubleOrNull() != null) continue

                val stem = RussianStemmer.stem(word)
                if (stem.length > 2 && stem !in categoryNameStems) {
                    categoryKeywordStems.add(stem)
                }
            }
        }

        return LearnedKeywordsResult(
            categoryKeywordStems = categoryKeywordStems.distinct(),
            accountAliasStems = accountAliasStems.distinct()
        )
    }
}