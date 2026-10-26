package com.example.financetracker.core.database.mapper

import com.example.financetracker.core.database.entity.AccountEntity
import com.example.financetracker.core.database.entity.CategoryEntity
import com.example.financetracker.core.database.entity.TransactionEntity
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id.toString(),
        name = name,
        balance = balance,
        currency = currency
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id.toLongOrNull() ?: 0L,
        name = name,
        balance = balance,
        currency = currency
    )
}

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id.toString(),
        name = name,
        type = type,
        iconRes = iconRes,
        budgetLimit = budgetLimit
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id.toLongOrNull() ?: 0L,
        name = name,
        type = type,
        iconRes = iconRes,
        budgetLimit = budgetLimit
    )
}

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id.toString(),
        amount = amount,
        categoryId = categoryId?.toString() ?: "",
        accountId = accountId.toString(),
        type = type,
        dateTime = dateTime,
        note = note
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id.toLongOrNull() ?: 0L,
        amount = amount,
        categoryId = categoryId.toLongOrNull(),
        accountId = accountId.toLongOrNull() ?: 0L,
        type = type,
        dateTime = dateTime,
        note = note
    )
}