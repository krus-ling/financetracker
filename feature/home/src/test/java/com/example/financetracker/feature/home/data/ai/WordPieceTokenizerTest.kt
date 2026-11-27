package com.example.financetracker.feature.home.data.ai

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class WordPieceTokenizerTest {

    private val testVocab = mapOf(
        "[PAD]" to 0,
        "[UNK]" to 1,
        "[CLS]" to 2,
        "[SEP]" to 3,
        "спорт" to 10,
        "##зал" to 11,
        "купил" to 12,
        "корм" to 13,
        "!" to 14
    )

    @Test
    fun `splits compound word into root and ##suffix and wraps with CLS and SEP`() {
        val tokenizer = WordPieceTokenizer(testVocab, maxLength = 32)
        val result = tokenizer.encode("спортзал!")

        // [CLS]=2, спорт=10, ##зал=11, !=14, [SEP]=3
        assertArrayEquals(longArrayOf(2L, 10L, 11L, 14L, 3L), result.inputIds)
        assertArrayEquals(longArrayOf(1L, 1L, 1L, 1L, 1L), result.attentionMask)
        assertArrayEquals(longArrayOf(0L, 0L, 0L, 0L, 0L), result.tokenTypeIds)
    }

    @Test
    fun `truncates tokens beyond maxLength preserving CLS and SEP`() {
        val tokenizer = WordPieceTokenizer(testVocab, maxLength = 4)
        val result = tokenizer.encode("купил корм спорт")

        assertEquals(4, result.inputIds.size)
        assertEquals(2L, result.inputIds.first()) // [CLS]
        assertEquals(3L, result.inputIds.last())  // [SEP]
    }

    @Test
    fun `unknown word maps to UNK token`() {
        val tokenizer = WordPieceTokenizer(testVocab)
        val result = tokenizer.encode("неизвестноеслово")

        assertArrayEquals(longArrayOf(2L, 1L, 3L), result.inputIds)
    }
}