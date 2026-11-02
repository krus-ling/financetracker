package com.example.financetracker

import android.app.Application
import com.example.financetracker.core.database.di.databaseModule
import com.example.financetracker.feature.home.di.homeModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class FinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FinanceApp)
            modules(
                databaseModule,
                homeModule
            )
        }
    }
}