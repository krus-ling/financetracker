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
                // Категории расходов (EXPENSE)
                CategoryEntity(name = "Продукты", type = CategoryType.EXPENSE, iconRes = "ic_category_groceries", isSystem = true),
                CategoryEntity(name = "Кафе и рестораны", type = CategoryType.EXPENSE, iconRes = "ic_category_cafe", isSystem = true),
                CategoryEntity(name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "ic_category_transport", isSystem = true),
                CategoryEntity(name = "Жильё", type = CategoryType.EXPENSE, iconRes = "ic_category_housing", isSystem = true),
                CategoryEntity(name = "Связь и интернет", type = CategoryType.EXPENSE, iconRes = "ic_category_internet", isSystem = true),
                CategoryEntity(name = "Здоровье", type = CategoryType.EXPENSE, iconRes = "ic_category_health", isSystem = true),
                CategoryEntity(name = "Развлечения", type = CategoryType.EXPENSE, iconRes = "ic_category_entertainment", isSystem = true),
                CategoryEntity(name = "Одежда", type = CategoryType.EXPENSE, iconRes = "ic_category_clothing", isSystem = true),
                CategoryEntity(name = "Подарки", type = CategoryType.EXPENSE, iconRes = "ic_category_gifts", isSystem = true),
                CategoryEntity(name = "Другое", type = CategoryType.EXPENSE, iconRes = "ic_category_other", isSystem = true),

                // Категории доходов (INCOME)
                CategoryEntity(name = "Зарплата", type = CategoryType.INCOME, iconRes = "ic_category_salary", isSystem = true),
                CategoryEntity(name = "Фриланс", type = CategoryType.INCOME, iconRes = "ic_category_freelance", isSystem = true),
                CategoryEntity(name = "Проценты", type = CategoryType.INCOME, iconRes = "ic_category_percent", isSystem = true),
                CategoryEntity(name = "Кешбэк", type = CategoryType.INCOME, iconRes = "ic_category_cashback", isSystem = true),
                CategoryEntity(name = "Подарки", type = CategoryType.INCOME, iconRes = "ic_category_gifts", isSystem = true),
                CategoryEntity(name = "Другое", type = CategoryType.INCOME, iconRes = "ic_category_other", isSystem = true)
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