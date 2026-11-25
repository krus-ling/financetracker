package com.example.financetracker.core.database.repository

import com.example.financetracker.core.database.dao.KeywordDao
import com.example.financetracker.core.model.repository.KeywordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KeywordRepositoryImpl(
    private val keywordDao: KeywordDao
) : KeywordRepository {

    override fun observeCategoryKeywords(): Flow<Map<String, String>> {
        return keywordDao.getAllCategoryKeywords().map { entities ->
            val map = LinkedHashMap<String, String>(entities.size)
            // DAO сортирует по usageCount DESC и lastUsedAt DESC,
            // поэтому putIfAbsent сохраняет самую сильную и свежую связку
            for ((keywordStem, categoryId) in entities) {
                map.putIfAbsent(keywordStem, categoryId.toString())
            }
            map
        }
    }

    override fun observeAccountAliases(): Flow<Map<String, String>> {
        return keywordDao.getAllAccountAliases().map { entities ->
            val map = LinkedHashMap<String, String>(entities.size)
            for ((aliasStem, accountId) in entities) {
                map.putIfAbsent(aliasStem, accountId.toString())
            }
            map
        }
    }

    override suspend fun learnCategoryKeywords(stems: List<String>, categoryId: String) {
        val id = categoryId.toLongOrNull() ?: return
        val now = System.currentTimeMillis()
        for (stem in stems) {
            val clean = stem.trim().lowercase()
            if (clean.isNotEmpty()) {
                keywordDao.upsertCategoryKeyword(stem = clean, categoryId = id, timestamp = now)
            }
        }
    }

    override suspend fun learnAccountAliases(stems: List<String>, accountId: String) {
        val id = accountId.toLongOrNull() ?: return
        val now = System.currentTimeMillis()
        for (stem in stems) {
            val clean = stem.trim().lowercase()
            if (clean.isNotEmpty()) {
                keywordDao.upsertAccountAlias(stem = clean, accountId = id, timestamp = now)
            }
        }
    }

    override suspend fun forgetCategoryKeyword(stem: String, categoryId: String) {
        val id = categoryId.toLongOrNull() ?: return
        keywordDao.deleteCategoryKeyword(stem.trim().lowercase(), id)
    }

    override suspend fun forgetAccountAlias(stem: String, accountId: String) {
        val id = accountId.toLongOrNull() ?: return
        keywordDao.deleteAccountAlias(stem.trim().lowercase(), id)
    }
}