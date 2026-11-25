package com.example.financetracker.feature.home.data.ai

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.os.Looper
import android.util.LruCache
import com.example.financetracker.feature.home.domain.parser.ai.FastTextCharEmbedder
import com.example.financetracker.feature.home.domain.parser.ai.TextEmbedder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable
import kotlin.math.sqrt

/**
 * On-Device эмбеддер на базе квантованной модели `rubert-mini-retriever-int8.onnx` и ONNX Runtime Mobile.
 *
 * Извлекает `[CLS]`-представление (312-мерный вектор) из `last_hidden_state`
 * и применяет L2-нормализацию, согласно `1_Pooling/config.json` (`sergeyzh/rubert-mini-retriever`).
 *
 * Включает:
 * - Ленивый/фоновый прогрев модели ([warmUp]) без блокировки UI-потока;
 * - Кэширование векторов в [android.util.LruCache];
 * - Безопасный fallback на [com.example.financetracker.feature.home.domain.parser.ai.FastTextCharEmbedder]
 * при отсутствии модели или до окончания загрузки.
 */
class OnDeviceBertEmbedder(
    context: Context,
    private val modelAssetPath: String = "models/rubert-mini-retriever-int8.onnx",
    private val vocabAssetPath: String = "models/vocab.txt"
) : TextEmbedder, Closeable {

    private val appContext = context.applicationContext
    private val fallbackEmbedder = FastTextCharEmbedder()

    /** Кэш реальных 312-мерных векторов для мгновенного повторного доступа */
    private val embeddingCache = LruCache<String, FloatArray>(256)

    private val initMutex = Mutex()
    private val initLock = Any()

    @Volatile
    private var environment: OrtEnvironment? = null

    @Volatile
    private var session: OrtSession? = null

    @Volatile
    private var tokenizer: WordPieceTokenizer? = null

    @Volatile
    private var isFailed = false

    private val isReady: Boolean
        get() = session != null && tokenizer != null

    override val minCategorySimilarity: Float
        get() = if (isReady) BERT_MIN_CATEGORY_SIMILARITY else fallbackEmbedder.minCategorySimilarity

    override val minAccountSimilarity: Float
        get() = if (isReady) BERT_MIN_ACCOUNT_SIMILARITY else fallbackEmbedder.minAccountSimilarity

    override val minScoreMargin: Float
        get() = if (isReady) BERT_MIN_SCORE_MARGIN else fallbackEmbedder.minScoreMargin

    /**
     * Асинхронно загружает ONNX-модель и словарь в память на [Dispatchers.IO].
     */
    override suspend fun warmUp() {
        if (isReady || isFailed) return
        initMutex.withLock {
            if (isReady || isFailed) return
            withContext(Dispatchers.IO) {
                initializeInternal()
            }
        }
    }


    override fun embed(text: String): FloatArray {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return FloatArray(EMBEDDING_DIM)
        // 1. Быстрая проверка кэша
        embeddingCache.get(cleanText)?.let { return it }
        // 2. Если модель ещё не загружена: на фоновом потоке инициализируем сразу,
        // а главный поток никогда не блокируем (временно используем лёгкий фоллбэк)
        if (!isReady && !isFailed) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                initializeInternal()
            } else {
                return fallbackEmbedder.embed(cleanText)
            }
        }
        val currentSession = session
        val currentEnv = environment
        val currentTokenizer = tokenizer
        if (currentSession == null || currentEnv == null || currentTokenizer == null) {
            return fallbackEmbedder.embed(cleanText)
        }
        val vector = runInference(cleanText, currentEnv, currentSession, currentTokenizer)
        return if (vector != null) {
            embeddingCache.put(cleanText, vector)
            vector
        } else {
            fallbackEmbedder.embed(cleanText)
        }
    }

    private fun initializeInternal() {
        if (isReady || isFailed) return
        synchronized(initLock) {
            if (isReady || isFailed) return
            runCatching {
                val loadedTokenizer = WordPieceTokenizer.fromAssets(appContext, vocabAssetPath)
                val modelBytes = appContext.assets.open(modelAssetPath).use { it.readBytes() }
                val env = OrtEnvironment.getEnvironment()
                val options = OrtSession.SessionOptions().apply {
                    setIntraOpNumThreads(2)
                    setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                }
                val ortSession = env.createSession(modelBytes, options)
                tokenizer = loadedTokenizer
                environment = env
                session = ortSession
            }.onFailure {
                isFailed = true
            }
        }
    }

    private fun runInference(
        text: String,
        env: OrtEnvironment,
        ortSession: OrtSession,
        wordPieceTokenizer: WordPieceTokenizer
    ): FloatArray? {
        val tokenized = wordPieceTokenizer.encode(text)
        var inputIdsTensor: OnnxTensor? = null
        var maskTensor: OnnxTensor? = null
        var typeIdsTensor: OnnxTensor? = null
        return try {
            val inputs = HashMap<String, OnnxTensor>(3)
            val inputNames = ortSession.inputNames
            inputIdsTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.inputIds))
            inputs["input_ids"] = inputIdsTensor
            if ("attention_mask" in inputNames) {
                maskTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.attentionMask))
                inputs["attention_mask"] = maskTensor
            }
            if ("token_type_ids" in inputNames) {
                typeIdsTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.tokenTypeIds))
                inputs["token_type_ids"] = typeIdsTensor
            }
            ortSession.run(inputs).use { results ->
                @Suppress("UNCHECKED_CAST")
                val hiddenStates = results[0].value as Array<Array<FloatArray>>
                if (hiddenStates.isEmpty() || hiddenStates[0].isEmpty()) return null
                val clsVector = hiddenStates[0][0]
                normalizeL2(clsVector)
            }
        } catch (_: Exception) {
            null
        } finally {
            inputIdsTensor?.close()
            maskTensor?.close()
            typeIdsTensor?.close()
        }
    }

    private fun normalizeL2(vector: FloatArray): FloatArray {
        var sumSquares = 0.0
        for (v in vector) {
            sumSquares += (v * v).toDouble()
        }
        val norm = sqrt(sumSquares)
        if (norm <= 0.0) return vector
        return FloatArray(vector.size) { i ->
            (vector[i] / norm).toFloat()
        }
    }

    override fun close() {
        runCatching { session?.close() }
        session = null
    }

    private companion object {
        const val EMBEDDING_DIM = 312
        const val BERT_MIN_CATEGORY_SIMILARITY = 0.51f
        const val BERT_MIN_ACCOUNT_SIMILARITY = 0.60f
        const val BERT_MIN_SCORE_MARGIN = 0.025f
    }
}