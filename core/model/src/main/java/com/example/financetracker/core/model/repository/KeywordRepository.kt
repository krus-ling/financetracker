package com.example.financetracker.core.model.repository

import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий адаптивной памяти для самообучения быстрого ввода.
 * Управляет связками слово (стем) -> категория/счет.
 */
interface KeywordRepository {

    /**
     * Реактивная мапа стем слово -> categoryId.
     * При конфликте нескольких категорий для одного слова побеждает наиболее частотная и свежая связка.
     */
    fun observeCategoryKeywords(): Flow<Map<String, String>>

    /**
     * Реактивная мапа алиас (стем) -> accountId.
     */
    fun observeAccountAliases(): Flow<Map<String, String>>

    /**
     * Запоминает или усиливает связь списка ключевых слов с категорией.
     */
    suspend fun learnCategoryKeywords(stems: List<String>, categoryId: String)

    /**
     * Запоминает или усиливает связь списка алиасов со счетом.
     */
    suspend fun learnAccountAliases(stems: List<String>, accountId: String)

    /**
     * Удаляет привязку конкретного слова к категории.
     */
    suspend fun forgetCategoryKeyword(stem: String, categoryId: String)

    /**
     * Удаляет привязку алиаса к счету
     */
    suspend fun forgetAccountAlias(stem: String, accountId: String)
}