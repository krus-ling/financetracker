package com.example.financetracker.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.uikit.components.account.AccountActionButton
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AccountDetailScreen(
    account: Account,
    onBack: () -> Unit,
    onSave: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Text(
                    text = account.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { editing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF075646), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = "Текущий баланс · ${account.type.title}",
                    color = Color(0xFFB9D7CE),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatBalance(account.balance),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AccountActionButton(
                    title = "Расход",
                    icon = Icons.Default.ArrowDownward,
                    onClick = {}
                )
                AccountActionButton(
                    title = "Доход",
                    icon = Icons.Default.ArrowUpward,
                    onClick = {}
                )
                AccountActionButton(
                    title = "Перевод",
                    icon = Icons.Default.SwapHoriz,
                    onClick = {}
                )
            }
            Spacer(Modifier.height(28.dp))
            Text("Операции", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "История операций появится здесь",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (editing) {
        EditAccountBottomSheet(
            account = account,
            onDismiss = { editing = false },
            onSave = onSave
        )
    }
}

private fun formatBalance(balance: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ru-RU")).format(balance)
