package com.example.financetracker.feature.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet

@ExperimentalMaterial3Api
@Composable
fun AddAccountBottomSheet(
    onDismiss: () -> Unit,
    onAddClick: (name: String, initialBalance: Double) -> Unit
) {
    val nameFocusRequester = remember { FocusRequester() }

    var accountName by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val isNameValid = accountName.trim().isNotEmpty()
    val nameError = submitted && !isNameValid

    AppFormBottomSheet(
        title = "Новый счёт",
        primaryButtonText = "Создать счёт",
        onPrimaryClick = {
            submitted = true
            if (isNameValid) {
                val balance = balanceText.replace(',', '.').toDoubleOrNull() ?: 0.0
                onAddClick(accountName.trim(), balance)
                true
            } else {
                false
            }
        },
        onDismiss = onDismiss,
        onSheetOpened = { nameFocusRequester.requestFocus() }
    ) { clearFocus ->
        // Название счёта
        OutlinedTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text("Название счёта") },
            placeholder = { Text("Например, Наличные") },
            singleLine = true,
            isError = nameError,
            supportingText = if (nameError) {
                { Text("Введите название счёта") }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocusRequester)
        )

        // Начальный баланс
        AmountInputField(
            value = balanceText,
            onValueChange = { balanceText = it },
            label = "Начальный баланс",
            onDone = clearFocus
        )
    }
}
