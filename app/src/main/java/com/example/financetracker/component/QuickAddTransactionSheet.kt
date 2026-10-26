package com.example.financetracker.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTransactionSheet(onDismiss: () -> Unit, onAddClick: (amount: Double, title: String) -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val amountError = submitted && (amount == null || !amount.isFinite() || amount <= 0.0)
    val titleError = submitted && title.isBlank()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Новая операция", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' || char == ',' } },
                label = { Text("Сумма") }, placeholder = { Text("Например, 1250,50") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                supportingText = if (amountError) ({ Text("Введите сумму больше нуля") }) else null,
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Название или категория") }, placeholder = { Text("Например, продукты") },
                isError = titleError,
                supportingText = if (titleError) ({ Text("Укажите название операции") }) else null,
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                submitted = true
                if (amount != null && amount.isFinite() && amount > 0.0 && title.isNotBlank()) {
                    onAddClick(amount, title.trim())
                    onDismiss()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Добавить") }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Отмена") }
        }
    }
}
