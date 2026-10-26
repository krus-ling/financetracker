package com.example.financetracker.domain.model

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val iconRes: String,
    val budgetLimit: Double? = null
)