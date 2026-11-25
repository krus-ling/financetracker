package com.example.financetracker.feature.home.data.ai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class OnDeviceBertEmbedderTest {

    @Test
    fun rubertMiniRetrieverMatchesCategoriesAndRejectsNoise() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val embedder = OnDeviceBertEmbedder(context)
        val warmUpMs = measureTimeMillis { embedder.warmUp() }

        val categories = listOf("Питомцы", "Ремонт", "Спорт", "Здоровье", "Продукты", "Транспорт")
        val expectedMatches = mapOf(
            "корм" to "Питомцы",
            "перфоратор" to "Ремонт",
            "таблетки" to "Здоровье",
            "метро" to "Транспорт",
            "тренировка" to "Спорт",
            "ветеринар" to "Питомцы"
        )
        val noiseQueries = listOf("встреча", "купил", "наушники")

        val catVectors = categories.associateWith { embedder.embedCaseInvariant(it) }

        val inferenceMs = measureTimeMillis {
            for ((query, expected) in expectedMatches) {
                val qVec = embedder.embed(query)
                assertEquals(312, qVec.size)
                val scores = categories.map { cat ->
                    cat to embedder.cosineSimilarity(qVec, catVectors[cat]!!)
                }.sortedByDescending { it.second }
                println("MINI_RETRIEVER '$query' -> ${scores.joinToString { "${it.first}=${"%.3f".format(it.second)}" }}")

                val matched = embedder.findBestMatch(
                    token = query,
                    candidates = categories,
                    labelSelector = { it },
                    minSimilarity = embedder.minCategorySimilarity
                )
                assertEquals("Query '$query' mismatch", expected, matched)
            }

            for (noise in noiseQueries) {
                val qVec = embedder.embed(noise)
                val scores = categories.map { cat ->
                    cat to embedder.cosineSimilarity(qVec, catVectors[cat]!!)
                }.sortedByDescending { it.second }
                println("MINI_RETRIEVER noise '$noise' -> ${scores.joinToString { "${it.first}=${"%.3f".format(it.second)}" }}")

                val matched = embedder.findBestMatch(
                    token = noise,
                    candidates = categories,
                    labelSelector = { it },
                    minSimilarity = embedder.minCategorySimilarity
                )
                assertNull("Noise '$noise' should not match category", matched)
            }
        }
        println("MINI_RETRIEVER warmUp=${warmUpMs}ms, total inference (9 queries)=${inferenceMs}ms")
        embedder.close()
    }
}