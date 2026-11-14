package com.example.financetracker.feature.home.components.sheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.uikit.components.account.AccountTypeChip
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.input.AppTextField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.util.AmountFormatter
import com.example.financetracker.core.uikit.util.toAmountOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountBottomSheet(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    var name by remember(account.id) { mutableStateOf(account.name) }
    var type by remember(account.id) { mutableStateOf(account.type) }
    var balance by remember(account.id) {
        mutableStateOf(AmountFormatter.formatForInput(account.balance))
    }

    val validBalance = balance.toAmountOrNull()?.takeIf { it >= 0 }
    val isNameValid = name.trim().isNotBlank()
    val isFormValid = isNameValid && validBalance != null

    val currencyDisplay = remember(account.currency) {
        getCurrencyDisplayTitle(account.currency)
    }

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
        onDismiss = onDismiss,
    ) { clearFocus ->
        // Название счёта
        AppTextField(
            value = name,
            onValueChange = { if (it.length <= 30) name = it },
            label = "Название",
            isError = !isNameValid && name.isNotEmpty(),
            supportingText = {
                if (!isNameValid && name.isNotEmpty()) {
                    Text("Название не может быть пустым")
                } else {
                    Text("${name.length}/30")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Text(
            text = "Тип",
            style = MaterialTheme.typography.titleMedium
        )

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

        // Информационное поле валюты
        AppTextField(
            value = currencyDisplay,
            onValueChange = {},
            label = "Валюта",
            readOnly = true,
            enabled = false,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Валюта заблокирована"
                )
            },
            supportingText = { Text("Валюту нельзя изменить: по счёту есть операции") }
        )

        // Поле ввода текущего баланса
        AmountInputField(
            value = balance,
            onValueChange = { balance = it },
            label = "Текущий баланс",
            onDone = clearFocus,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getCurrencyDisplayTitle(currency: String): String = when (currency.uppercase()) {
    "RUB" -> "RUB · Российский рубль"
    "USD" -> "USD · Доллар США"
    "EUR" -> "EUR · Евро"
    else -> "$currency · Валюта"
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun EditAccountBottomSheetPreview() {
    FinancetrackerTheme(darkTheme = false) {
        EditAccountBottomSheet(
            account = Account(
                id = "1",
                name = "Основная карта",
                balance = 125000.0,
                currency = "RUB",
                type = AccountType.CARD
            ),
            onDismiss = {},
            onSave = {}
        )
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun EditAccountBottomSheetDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        EditAccountBottomSheet(
            account = Account(
                id = "1",
                name = "Основная карта",
                balance = 125000.0,
                currency = "RUB",
                type = AccountType.CARD
            ),
            onDismiss = {},
            onSave = {}
        )
    }
}