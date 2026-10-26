package com.example.financetracker.domain.model

data class Account(
    val id: String,
    val name: String,
    val balance: Double,
    val currency: String = "RUB"
)