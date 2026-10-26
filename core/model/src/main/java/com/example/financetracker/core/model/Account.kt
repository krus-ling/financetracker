package com.example.financetracker.core.model

data class Account(
    val id: String,
    val name: String,
    val balance: Double,
    val currency: String = "RUB"
)