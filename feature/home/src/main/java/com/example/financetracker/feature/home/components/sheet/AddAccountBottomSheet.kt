package com.example.financetracker.feature.home.components.sheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.util.toAmountOrNull
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun AddAccountBottomSheet(
    onDismiss: () -> Unit,
    onAddClick: (name: String, initialBalance: Double) -> Unit,
    onCheckDeletedAccount: (suspend (String) -> Account?)? = null,
    onRestoreDeletedAccount: ((String) -> Unit)? = null,
    onPurgeDeletedAccount: ((String) -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    var existingDeletedAccount by remember { mutableStateOf<Account?>(null) }

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
                val balance = balanceText.toAmountOrNull() ?: 0.0
                val trimmedName = accountName.trim()

                coroutineScope.launch {
                    val deleted = onCheckDeletedAccount?.invoke(trimmedName)
                    if (deleted != null) {
                        // Нашли в корзине - показываем диалог
                        existingDeletedAccount = deleted
                    } else {
                        // Не нашли - создаем счет как обычно
                        onAddClick(trimmedName, balance)
                        onDismiss()
                    }
                }
                false // Не закрываем шторку сразу, ждём результат проверки
            } else
                false
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

    existingDeletedAccount?.let { deleted ->
        AlertDialog(
            onDismissRequest = { existingDeletedAccount = null },
            title = { Text("Кажется, такое название уже где-то встречалось... 🤔️") },
            text = {
                Text("Счёт «${deleted.name}» находится в корзине. Восстановить его со всей историей операций или создать новый?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreDeletedAccount?.invoke(deleted.id)
                        existingDeletedAccount = null
                        onDismiss()
                    }
                ) {
                    Text("Восстановить старый")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val balance = balanceText.toAmountOrNull() ?: 0.0
                        onPurgeDeletedAccount?.invoke(deleted.id) // удаляем старый счёт из корзины навсегда
                        onAddClick(accountName.trim(), balance)
                        existingDeletedAccount = null
                        onDismiss()
                    }
                ) {
                    Text("Создать новый")
                }
            }
        )
    }
}
