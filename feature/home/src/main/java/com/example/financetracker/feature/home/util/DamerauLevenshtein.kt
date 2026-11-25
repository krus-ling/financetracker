package com.example.financetracker.feature.home.util

import kotlin.math.max
import kotlin.math.min

/**
 * Утилита нечеткого поиска строк по алгоритму Дамерау-Левенштейна.
 *
 * Учитывает:
 * - вставки символов;
 * - удаления символов;
 * - замены символов;
 * - транспозицию (перестановку двух соседних букв).
 */
object DamerauLevenshtein {

    /**
     * Вычисляет расстояние редактирования между двумя строками.
     */
    fun distance(source: String, target: String): Int {
        val s = source.lowercase().replace('ё', 'е').trim()
        val t = target.lowercase().replace('ё', 'е').trim()

        val sLen = s.length
        val tLen = t.length

        if (s == t) return 0
        if (s.isEmpty()) return tLen
        if (t.isEmpty()) return sLen


        // Таблица расстояний
        val d = Array(sLen + 1) { IntArray(tLen + 1) }

        for (i in 0..sLen) d[i][0] = i
        for (j in 0..tLen) d[0][j] = j

        for (i in 1..sLen) {
            for (j in 1..tLen) {
                val cost = if (s[i - 1] == t[j - 1]) 0 else 1

                d[i][j] = min(
                    d[i - 1][j] + 1, // Удаление
                    min(
                        d[i][j - 1] + 1, // Вставка
                        d[i - 1][j - 1] + cost // Замена
                    )
                )

                // Проверка транспозиции (перестановки двух соседних букв)
                if (i > 1 && j > 1 && s[i - 1] == t[j - 2] && s[i - 2] == t[j - 1]) {
                    d[i][j] = min(d[i][j], d[i - 2][j - 2] + cost)
                }
            }
        }

        return d[sLen][tLen]
    }

    /**
     * Проверяет, похожи ли две строки с учётом допустимого порога опечаток.
     *
     * @param maxDistance Максимально допустимое количество правок.
     * Если не передано, вычисляется автоматически по длине слов.
     */
    fun isSimilar(
        source: String,
        target: String,
        maxDistance: Int = defaultMaxDistance(source, target)
    ): Boolean {
        if (maxDistance == 0) return source.equals(target, ignoreCase = true)
        return distance(source, target) <= maxDistance
    }

    /**
     * Возвращает коэффициент схожести от 0.0 (абсолютно разные) до 1.0 (идентичные).
     */
    fun similarityRatio(source: String, target: String): Float {
        val maxLen = max(source.length, target.length)
        if (maxLen == 0) return 1.0f
        val dist = distance(source, target)
        return (1.0f - dist.toFloat() / maxLen).coerceIn(0.0f, 1.0f)
    }

    /**
     * Адаптивный расчёт допустимого количества опечаток:
     * - длина <= 3: 0 опечаток (короткие слова не должны ложно матчиться)
     * - длина 4..6: максимум 1 опечатка
     * - длина >= 7: максимум 2 опечатки
     */
    private fun defaultMaxDistance(s1: String, s2: String): Int {
        val minLen = min(s1.length, s2.length)
        return when {
            minLen <= 4 -> 0
            minLen <= 7 -> 1
            else -> 2
        }
    }
}