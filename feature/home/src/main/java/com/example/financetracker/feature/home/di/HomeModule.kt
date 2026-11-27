package com.example.financetracker.feature.home.di

import com.example.financetracker.feature.home.data.ai.OnDeviceBertEmbedder
import com.example.financetracker.feature.home.domain.parser.ai.TextEmbedder
import com.example.financetracker.feature.home.presentation.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    // Синглтон On-Device нейросети rubert-mini-retriever-int8
    single<TextEmbedder> {
        OnDeviceBertEmbedder(context = androidContext())
    }

    viewModel {
        HomeViewModel(
            accountRepository = get(),
            transactionRepository = get(),
            categoryRepository = get(),
            keywordRepository = get(),
            textEmbedder = get()
        )
    }
}