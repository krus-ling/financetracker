package com.example.financetracker.feature.home.components.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.input.AppTextField
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
    categories: List<Category> = emptyList(),
    deletedAccounts: List<Account> = emptyList(),
    onOpenAccountDetails: ((String) -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val nameFocusRequester = remember { FocusRequester() }

    var accountName by rememberSaveable { mutableStateOf("") }
    var balanceText by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    // Запоминаем имя найденного в корзине счёта:
    var deletedAccountName by rememberSaveable { mutableStateOf<String?>(null) }
    var isNavigatingToDetails by rememberSaveable { mutableStateOf(false) }

    // Найденный в корзине счёт и выбранный режим
    var deletedAccount by remember { mutableStateOf<Account?>(null) }
    var deletedTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isRestoreSelected by rememberSaveable { mutableStateOf(true) }

    val currentDeleted = deletedAccount ?: deletedAccounts.find {
        it.name.trim().equals(deletedAccountName?.trim(), ignoreCase = true)
    }

    val isNameValid = accountName.trim().isNotEmpty()
    val nameError = submitted && !isNameValid

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    LaunchedEffect(deletedAccountName) {
        val name = deletedAccountName
        if (name != null && deletedAccount == null) {
            val found = onCheckDeletedAccount?.invoke(name)
            if (found != null) {
                deletedAccount = found
                deletedTransactions = onGetAccountTransactions?.invoke(found.id) ?: emptyList()
            }
        }
    }

    LaunchedEffect(currentDeleted?.id) {
        val id = currentDeleted?.id
        if (id != null && deletedTransactions.isEmpty()) {
            deletedTransactions = onGetAccountTransactions?.invoke(id) ?: emptyList()
        }
    }

    AppFormBottomSheet(
        title = if (currentDeleted == null) "Новый счёт" else "Кажется, такой счёт уже был 🤔",
        primaryButtonText = if (currentDeleted == null) "Создать счёт" else "Готово",
        cancelButtonText = "Отмена",
        isPrimaryEnabled = if (currentDeleted != null && !isRestoreSelected) isNameValid else true,
        onPrimaryClick = {
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
                            deletedAccountName = found.name
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
        onDismiss = {
            if (!isNavigatingToDetails) {
                accountName = ""
                balanceText = ""
                submitted = false
                deletedAccount = null
                deletedAccountName = null
                deletedTransactions = emptyList()
                isRestoreSelected = true
            }
            isNavigatingToDetails = false
            onDismiss()
        },
        onSheetOpened = {
            if (currentDeleted == null) nameFocusRequester.requestFocus()
        },
        modifier = if (currentDeleted != null) Modifier.fillMaxHeight() else Modifier,
    ) { clearFocus ->

        // 1. Чипы переключения (только если счёт найден в корзине)
        if (currentDeleted != null) {
            AppSegmentedControl(
                items = remember { listOf(true, false) },
                selectedItem = isRestoreSelected,
                onItemSelected = { isRestoreSelected = it },
                label = { if (it) "Восстановить старый" else "Создать новый" }
            )
        }

        when {
            // РЕЖИМ 1: Восстановление счёта из корзины
            currentDeleted != null && isRestoreSelected -> {
                // Заблокированное название
                AppTextField(
                    value = currentDeleted.name,
                    onValueChange = {},
                    label = "Название счёта",
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

                // Заблокированный баланс из корзины
                AmountInputField(
                    value = AmountFormatter.formatForInput(currentDeleted.balance),
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
                        "Последние операции:"
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
                            deletedTransactions.take(5).forEach { tx ->
                                val cat = categoriesMap[tx.categoryId]
                                HomeTransactionRow(
                                    transaction = tx,
                                    category = cat,
                                    account = currentDeleted,
                                    onClick = {}
                                )
                            }

                            if (onOpenAccountDetails != null) {

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isNavigatingToDetails = true
                                            onOpenAccountDetails(currentDeleted.id)
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Все операции (${deletedTransactions.size})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(Modifier.width(4.dp))

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // РЕЖИМ 2: Обычное создание или ввод нового счёта
            else -> {
                AppTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = "Название счёта",
                    placeholder = "Например, Наличные",
                    isError = nameError,
                    errorMessage = "Введите название счёта",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    focusRequester = nameFocusRequester
                )

                AmountInputField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = "Начальный баланс",
                    onDone = clearFocus
                )

                // Если перезаписываем старый счёт — предупреждаем пользователя
                if (currentDeleted != null) {
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
                                text = "Старый счёт «${currentDeleted.name}» будет безвозвратно удалён из корзины",
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
