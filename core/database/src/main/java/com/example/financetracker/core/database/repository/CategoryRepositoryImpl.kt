package com.example.financetracker.core.database.repository

import com.example.financetracker.core.database.AppDatabase.Companion.defaultCategories
import com.example.financetracker.core.database.dao.CategoryDao
import com.example.financetracker.core.database.entity.CategoryEntity
import com.example.financetracker.core.database.mapper.toDomain
import com.example.financetracker.core.database.mapper.toEntity
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
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

    override suspend fun getCategoryById(id: String): Category? {
        val longId = id.toLongOrNull() ?: return null
        return categoryDao.getCategoryById(longId)?.toDomain()
    }

    override suspend fun createCategory(
        name: String,
        type: CategoryType,
        iconRes: String,
        budgetLimit: Double?
    ): Long {
        return categoryDao.insertCategory(
            CategoryEntity(
                name = name,
                type = type,
                iconRes = iconRes,
                budgetLimit = budgetLimit
            )
        )
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun updateBudgetLimit(categoryId: String, budgetLimit: Double?) {
        val longId = categoryId.toLongOrNull() ?: return
        categoryDao.updateBudgetLimit(longId, budgetLimit)
    }

    override suspend fun deleteCategory(categoryId: String) {
        val longId = categoryId.toLongOrNull() ?: return
        categoryDao.deleteCategoryById(longId)
    }

    override suspend fun checkAndSeedDefaultCategories() {
        if (categoryDao.getCount() == 0) {
            categoryDao.insertAll(defaultCategories)
        }
    }
}