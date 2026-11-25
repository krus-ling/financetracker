package com.example.financetracker.feature.home.domain.parser.extractor

import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.feature.home.domain.parser.RussianStemmer
import com.example.financetracker.feature.home.domain.parser.ai.FastTextCharEmbedder
import com.example.financetracker.feature.home.domain.parser.ai.TextEmbedder
import com.example.financetracker.feature.home.domain.parser.matcher.AccountMatcher

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
 * Отсеивает даты, суммы, стоп-слова и разделяет триггеры счетов и категорий.
 */
object KeywordExtractor {

    private val NOISE_WORDS = setOf(
        "в", "во", "на", "с", "со", "к", "ко", "по", "для", "от", "до", "из", "за", "и", "а", "но", "или", "у",
        "купил", "купила", "потратил", "потратила", "оплатил", "оплатила", "заплатил", "заплатила",
        "взял", "взяла", "сходил", "сходила", "заказал", "заказала", "отдал", "отдала",
        "рублей", "рубля", "руб", "тысяч", "тысячи", "тысяча", "штук", "штуки"
    )
    private val GENERIC_ACCOUNT_WORDS = setOf(
        "карта", "карты", "карте", "карту", "картой", "карточка", "карточки",
        "нал", "нала", "налом", "наличка", "наличные", "кэш", "кэша", "кэшем", "кеш"
    )

    private val ACCOUNT_SOURCE_PREPOSITIONS = setOf("с", "со", "из")

    fun extractCandidates(
        rawText: String,
        accounts: List<Account>,
        finalCategory: Category?,
        finalAccount: Account?,
        textEmbedder: TextEmbedder = FastTextCharEmbedder()
    ): LearnedKeywordsResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return LearnedKeywordsResult(emptyList(), emptyList())

        var prepared = trimmed
            .replace(
                Regex("""(?<=\d)[.\s]+(?=\d{3}(?!\d))"""),
                ""
            )
            .replace(
                Regex("""(\d+(?:[.,]\d+)?)\s+(к|k|тыс(?:яч|ячи)?)\b""", RegexOption.IGNORE_CASE),
                "$1$2"
            )
        val tokens = prepared.split(Regex("""\s+""")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return LearnedKeywordsResult(emptyList(), emptyList())

        val lowerTokens = tokens.map { it.lowercase().replace('ё', 'е').trim() }
        val ignoredIndices = mutableSetOf<Int>()

        DateExtractor.extract(tokens)?.let { ignoredIndices.addAll(it.consumedIndices) }
        AmountExtractor.extract(tokens, ignoredIndices)?.let {
            ignoredIndices.addAll(it.consumedIndices)
        }
        TypeExtractor.extract(tokens, ignoredIndices).let {
            ignoredIndices.addAll(it.consumedIndices)
        }

        val accountIndices = mutableSetOf<Int>()
        val accountAliasStems = mutableListOf<String>()

        // 1. Слова, распознанные AccountMatcher или стоящие после предлогов источника (с совка, из заначки)
        val matchedAccount = AccountMatcher.match(
            tokens = tokens,
            accounts = accounts,
            defaultAccountId = null,
            textEmbedder = textEmbedder,
            excludedIndices = ignoredIndices
        )
        accountIndices.addAll(matchedAccount.consumedIndices)

        for (i in 1 until lowerTokens.size) {
            if (i in ignoredIndices || (i - 1) in ignoredIndices) continue
            if (lowerTokens[i - 1] in ACCOUNT_SOURCE_PREPOSITIONS) {
                accountIndices.add(i - 1)
                accountIndices.add(i)
            }
        }

        if (finalAccount != null) {
            for (idx in accountIndices) {
                val word = lowerTokens[idx]
                if (word in NOISE_WORDS || word in GENERIC_ACCOUNT_WORDS || word.length <= 2) continue
                accountAliasStems.add(RussianStemmer.stem(word))
            }
        }

        // 2. Оставшиеся значимые слова (кроме имени самой категории и счетов) привязываем к категории
        val categoryKeywordStems = mutableListOf<String>()
        if (finalCategory != null) {
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