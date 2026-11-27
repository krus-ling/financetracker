package com.example.financetracker.feature.home.domain.parser.matcher

import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.feature.home.domain.parser.RussianStemmer
import com.example.financetracker.feature.home.domain.parser.ai.TextEmbedder
import com.example.financetracker.feature.home.util.DamerauLevenshtein

/**
 * Результат сопоставления счета
 *
 * @param accountId Идентификатор найденного счёта (или дефолтный).
 * @param consumedIndices Индексы токенов фразы, идентифицировавших счёт (включая предлог «с/из»).
 */
data class MatchedAccount(
    val accountId: String?,
    val consumedIndices: Set<Int>
)

/**
 * Распознавание счёта с поддержкой:
 *
 * 1. сленга популярных банков («тинек», «сбер», «совок», «альфик», «нал»);
 * 2. прямого стемминга пользовательских названий счетов;
 * 3. нечёткого поиска опечаток через DamerauLevenshtein («савкомбанк» -> «Совкомбанк»);
 * 4. семантических эмбеддингов через TextEmbedder («акции» -> счёт «Инвестиции»);
 * 5. поглощения предлогов источника («с тинька», «из копилки»), чтобы они не попадали в заметку.
 */
object AccountMatcher {

    private val ACCOUNT_PREPOSITIONS = setOf("с", "со", "из", "на", "в", "во")

    private val CASH_EXACT = setOf("нал", "нала", "налом", "кэш", "кэша", "кэшем", "кеш")
    private val CARD_WORDS = setOf("карта", "карты", "карте", "карту", "картой", "карточка", "карточки", "карточкой")
    private val T_BANK_PREFIXES = setOf("тиньк", "тинек", "тбанк", "т-банк")
    private val SOVKOM_PREFIXES = setOf("совок", "совк", "халв")
    private val VTB_SLANG = setOf("втб", "втб24")
    private val OZON_SLANG = setOf("озон", "озонбанк")

    fun match(
        tokens: List<String>,
        accounts: List<Account>,
        defaultAccountId: String?,
        textEmbedder: TextEmbedder,
        learnedAliases: Map<String, String> = emptyMap(),
        excludedIndices: Set<Int> = emptySet()
    ): MatchedAccount {
        val lowerTokens = tokens.map { it.lowercase().replace('ё', 'е').trim() }

        // 0. Адаптивная память пользователя
        if (learnedAliases.isNotEmpty()) {
            for (i in lowerTokens.indices) {
                if (i in excludedIndices) continue
                val token = lowerTokens[i]
                val tokenStem = RussianStemmer.stem(token)

                val learnedAccountId = learnedAliases[token] ?: learnedAliases[tokenStem]
                if (learnedAccountId != null) {
                    val matched = accounts.firstOrNull { it.id == learnedAccountId }
                    if (matched != null) {
                        val consumed = mutableSetOf(i)
                        attachPreposition(i, lowerTokens, excludedIndices, consumed)
                        return MatchedAccount(matched.id, consumed)
                    }
                }
            }
        }

        // 1. Поиск по сленгу банков и наличных
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]

            val matched = when {
                token in CASH_EXACT || token.startsWith("налич") || token.startsWith("бумажн") -> accounts.firstOrNull {
                    it.type == AccountType.CASH || it.name.lowercase().contains("налич")
                }
                token in CARD_WORDS -> accounts.firstOrNull {
                    it.type == AccountType.CARD && it.isDefault
                } ?: accounts.firstOrNull { it.type == AccountType.CARD }
                T_BANK_PREFIXES.any { token.startsWith(it) } -> accounts.firstOrNull {
                    it.name.lowercase().contains("тиньк") || it.name.lowercase().contains("т-банк")
                }
                token.startsWith("сбер") -> accounts.firstOrNull {
                    it.name.lowercase().contains("сбер")
                }
                token.startsWith("альф") -> accounts.firstOrNull {
                    it.name.lowercase().contains("альфа")
                }
                SOVKOM_PREFIXES.any { token.startsWith(it) } -> accounts.firstOrNull {
                    it.name.lowercase().contains("совком") || it.name.lowercase().contains("халв")
                }
                token.startsWith("втб") -> accounts.firstOrNull {
                    it.name.lowercase().contains("втб")
                }
                token.startsWith("озон") -> accounts.firstOrNull {
                    it.name.lowercase().contains("озон")
                }
                else -> null
            }

            if (matched != null) {
                val consumed = mutableSetOf(i)
                attachPreposition(i, lowerTokens, excludedIndices, consumed)
                return MatchedAccount(matched.id, consumed)
            }
        }

        // 2. Прямое совпадение или стемминг имени счета
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            val tokenStem = RussianStemmer.stem(token)

            val matched = accounts.firstOrNull { acc ->
                acc.name.split(" ").any { word ->
                    val accWordStem = RussianStemmer.stem(word)
                    accWordStem.length > 2 && (tokenStem == accWordStem || token.equals(word, ignoreCase = true))
                }
            }

            if (matched != null) {
                val consumed = mutableSetOf(i)
                attachPreposition(i, lowerTokens, excludedIndices, consumed)
                return MatchedAccount(matched.id, consumed)
            }
        }

        // 3. Нечеткий поиск (опечатки через Levenshtein)
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            if (token.length <= 3) continue

            val matched = accounts.firstOrNull { acc ->
                acc.name.split(" ").any { word ->
                    DamerauLevenshtein.isSimilar(token, word)
                }
            }

            if (matched != null) {
                val consumed = mutableSetOf(i)
                attachPreposition(i, lowerTokens, excludedIndices, consumed)
                return MatchedAccount(matched.id, consumed)
            }
        }

        // 4. Семантический AI-поиск (TextEmbedder)
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            if (token.length <= 3) continue
            val bestAccount = textEmbedder.findBestMatch(
                token = token,
                candidates = accounts,
                labelSelector = { it.name },
                minSimilarity = textEmbedder.minAccountSimilarity
            )
            if (bestAccount != null) {
                val consumed = mutableSetOf(i)
                attachPreposition(i, lowerTokens, excludedIndices, consumed)
                return MatchedAccount(bestAccount.id, consumed)
            }
        }

        // 5. Если счет не найден во фразе - используем дефолтный
        return MatchedAccount(defaultAccountId, emptySet())
    }

    /**
     * Поглощает стоящий перед аккаунтом предлог («на озон», «из альфа»),
     * чтобы он не оставался паразитным мусором в тексте заметки.
     */
    private fun attachPreposition(
        accountIndex: Int,
        tokens: List<String>,
        excludedIndices: Set<Int>,
        consumed: MutableSet<Int>
    ) {
        if (accountIndex > 0 && (accountIndex - 1) !in excludedIndices) {
            if (tokens[accountIndex - 1] in ACCOUNT_PREPOSITIONS) {
                consumed.add(accountIndex - 1)
            }
        }
    }
}