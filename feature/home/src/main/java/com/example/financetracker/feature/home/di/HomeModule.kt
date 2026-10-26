package com.example.financetracker.feature.home.di

import com.example.financetracker.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel {
        HomeViewModel(
            accountRepository = get(),
            transactionRepository = get(),
            categoryRepository = get()
        )
    }
}