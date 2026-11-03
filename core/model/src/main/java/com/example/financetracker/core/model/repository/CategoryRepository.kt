package com.example.financetracker.core.model.repository

import com.example.financetracker.core.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun checkAndSeedDefaultCategories()
}