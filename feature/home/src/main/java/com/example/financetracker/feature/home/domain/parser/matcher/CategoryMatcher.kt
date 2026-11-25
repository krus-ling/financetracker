package com.example.financetracker.feature.home.domain.parser.matcher

import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.feature.home.domain.parser.RussianStemmer
import com.example.financetracker.feature.home.domain.parser.ai.TextEmbedder
import com.example.financetracker.feature.home.util.DamerauLevenshtein

/**
 * Результат сопоставления категории.
 *
 * @param category Найденная категория (или null).
 * @param isDirectNameMatch true, если категория совпала дословно со своим именем.
 * @param matchedTokenIndices Индексы токенов, по которым найдена категория.
 */
data class MatchedCategory(
    val category: Category?,
    val isDirectNameMatch: Boolean,
    val matchedTokenIndices: Set<Int>
)

/**
 * Распознавание категории с фиксацией флага isDirectNameMatch.
 * Если категория названа дословно («Транспорт») -> isDirectNameMatch = true (слово исключается из заметки).
 * Если категория подобрана через синоним («Такси»), мерчант («Пятёрочка») или AI («абонемент») -> isDirectNameMatch = false (слово сохраняется в заметку).
 * 4 уровня поиска: Стемминг -> Levenshtein -> База мерчантов -> TextEmbedder.
 */
object CategoryMatcher {

    private val STOP_WORDS = setOf(
        "в", "на", "с", "со", "к", "ко", "по", "для", "от", "до", "из", "за", "и", "а", "рублей", "руб", "р"
    )

    private val CATEGORY_SYNONYMS = mapOf(
        "продукт" to listOf("пятерочка", "магнит", "вкусвилл", "перекресток", "ашан", "лента", "супермаркет", "еда", "магазин", "хлеб", "сыр", "молоко"),
        "транспорт" to listOf("яндекс", "убер", "такси", "бензин", "лукойл", "газпром", "заправка", "метро", "автобус", "каршеринг", "проезд"),
        "кафе" to listOf("кофе", "кофейня", "макдак", "вкусно", "точка", "додо", "бургер", "ресторан", "обед", "шаурма", "пицца", "бар"),
        "здоров" to listOf("аптека", "лекарства", "доктор", "врач", "анализы", "стоматолог", "витамины"),
        "жиль" to listOf("жкх", "квартплата", "интернет", "аренда", "свет", "коммуналка", "вода"),
        "одежд" to listOf("вайлдберриз", "вб", "озон", "обувь", "куртка", "кроссовки", "джинсы"),
        "спорт" to listOf("зал", "качалка", "абонемент", "фитнес", "бассейн", "тренировка")
    )

    fun match(
        tokens: List<String>,
        categories: List<Category>,
        operationType: TransactionType,
        textEmbedder: TextEmbedder,
        learnedKeywords: Map<String, String> = emptyMap(),
        excludedIndices: Set<Int> = emptySet()
    ): MatchedCategory {
        val lowerTokens = tokens.map { it.lowercase().replace('ё', 'е').trim() }

        // Фильтруем категории по типу операции (расход или доход)
        val candidateCategories = categories.filter { cat ->
            when (operationType) {
                TransactionType.INCOME -> cat.type == CategoryType.INCOME
                TransactionType.EXPENSE -> cat.type == CategoryType.EXPENSE
                TransactionType.TRANSFER -> true
            }
        }

        // 1. Уровень 1. Прямое совпадение по стеммингу с названием категории (дословное)
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            if (token in STOP_WORDS) continue
            val tokenStem = RussianStemmer.stem(token)

            val matched = candidateCategories.firstOrNull { cat ->
                cat.name.split(" ").any { word ->
                    val catSem = RussianStemmer.stem(word)
                    catSem.length > 2 && (tokenStem == catSem || token.equals(word, ignoreCase = true))
                }
            }

            if (matched != null) {
                return MatchedCategory(matched, isDirectNameMatch = true, setOf(i))
            }
        }

        // 2. Уровень 2. Адаптивная память пользователя
        if (learnedKeywords.isNotEmpty()) {
            for (i in lowerTokens.indices) {
                if (i in excludedIndices) continue
                val token = lowerTokens[i]
                if (token in STOP_WORDS || token.length <= 2) continue
                val tokenStem = RussianStemmer.stem(token)

                val learnedCatId = learnedKeywords[token] ?: learnedKeywords[tokenStem]
                if (learnedCatId != null) {
                    val matched = candidateCategories.firstOrNull { it.id == learnedCatId }
                    if (matched != null) {
                        val isDirect = matched.name.split(" ").any { word ->
                            RussianStemmer.stem(word) == tokenStem
                        }
                        return MatchedCategory(matched, isDirectNameMatch = isDirect, setOf(i))
                    }
                }
            }
        }

        // 3. Нечеткий поиск по названиям категории (дословная опечатка)
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            if (token in STOP_WORDS || token.length <= 3) continue

            val matched = candidateCategories.firstOrNull { cat ->
                cat.name.split(" ").any { word ->
                    DamerauLevenshtein.isSimilar(token, word)
                }
            }

            if (matched != null) {
                return MatchedCategory(matched, isDirectNameMatch = true, setOf(i))
            }
        }

        // 4. Уровень 3. Словарь синонимов и мерчантов (косвенное совпадение - такси, пятерочка)
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            if (token in STOP_WORDS) continue

            val synonymRoot = CATEGORY_SYNONYMS.entries.firstOrNull { (_, synonyms) ->
                synonyms.any { syn -> token.contains(syn) || DamerauLevenshtein.isSimilar(token, syn) }
            }?.key

            if (synonymRoot != null) {
                val matched = candidateCategories.firstOrNull { cat ->
                    cat.name.split(" ").any { word ->
                        val lowerWord = word.lowercase().replace('ё', 'е')
                        RussianStemmer.stem(lowerWord).startsWith(synonymRoot) || lowerWord.startsWith(synonymRoot)
                    }
                }

                if (matched  != null) {
                    return MatchedCategory(matched, isDirectNameMatch = false, setOf(i))
                }
            }
        }

        // 5. Уровень 4. On-Device Векторный эмбеддер
        for (i in lowerTokens.indices) {
            if (i in excludedIndices) continue
            val token = lowerTokens[i]
            if (token in STOP_WORDS || token.length <= 3) continue

            val tokenVec = textEmbedder.embed(token)
            var bestSimilarity = 0f
            var bestCategory: Category? = null

            for (cat in candidateCategories) {
                val catVec = textEmbedder.embed(cat.name)
                val sim = textEmbedder.cosineSimilarity(tokenVec, catVec)
                if (sim > bestSimilarity && sim >= 0.65f) {
                    bestSimilarity = sim
                    bestCategory = cat
                }
            }

            if (bestCategory != null) {
                return MatchedCategory(bestCategory, isDirectNameMatch = false, setOf(i))
            }
        }

        return MatchedCategory(null, isDirectNameMatch = false, emptySet())
    }
}