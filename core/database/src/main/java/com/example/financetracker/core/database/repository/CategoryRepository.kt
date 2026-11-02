package com.example.financetracker.core.database.repository

import com.example.financetracker.core.database.dao.CategoryDao
import com.example.financetracker.core.database.mapper.toDomain
import com.example.financetracker.core.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}