package com.example.financetracker.feature.home.domain.parser.extractor

import com.example.financetracker.feature.home.domain.parser.matcher.MatchedCategory

/**
 * Модуль сборки умной заметки из неиспользованных токенов.
 *
 * Правила:
 * - дата, сумма, тип операции и счет всегда исключаются из заметки;
 * - категория исключается только при дословном совпадении с ее названием
 * ([com.example.financetracker.feature.home.domain.parser.matcher.MatchedCategory.isDirectNameMatch] == true);
 * - Если категория определена косвенно (через мерчант, синоним или AI - такси, пятерочка, абонемент),
 * исходное слово сохраняется в заметке
 */
object NoteExtractor {

    private val ORPHAN_EDGE_PREPOSITIONS = setOf(
        "в", "во", "на", "с", "со", "к", "ко", "по", "для", "от", "до", "из", "за", "и", "а"
    )

    fun extract(
        rawTokens: List<String>,
        baseConsumedIndices: Set<Int>,
        matchedCategory: MatchedCategory
    ): String {
        val excluded = baseConsumedIndices.toMutableSet()

        // Дословное название (транспорт 500) не дублируем в заметки, а синоним и мерчанты (такси 500) оставляем
        if (matchedCategory.isDirectNameMatch) {
            excluded.addAll(matchedCategory.matchedTokenIndices)
        }

        val remaining = rawTokens
            .filterIndexed { index, _ -> index !in excluded }
            .toMutableList()

        // Удаляем одиночные висячие предлоги на краях оставшейся фразы
        while (remaining.isNotEmpty() && remaining.first().lowercase() in ORPHAN_EDGE_PREPOSITIONS) {
            remaining.removeAt(0)
        }
        while (remaining.isNotEmpty() && remaining.last().lowercase() in ORPHAN_EDGE_PREPOSITIONS) {
            remaining.removeAt(remaining.lastIndex)
        }

        val joined = remaining.joinToString(" ").trim()
        if (joined.isEmpty()) return ""

        return joined.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}