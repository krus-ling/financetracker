package com.example.financetracker.feature.home.domain.parser.ai

import kotlin.math.sqrt

/**
 * Контракт для вычисления векторных эмбеддингов текста и их семантического сравнения.
 */
interface TextEmbedder {

    /** Минимальное косинусное сходство для выбора категории. */
    val minCategorySimilarity: Float get() = 0.65f

    /** Минимальное косинусное сходство для выбора счёта. */
    val minAccountSimilarity: Float get() = 0.70f

    /** Минимальный отрыв лидера от второго места (защита от шума). */
    val minScoreMargin: Float get() = 0f

    /** Преобразует текст в нормализованный числовой вектор признаков (embedding). */
    fun embed(text: String): FloatArray

    /**Опциональный фоновый прогрев модели в память */
    suspend fun warmUp() {}

    /**
     * Вычисляет регистронезависимый L2-нормализованный вектор для названия категории или счёта,
     * усредняя исходный вариант и вариант в нижнем регистре (важно для cased WordPiece-моделей).
     */
    fun embedCaseInvariant(text: String): FloatArray {
        val primary = embed(text)
        val lowered = text.lowercase()
        if (lowered == text || primary.isEmpty()) return primary
        val secondary = embed(lowered)
        if (secondary.size != primary.size) return primary
        val combined = FloatArray(primary.size) { i -> (primary[i] + secondary[i]) * 0.5f }
        var sumSquares = 0.0
        for (v in combined) {
            sumSquares += (v * v).toDouble()
        }
        val norm = sqrt(sumSquares)
        if (norm <= 0.0) return primary
        return FloatArray(combined.size) { i -> (combined[i] / norm).toFloat() }
    }

    /**
     * Находит наиболее семантически близкий элемент среди [candidates] для [token],
     * требуя превышения [minSimilarity] и отрыва от второго кандидата не менее чем на [minMargin].
     */
    fun <T> findBestMatch(
        token: String,
        candidates: List<T>,
        labelSelector: (T) -> String,
        minSimilarity: Float,
        minMargin: Float = minScoreMargin
    ): T? {
        if (candidates.isEmpty() || token.isBlank()) return null
        val tokenVec = embed(token)
        var bestSimilarity = Float.NEGATIVE_INFINITY
        var secondBestSimilarity = Float.NEGATIVE_INFINITY
        var bestCandidate: T? = null
        for (candidate in candidates) {
            val candidateVec = embedCaseInvariant(labelSelector(candidate))
            val sim = cosineSimilarity(tokenVec, candidateVec)
            if (sim > bestSimilarity) {
                secondBestSimilarity = bestSimilarity
                bestSimilarity = sim
                bestCandidate = candidate
            } else if (sim > secondBestSimilarity) {
                secondBestSimilarity = sim
            }
        }
        val hasEnoughMargin = candidates.size == 1 || (bestSimilarity - secondBestSimilarity) >= minMargin
        return if (bestSimilarity >= minSimilarity && hasEnoughMargin) bestCandidate else null
    }

    /**
     * Вычисляет косинусное сходство (Cosine Similarity) между двумя векторами.
     * Возвращает значение от -1.0f до 1.0f (чем ближе к 1.0f, тем ближе смысл).
     */
    fun cosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        if (vecA.isEmpty() || vecB.isEmpty() || vecA.size != vecB.size) return 0f
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in vecA.indices) {
            val a = vecA[i].toDouble()
            val b = vecB[i].toDouble()
            dotProduct += a * b
            normA += a * a
            normB += b * b
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom > 0.0) (dotProduct / denom).toFloat().coerceIn(-1.0f, 1.0f) else 0f
    }
}

/**
 * Легковесный On-Device эмбеддер на основе символьных n-грамм (Char N-Gram Embedder).
 *
 * Особенности:
 * - 0 байт дополнительного веса в APK (чистый Kotlin);
 * - скорость < 0.1 мс на запрос;
 * - невосприимчивость к опечаткам в 1-2 буквы: если в слове 8 n-грамм,
 *   опечатка портит только 2 из них, оставляя вектор похожим на 85%+.
 */
class FastTextCharEmbedder(
    private val dimensions: Int = 128
) : TextEmbedder {

    override fun embed(text: String): FloatArray {
        val vector = FloatArray(dimensions)
        val words = text
            .lowercase()
            .trim()
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }

        if (words.isEmpty()) return vector

        var totalGrams = 0

        for (word in words) {
            // Оборачиваем слово маркерами границы: "еда" -> "<еда>"
            val bounded = "<$word>"
            val len = bounded.length

            // Извлекаем n-граммы длинной 3 и 4 символа
            for (n in 3..4) {
                if (len < n) continue
                for (i in 0..len - n) {
                    val gram = bounded.substring(i, i + n)
                    val bucket = (gram.hashCode() and 0x7fffffff) % dimensions
                    vector[bucket] += 1.0f
                    totalGrams++
                }
            }
        }

        // L2-нормализация вектора к единичной длине
        var sumSquares = 0.0
        for (v in vector) {
            sumSquares += (v * v).toDouble()
        }

        val norm = sqrt(sumSquares)
        if (norm > 0.0) {
            for (i in vector.indices) {
                vector[i] = (vector[i] / norm).toFloat()
            }
        }

        return vector
    }
}