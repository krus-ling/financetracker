package com.example.financetracker.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface ScreenKey : NavKey {

    @Serializable
    data object Home : ScreenKey

    @Serializable
    data object Analytics : ScreenKey

    @Serializable
    data object Profile : ScreenKey

    @Serializable
    data class AccountDetail(val accountId: String) : ScreenKey

    @Serializable
    data object Categories : ScreenKey
}