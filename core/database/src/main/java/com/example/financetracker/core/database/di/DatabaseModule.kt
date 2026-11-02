package com.example.financetracker.core.database.di

import com.example.financetracker.core.database.AppDatabase
import com.example.financetracker.core.database.repository.AccountRepository
import com.example.financetracker.core.database.repository.CategoryRepository
import com.example.financetracker.core.database.repository.TransactionRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    // Единственный экземпляр бд на все приложение
    single { AppDatabase.buildDatabase(androidContext()) }

    // Предоставление dao через базу
    single { get<AppDatabase>().accountDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().transactionDao() }

    // Репозитории
    single { AccountRepository(accountDao = get()) }
    single { TransactionRepository(transactionDao = get(), accountDao = get()) }
    single { CategoryRepository(categoryDao = get()) }
}