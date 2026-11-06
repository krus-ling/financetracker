package com.example.financetracker.core.model.repository

import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun createCategory(
        name: String,
        type: CategoryType,
        iconRes: String,
        budgetLimit: Double? = null
    ): Long
    suspend fun updateCategory(category: Category)
    suspend fun updateBudgetLimit(categoryId: String, budgetLimit: Double?)
    suspend fun deleteCategory(categoryId: String)
    suspend fun checkAndSeedDefaultCategories()
}