package com.example.financetracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.component.QuickAddTransactionSheet
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.feature.home.HomeScreen
import com.example.financetracker.feature.home.HomeViewModel
import com.example.financetracker.placeholder.AnalyticsPlaceholderScreen
import com.example.financetracker.placeholder.ProfilePlaceholderScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScaffold(
    homeViewModel: HomeViewModel = koinViewModel()
) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var showQuickAdd by remember { mutableStateOf(false) }
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = { BottomNavBar(currentTab, onTabSelected = { currentTab = it }) },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить операцию")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (currentTab) {
            NavTab.HOME -> HomeScreen(
                Modifier.padding(innerPadding),
                viewModel = homeViewModel
            )
            NavTab.ANALYTICS -> AnalyticsPlaceholderScreen(Modifier.padding(innerPadding))
            NavTab.PROFILE -> ProfilePlaceholderScreen(Modifier.padding(innerPadding))
        }
    }
    if (showQuickAdd) {
        QuickAddTransactionSheet(
            categories = homeUiState.categories,
            onDismiss = { showQuickAdd = false },
            onAddClick = { amount, categoryId, type, note ->
                homeViewModel.addTransaction(
                    amount = amount,
                    categoryId = categoryId,
                    type = type,
                    note = note
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScaffoldLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        MainScaffold()
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScaffoldDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        MainScaffold()
    }
}