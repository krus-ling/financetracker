package com.example.financetracker.core.model

enum class AccountType(val title: String) {
    CARD("Карта"),
    CASH("Наличные"),
    SAVINGS("Накопления"),
    INVESTMENT("Инвестиции"),
    OTHER("Другое")
}
