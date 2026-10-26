package com.example.financetracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financetracker.core.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val iconRes: String,
    val budgetLimit: Double? = null
)