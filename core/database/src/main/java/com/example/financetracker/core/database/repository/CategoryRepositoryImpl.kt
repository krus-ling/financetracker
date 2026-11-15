package com.example.financetracker.core.database.repository

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

    override suspend fun checkAndSeedDefaultCategories() {
        if (categoryDao.getCount() == 0) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Продукты", type = CategoryType.EXPENSE, iconRes = "shopping_cart", isSystem = true),
                CategoryEntity(name = "Кафе", type = CategoryType.EXPENSE, iconRes = "restaurant", isSystem = true),
                CategoryEntity(name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "directions_bus", isSystem = true),
                CategoryEntity(name = "Зарплата", type = CategoryType.INCOME, iconRes = "work", isSystem = true),
                CategoryEntity(name = "Дом", type = CategoryType.EXPENSE, iconRes = "home", isSystem = true),
                CategoryEntity(name = "Развлечения", type = CategoryType.EXPENSE, iconRes = "theater_comedy", isSystem = true),
                CategoryEntity(name = "Здоровье", type = CategoryType.EXPENSE, iconRes = "favorite", isSystem = true),
                CategoryEntity(name = "Другое", type = CategoryType.EXPENSE, iconRes = "more_horiz", isSystem = true)
            )
            categoryDao.insertAll(defaultCategories)
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
}