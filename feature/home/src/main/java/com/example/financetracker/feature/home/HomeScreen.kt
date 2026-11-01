package com.example.financetracker.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    val selectedAccount = uiState.accounts.firstOrNull { it.id == selectedAccountId }

    if (selectedAccount != null) {
        AccountDetailScreen(
            account = selectedAccount,
            onBack = { selectedAccountId = null },
            onSave = viewModel::updateAccount,
            modifier = modifier
        )
    } else if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        HomeAccountList(
            accounts = uiState.accounts,
            onAccountClick = { selectedAccountId = it },
            modifier = modifier
        )
    }
}

@Composable
private fun HomeAccountList(
    accounts: List<Account>,
    onAccountClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Мои счета",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            if (accounts.isEmpty()) {
                Text(
                    text = "Счетов пока нет",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                accounts.forEach { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountClick(account.id) },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(22.dp)) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = formatAccountBalance(account.balance),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = account.type.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatAccountBalance(balance: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ru-RU")).format(balance)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val previewAccounts = listOf(
        Account("1", "Основная карта", 40510.20, type = AccountType.CARD),
        Account("2", "Наличные", 8200.0, type = AccountType.CASH)
    )

    FinancetrackerTheme {
        HomeAccountList(
            accounts = previewAccounts,
            onAccountClick = {}
        )
    }
}
