package com.example.financetracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Главная", Icons.Filled.Home),
    ANALYTICS("Аналитика", Icons.Filled.Analytics),
    PROFILE("Профиль", Icons.Filled.Person)
}
