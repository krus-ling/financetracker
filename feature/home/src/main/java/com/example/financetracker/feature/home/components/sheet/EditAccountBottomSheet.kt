package com.example.financetracker.feature.home.components.sheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
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

    val isFormValid = name.isNotBlank() && validBalance != null

    AppFormBottomSheet(
        title = "Редактирование счёта",
        primaryButtonText = "Сохранить",
        isPrimaryEnabled = isFormValid,
        onPrimaryClick = {
            if (validBalance != null && name.isNotBlank()) {
                onSave(account.copy(name = name.trim(), type = type, balance = validBalance))
                true
            } else {
                false
            }
        },
        onDismiss = onDismiss
    ) { clearFocus ->
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 30) name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название") },
            supportingText = { Text("${name.length}/30") },
            singleLine = true
        )
        Text("Тип", style = MaterialTheme.typography.titleMedium)
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
        AmountInputField(
            value = balance,
            onValueChange = { balance = it },
            label = "Текущий баланс",
            onDone = clearFocus,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
