package com.example.financetracker.feature.home.data.ai

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.text.iterator

/**
 * Результат токенизации текста, подготовленный к передаче в тензоры BERT/ONNX Runtime.
 *
 * @property inputIds Идентификаторы WordPiece-токенов (обрамлённые `[CLS]` и `[SEP]`).
 * @property attentionMask Маска внимания (`1L` для реальных токенов).
 * @property tokenTypeIds Идентификаторы сегмента (`0L` для одиночной фразы).
 */
data class TokenizedInput(
    val inputIds: LongArray,
    val attentionMask: LongArray,
    val tokenTypeIds: LongArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TokenizedInput) return false
        return inputIds.contentEquals(other.inputIds) &&
                attentionMask.contentEquals(other.attentionMask) &&
                tokenTypeIds.contentEquals(other.tokenTypeIds)
    }
    override fun hashCode(): Int {
        var result = inputIds.contentHashCode()
        result = 31 * result + attentionMask.contentHashCode()
        result = 31 * result + tokenTypeIds.contentHashCode()
        return result
    }
}

/**
 * WordPiece-токенизатор для cased-модели `sergeyzh/rubert-mini-retriever`.
 *
 * Сохраняет исходный регистр букв и букву «ё», так как словарь модели
 * (`vocab.txt`, 55 083 токена) различает регистр и диакритику.
 *
 * @param vocab Словарь соответствия «токен -> индекс».
 * @param doLowerCase Принудительное приведение к нижнему регистру (по умолчанию `false`).
 * @param maxLength Максимальная длина последовательности с учётом `[CLS]` и `[SEP]`.
 * @param maxCharsPerWord Порог длины одного слова, свыше которого оно заменяется на `[UNK]`.
 */
class WordPieceTokenizer(
    private val vocab: Map<String, Int>,
    private val doLowerCase: Boolean = false,
    private val maxLength: Int = 32,
    private val maxCharsPerWord: Int = 100
) {
    /** Индекс токена неизвестного слова */
    private val unkTokenId: Int = vocab["[UNK]"] ?: 1

    /** Индекс стартового токена классификации всей фразы */
    private val clsTokenId: Int = vocab["[CLS]"] ?: 2

    /** Индекс разделительного финального токена */
    private val sepTokenId: Int = vocab["[SEP]"] ?: 3

    /**
     * Кодирует входную строку в тензоры `[CLS] ...tokens... [SEP]`
     * с динамической длиной (не более [maxLength]).
     *
     * Использование динамической длины без лишнего паддинга нулями ускоряет
     * механизм Self-Attention на коротких финансовых фразах в несколько раз.
     */
    fun encode(text: String): TokenizedInput {
        val cleaned = if (doLowerCase) text.lowercase() else text
        val basicTokens = basicTokenize(cleaned)

        val wordPieceIds = mutableListOf<Int>()
        val maxContentTokens = (maxLength - 2).coerceAtLeast(1)

        for (token in basicTokens) {
            if (wordPieceIds.size >= maxContentTokens) break
            for (subTokenId in tokenizeWord(token)) {
                if (wordPieceIds.size >= maxContentTokens) break
                wordPieceIds.add(subTokenId)
            }
        }

        val seqLength = wordPieceIds.size + 2
        val inputIds = LongArray(seqLength)
        val attentionMask = LongArray(seqLength) { 1L }
        val tokenTypeIds = LongArray(seqLength) { 0L }

        inputIds[0] = clsTokenId.toLong()
        for (i in wordPieceIds.indices) {
            inputIds[i + 1] = wordPieceIds[i].toLong()
        }
        inputIds[seqLength - 1] = sepTokenId.toLong()

        return TokenizedInput(
            inputIds = inputIds,
            attentionMask = attentionMask,
            tokenTypeIds = tokenTypeIds
        )
    }

    /**
     * Первичная нарезка текста по пробелам и знакам пунктуации/валюты.
     */
    private fun basicTokenize(text: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        for (ch in text.trim()) {
            when {
                ch.isWhitespace() -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                isPunctuation(ch) -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current.clear()
                    }
                    result.add(ch.toString())
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        return result
    }

    /**
     * Жадный алгоритм разбивки слова на подслова из [vocab] слева направо
     * с префиксом `##` для продолжений.
     */
    private fun tokenizeWord(word: String): List<Int> {
        if (word.length > maxCharsPerWord) return listOf(unkTokenId)
        val outputIds = mutableListOf<Int>()
        var start = 0
        while (start < word.length) {
            var end = word.length
            var matchedId: Int? = null
            while (start < end) {
                val substr = if (start == 0) {
                    word.substring(start, end)
                } else {
                    "##${word.substring(start, end)}"
                }
                val id = vocab[substr]
                if (id != null) {
                    matchedId = id
                    break
                }
                end--
            }
            if (matchedId == null) {
                return listOf(unkTokenId)
            }
            outputIds.add(matchedId)
            start = end
        }
        return outputIds
    }

    /**
     * Проверяет, является ли символ знаком пунктуации или символом валюты.
     */
    private fun isPunctuation(ch: Char): Boolean {
        val type = Character.getType(ch)
        return type == Character.CONNECTOR_PUNCTUATION.toInt() ||
                type == Character.DASH_PUNCTUATION.toInt() ||
                type == Character.START_PUNCTUATION.toInt() ||
                type == Character.END_PUNCTUATION.toInt() ||
                type == Character.INITIAL_QUOTE_PUNCTUATION.toInt() ||
                type == Character.FINAL_QUOTE_PUNCTUATION.toInt() ||
                type == Character.OTHER_PUNCTUATION.toInt() ||
                type == Character.CURRENCY_SYMBOL.toInt()
    }

    companion object {
        /**
         * Загружает словарь `vocab.txt` из произвольного [InputStream].
         */
        fun fromStream(inputStream: InputStream, maxLength: Int = 32): WordPieceTokenizer {
            val vocab = LinkedHashMap<String, Int>(60_000)
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var index = 0
                var line = reader.readLine()
                while (line != null) {
                    vocab[line] = index++
                    line = reader.readLine()
                }
            }
            return WordPieceTokenizer(vocab = vocab, maxLength = maxLength)
        }

        /**
         * Загружает словарь из Android `assets`.
         */
        fun fromAssets(
            context: Context,
            assetPath: String = "models/vocab.txt",
            maxLength: Int = 32
        ): WordPieceTokenizer {
            return context.assets.open(assetPath).use { stream ->
                fromStream(stream, maxLength = maxLength)
            }
        }
    }
}