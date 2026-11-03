package com.example.financetracker.core.database.repository

import com.example.financetracker.core.database.AppDatabase.Companion.defaultCategories
import com.example.financetracker.core.database.dao.CategoryDao
import com.example.financetracker.core.database.mapper.toDomain
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun checkAndSeedDefaultCategories() {
        if (categoryDao.getCount() == 0) {
            categoryDao.insertAll(defaultCategories)
        }
    }
}