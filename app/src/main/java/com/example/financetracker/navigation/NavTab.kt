package com.example.financetracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavTab(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "Главная", Icons.Filled.Home),
    ANALYTICS("analytics", "Аналитика", Icons.Filled.Analytics),
    PROFILE("profile", "Профиль", Icons.Filled.Person)
}
