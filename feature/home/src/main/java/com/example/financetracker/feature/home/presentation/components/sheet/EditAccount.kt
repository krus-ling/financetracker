package com.example.financetracker.feature.home.presentation.components.sheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.uikit.components.account.AccountTypeSelector
import com.example.financetracker.core.uikit.components.account.DefaultAccountSwitchRow
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.input.AppTextField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.util.AmountFormatter
import com.example.financetracker.core.uikit.util.toAmountOrNull

/** Максимальная длина названия счёта */
private const val MaxAccountNameLength = 30

/** Отображаемые названия поддерживаемых валют */
private val CurrencyDisplayTitles = mapOf(
    "RUB" to "RUB · Российский рубль",
    "USD" to "USD · Доллар США",
    "EUR" to "EUR · Евро",
)

// =========================================================================
// 1. ОСНОВНЫЕ КОМПОНЕНТЫ ЭКРАНА (ENTRY POINTS)
// =========================================================================

/**
 * Модальная шторка редактирования существующего счёта.
 *
 * Позволяет изменить название (до 30 символов), тип счёта, текущий баланс (от 0)
 * и статус «по умолчанию». Валюта счёта заблокирована для редактирования.
 * Кнопка сохранения активна только при валидных полях и наличии изменений.
 *
 * @param account Текущая модель редактируемого счёта.
 * @param onDismiss Закрытие шторки без сохранения.
 * @param onSave Сохранение обновлённой модели счёта.
 */
@Composable
fun EditAccountBottomSheet(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    EditAccountBottomSheetContent(
        account = account,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

/**
 * Внутренняя презентационная реализация шторки редактирования счёта (используется и в Preview).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountBottomSheetContent(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit,
    initialName: String = account.name,
    initialBalance: String = AmountFormatter.formatForInput(account.balance),
) {
    // Поля формы (сохраняются при повороте экрана)
    var name by rememberSaveable(account.id) { mutableStateOf(initialName) }
    var type by rememberSaveable(account.id) { mutableStateOf(account.type) }
    var balance by rememberSaveable(account.id) { mutableStateOf(initialBalance) }
    var isDefault by rememberSaveable(account.id) { mutableStateOf(account.isDefault) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Валидация и вычисляемые значения
    val validBalance = remember(balance) {
        balance.toAmountOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
    }
    val trimmedName = name.trim()
    val isNameValid = trimmedName.isNotEmpty()
    val isBalanceValid = validBalance != null
    val hasChanges = trimmedName != account.name.trim() ||
            type != account.type ||
            validBalance != account.balance ||
            isDefault != account.isDefault
    val canSave = isNameValid && isBalanceValid && hasChanges && !isSubmitting

    val currencyDisplay = remember(account.currency) {
        getCurrencyDisplayTitle(account.currency)
    }

    AppFormBottomSheet(
        title = "Редактирование счёта",
        primaryButtonText = "Сохранить",
        isPrimaryEnabled = canSave,
        onPrimaryClick = {
            if (!canSave) {
                false
            } else {
                isSubmitting = true
                onSave(
                    account.copy(
                        name = trimmedName,
                        type = type,
                        balance = validBalance,
                        isDefault = isDefault
                    )
                )
                true
            }
        },
        onDismiss = onDismiss,
    ) { clearFocus ->
        // Название счёта
        AppTextField(
            value = name,
            onValueChange = { if (it.length <= MaxAccountNameLength) name = it },
            label = "Название",
            placeholder = "Например, Основная карта",
            isError = !isNameValid,
            supportingText = {
                if (!isNameValid) {
                    Text("Название не может быть пустым")
                } else {
                    Text("${name.length}/$MaxAccountNameLength")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        // Тип счёта
        AccountTypeSelector(
            selectedType = type,
            onTypeSelected = { type = it }
        )

        // Валюта (заблокирована для редактирования)
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

        // Текущий баланс
        AmountInputField(
            value = balance,
            onValueChange = { balance = it },
            label = "Текущий баланс",
            isError = !isBalanceValid,
            errorMessage = "Укажите баланс, например 0",
            onDone = clearFocus,
            modifier = Modifier.fillMaxWidth()
        )

        // Статус "по умолчанию"
        DefaultAccountSwitchRow(
            isDefault = isDefault,
            onCheckedChange = { isDefault = it }
        )
    }
}

/**
 * Возвращает локализованное отображаемое название валюты по её ISO-коду.
 */
private fun getCurrencyDisplayTitle(currency: String): String {
    val normalized = currency.uppercase()
    return CurrencyDisplayTitles[normalized] ?: "$normalized · Валюта"
}

// =========================================================================
// 2. PREVIEWS & ТЕСТОВЫЕ ДАННЫЕ
// =========================================================================

private val previewEditAccount = Account(
    id = "1",
    name = "Основная карта",
    balance = 125000.0,
    currency = "RUB",
    type = AccountType.CARD,
    isDefault = true
)
@Preview(name = "1. Без изменений - Светлая", showBackground = true)
@Composable
private fun EditAccountBottomSheetPreview() {
    FinancetrackerTheme(darkTheme = false) {
        EditAccountBottomSheetContent(
            account = previewEditAccount,
            onDismiss = {},
            onSave = {}
        )
    }
}
@Preview(name = "2. Без изменений - Тёмная", showBackground = true)
@Composable
private fun EditAccountBottomSheetDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        EditAccountBottomSheetContent(
            account = previewEditAccount.copy(isDefault = false),
            onDismiss = {},
            onSave = {}
        )
    }
}
@Preview(name = "3. Ошибки валидации", showBackground = true)
@Composable
private fun EditAccountValidationErrorsPreview() {
    FinancetrackerTheme(darkTheme = false) {
        EditAccountBottomSheetContent(
            account = previewEditAccount,
            initialName = "",
            initialBalance = ".",
            onDismiss = {},
            onSave = {}
        )
    }
}
@Preview(name = "4. Изменён счёт (USD) - Светлая", showBackground = true)
@Composable
private fun EditAccountModifiedUsdPreview() {
    FinancetrackerTheme(darkTheme = false) {
        EditAccountBottomSheetContent(
            account = previewEditAccount.copy(
                name = "Валютный",
                currency = "USD",
                type = AccountType.SAVINGS,
                isDefault = false
            ),
            initialName = "Долларовая копилка",
            initialBalance = "1500",
            onDismiss = {},
            onSave = {}
        )
    }
}