package com.example.financetracker.feature.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.uikit.components.account.AccountTypeChip
import com.example.financetracker.core.uikit.components.account.AmountInputField
import com.example.financetracker.core.uikit.components.account.AppFormBottomSheet

@Composable
fun EditAccountBottomSheet(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    var name by remember(account.id) { mutableStateOf(account.name) }
    var type by remember(account.id) { mutableStateOf(account.type) }
    var balance by remember(account.id) { mutableStateOf(account.balance.toString()) }
    val validBalance = balance
        .replace(',', '.')
        .toDoubleOrNull()
        ?.takeIf { it >= 0 }

    AppFormBottomSheet(onDismiss = onDismiss) {
        Text("Редактирование счёта", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 30) name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название") },
            supportingText = { Text("${name.length}/30") },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        Text("Тип", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountType.entries.forEach { accountType ->
                AccountTypeChip(
                    type = accountType,
                    selected = type == accountType,
                    onClick = { type = accountType }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = "RUB · Российский рубль",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Валюта") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Валюта заблокирована"
                )
            },
            readOnly = true,
            enabled = false,
            supportingText = { Text("Валюту нельзя изменить: по счёту есть операции") }
        )
        Spacer(Modifier.height(16.dp))
        AmountInputField(
            value = balance,
            onValueChange = { balance = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена") }
            Button(
                onClick = {
                    validBalance?.let { newBalance ->
                        onSave(account.copy(name = name.trim(), type = type, balance = newBalance))
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && validBalance != null,
                modifier = Modifier.weight(1f)
            ) { Text("Сохранить") }
        }
        Spacer(Modifier.height(24.dp))
    }
}
