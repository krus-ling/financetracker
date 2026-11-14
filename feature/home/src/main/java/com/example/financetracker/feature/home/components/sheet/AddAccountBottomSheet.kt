package com.example.financetracker.feature.home.components.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.components.transaction.AppSegmentedControl
import com.example.financetracker.core.uikit.util.AmountFormatter
import com.example.financetracker.core.uikit.util.toAmountOrNull
import com.example.financetracker.feature.home.components.HomeTransactionRow
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun AddAccountBottomSheet(
    onDismiss: () -> Unit,
    onAddClick: (name: String, initialBalance: Double) -> Unit,
    onCheckDeletedAccount: (suspend (String) -> Account?)? = null,
    onRestoreDeletedAccount: ((String) -> Unit)? = null,
    onPurgeDeletedAccount: ((String) -> Unit)? = null,
    onGetAccountTransactions: (suspend (String) -> List<Transaction>)? = null,
    categories: List<Category> = emptyList()
) {
    val coroutineScope = rememberCoroutineScope()
    val nameFocusRequester = remember { FocusRequester() }

    var accountName by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    // Найденный в корзине счёт и выбранный режим
    var deletedAccount by remember { mutableStateOf<Account?>(null) }
    var deletedTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isRestoreSelected by remember { mutableStateOf(true) }

    val isNameValid = accountName.trim().isNotEmpty()
    val nameError = submitted && !isNameValid

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    AppFormBottomSheet(
        title = if (deletedAccount == null) "Новый счёт" else "Кажется, такой счёт уже был 🤔",
        primaryButtonText = if (deletedAccount == null) "Создать счёт" else "Готово",
        cancelButtonText = "Отмена",
        isPrimaryEnabled = if (deletedAccount != null && !isRestoreSelected) isNameValid else true,
        onPrimaryClick = {
            val currentDeleted = deletedAccount
            if (currentDeleted  == null) {
                // Обычный режим: проверяем имя в корзине
                submitted = true
                if (isNameValid) {
                    val balance = balanceText.toAmountOrNull() ?: 0.0
                    val trimmedName = accountName.trim()

                    coroutineScope.launch {
                        val found = onCheckDeletedAccount?.invoke(trimmedName)
                        if (found != null) {
                            // Нашли в корзине - показываем диалог
                            val txs = onGetAccountTransactions?.invoke(found.id) ?: emptyList()
                            deletedTransactions = txs
                            deletedAccount = found
                            isRestoreSelected = true
                        } else {
                            // Не нашли - создаем счет как обычно
                            onAddClick(trimmedName, balance)
                            onDismiss()
                        }
                    }
                }
                false
            } else {
                // Режим найденного в корзине счёта: обрабатываем выбор
                if (isRestoreSelected) {
                    onRestoreDeletedAccount?.invoke(currentDeleted.id)
                } else {
                    val balance = balanceText.toAmountOrNull() ?: 0.0
                    onPurgeDeletedAccount?.invoke(currentDeleted.id)
                    onAddClick(accountName.trim(), balance)
                }
                deletedAccount = null
                true // плавно закрываем шторку
            }
        },
        onDismiss = onDismiss,
        onSheetOpened = {
            if (deletedAccount == null) nameFocusRequester.requestFocus()
        },
        modifier = if (deletedAccount != null) Modifier.fillMaxHeight() else Modifier,
    ) { clearFocus ->
        val deleted = deletedAccount

        // 1. Чипы переключения (только если счёт найден в корзине)
        if (deleted != null) {
            AppSegmentedControl(
                items = remember { listOf(true, false) },
                selectedItem = isRestoreSelected,
                onItemSelected = { isRestoreSelected = it },
                label = { if (it) "Восстановить старый" else "Создать новый" }
            )
        }

        when {
            // РЕЖИМ 1: Восстановление счёта из корзины
            deleted != null && isRestoreSelected -> {
                // Заблокированное название
                OutlinedTextField(
                    value = deleted.name,
                    onValueChange = {},
                    label = { Text("Название счёта") },
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Заблокировано",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                // Заблокированный баланс из корзины
                AmountInputField(
                    value = AmountFormatter.formatForInput(deleted.balance),
                    onValueChange = {},
                    label = "Баланс из корзины",
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Заблокировано",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // История операций
                Text(
                    text = if (deletedTransactions.isNotEmpty()) {
                        "Последние операции (${deletedTransactions.size}):"
                    } else {
                        "Операций по счёту не найдено"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (deletedTransactions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            deletedTransactions.forEach { tx ->
                                val cat = categoriesMap[tx.categoryId]
                                HomeTransactionRow(
                                    transaction = tx,
                                    category = cat,
                                    account = deleted,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
            }
            // РЕЖИМ 2: Обычное создание или ввод нового счёта
            else -> {
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

                AmountInputField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = "Начальный баланс",
                    onDone = clearFocus
                )

                // Если перезаписываем старый счёт — предупреждаем пользователя
                if (deleted != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Старый счёт «${deleted.name}» будет безвозвратно удалён из корзины",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
