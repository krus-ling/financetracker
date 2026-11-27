package com.example.financetracker.feature.home.presentation.components.sheet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.account.AccountBalanceCard
import com.example.financetracker.core.uikit.components.account.AccountTypeSelector
import com.example.financetracker.core.uikit.components.account.DeletedAccountBanner
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.input.AppTextField
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.components.sheet.LocalAppFormSheetDismiss
import com.example.financetracker.core.uikit.components.transaction.AppSegmentedControl
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.util.AmountFormatter
import com.example.financetracker.core.uikit.util.toAmountOrNull
import com.example.financetracker.feature.home.presentation.components.HomeTransactionRow
import com.example.financetracker.feature.home.presentation.components.TransactionDayGroupView
import com.example.financetracker.feature.home.util.groupTransactionByDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

/** Максимальная длина названия счёта */
private const val MaxAccountNameLength = 30

/** Количество последних операций в карточке предпросмотра счёта из корзины */
private const val RecentDeletedTransactionsLimit = 5

/** Потокобезопасный форматтер даты удаления счёта */
private val DeletedAccountDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

// =========================================================================
// 1. ОСНОВНЫЕ КОМПОНЕНТЫ ЭКРАНА (ENTRY POINTS)
// =========================================================================

/**
 * Модальная шторка создания нового счёта с проверкой совпадений в корзине.
 *
 * Поддерживает три режима:
 * 1. **Создание счёта** — ввод названия (до 30 символов), выбор типа, начального баланса и флага
 *    «по умолчанию»;
 * 2. **Режим «Дежавю»** — если в корзине найден удалённый счёт с тем же именем, предлагает
 *    восстановить его (с предпросмотром операций) либо безвозвратно заменить новым;
 * 3. **Просмотр операций** — полная история операций удалённого счёта с группировкой по дням.
 *
 * @param onDismiss Закрытие шторки.
 * @param onAddClick Сохранение нового счёта.
 * @param onCheckDeletedAccount Асинхронный поиск удалённого счёта по имени в корзине.
 * @param onRestoreDeletedAccount Восстановление выбранного счёта из корзины по `id`.
 * @param onPurgeDeletedAccount Безвозвратное удаление счёта из корзины при перезаписи тем же именем.
 * @param onGetAccountTransactions Загрузчик операций счёта для предпросмотра и полной истории.
 * @param categories Справочник категорий для отображения строк операций.
 * @param accounts Активные счета (используются для отображения маршрутов переводов в корзине).
 * @param deletedAccounts Список удалённых счетов из состояния экрана.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccount(
    onDismiss: () -> Unit,
    onAddClick: (name: String, initialBalance: Double, isDefault: Boolean, type: AccountType) -> Unit,
    onCheckDeletedAccount: (suspend (String) -> Account?)? = null,
    onRestoreDeletedAccount: ((String) -> Unit)? = null,
    onPurgeDeletedAccount: ((String) -> Unit)? = null,
    onGetAccountTransactions: (suspend (String) -> List<Transaction>)? = null,
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    deletedAccounts: List<Account> = emptyList(),
) {
    AddAccountContent(
        onDismiss = onDismiss,
        onAddClick = onAddClick,
        onCheckDeletedAccount = onCheckDeletedAccount,
        onRestoreDeletedAccount = onRestoreDeletedAccount,
        onPurgeDeletedAccount = onPurgeDeletedAccount,
        onGetAccountTransactions = onGetAccountTransactions,
        categories = categories,
        accounts = accounts,
        deletedAccounts = deletedAccounts
    )
}

/**
 * Внутренняя презентационная реализация шторки создания счёта (используется и в Preview).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountContent(
    onDismiss: () -> Unit,
    onAddClick: (name: String, initialBalance: Double, isDefault: Boolean, type: AccountType) -> Unit,
    onCheckDeletedAccount: (suspend (String) -> Account?)? = null,
    onRestoreDeletedAccount: ((String) -> Unit)? = null,
    onPurgeDeletedAccount: ((String) -> Unit)? = null,
    onGetAccountTransactions: (suspend (String) -> List<Transaction>)? = null,
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    deletedAccounts: List<Account> = emptyList(),
    initialDeletedAccount: Account? = null,
    initialIsViewingAllTransactions: Boolean = false,
    initialIsRestoreSelected: Boolean = true,
    initialAccountName: String = "",
    initialBalanceText: String = "",
    initialSubmitted: Boolean = false,
    initialDeletedTransactions: List<Transaction> = emptyList(),
) {
    val state = rememberAddAccountState(
        categories = categories,
        accounts = accounts,
        deletedAccounts = deletedAccounts,
        initialDeletedAccount = initialDeletedAccount,
        initialIsViewingAllTransactions = initialIsViewingAllTransactions,
        initialIsRestoreSelected = initialIsRestoreSelected,
        initialAccountName = initialAccountName,
        initialBalanceText = initialBalanceText,
        initialSubmitted = initialSubmitted,
        initialDeletedTransactions = initialDeletedTransactions,
        onAddClick = onAddClick,
        onCheckDeletedAccount = onCheckDeletedAccount,
        onRestoreDeletedAccount = onRestoreDeletedAccount,
        onPurgeDeletedAccount = onPurgeDeletedAccount,
        onGetAccountTransactions = onGetAccountTransactions,
    )

    val currentDeleted = state.currentDeleted

    AppFormBottomSheet(
        title = when {
            state.isViewingAllTransactions && currentDeleted != null -> currentDeleted.name
            currentDeleted != null -> "Кажется, такой счёт уже был 🤔"
            else -> "Новый счёт"
        },
        titleLeadingContent = if (state.isViewingAllTransactions) {
            {
                IconButton(
                    onClick = { state.isViewingAllTransactions = false },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        titleTrailingContent = null,
        containerColor = if (state.isViewingAllTransactions) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surface
        },
        primaryButtonText = when {
            state.isViewingAllTransactions -> "Назад к счёту"
            currentDeleted == null -> "Создать счёт"
            else -> "Готово"
        },
        cancelButtonText = if (state.isViewingAllTransactions) null else "Отмена",
        isPrimaryEnabled = !state.isSubmitting && when {
            state.isViewingAllTransactions -> true
            currentDeleted != null && !state.isRestoreSelected -> state.isNameValid && state.isBalanceValid
            else -> true
        },
        onPrimaryClick = state::handlePrimaryClick,
        onDismiss = onDismiss,
        onSheetOpened = {
            if (state.currentDeleted == null) {
                state.nameFocusRequester.requestFocus()
            }
        },
        modifier = if (currentDeleted != null) Modifier.fillMaxHeight() else Modifier,
    ) { clearFocus ->
        val dismissSheet = LocalAppFormSheetDismiss.current

        SideEffect {
            state.requestDismiss = dismissSheet
        }

        // Перехватываем «Назад» внутри окна шторки, предотвращая стягивание Predictive Back
        BackHandler(enabled = state.isViewingAllTransactions) {
            state.isViewingAllTransactions = false
        }

        if (state.isViewingAllTransactions && currentDeleted != null) {

            // Полная история операций счёта из корзины
            DeletedAccountTransactionsContent(
                account = currentDeleted,
                transactions = state.deletedTransactions,
                categoriesMap = state.categoriesMap,
                accountsMap = state.accountsMap
            )
        } else {
            // Переключатель режима (только если счёт найден в корзине)
            if (currentDeleted != null) {
                AppSegmentedControl(
                    items = remember { listOf(true, false) },
                    selectedItem = state.isRestoreSelected,
                    onItemSelected = { state.isRestoreSelected = it },
                    label = { if (it) "Восстановить старый" else "Создать новый" }
                )
            }

            if (currentDeleted != null && state.isRestoreSelected) {
                // РЕЖИМ 1: Восстановление счета из корзины
                DeletedAccountRestoreSection(
                    account = currentDeleted,
                    transactions = state.deletedTransactions,
                    isLoadingTransactions = state.isLoadingTransactions,
                    categoriesMap = state.categoriesMap,
                    accountsMap = state.accountsMap,
                    onViewAllTransactions = { state.isViewingAllTransactions = true }
                )
            } else {
                // РЕЖИМ 2: Создание нового счёта (или ввод имени заново поверх счёта из корзины)
                NewAccountFormSection(
                    accountName = state.accountName,
                    isNameError = state.nameError,
                    selectedType = state.selectedType,
                    balanceText = state.balanceText,
                    isBalanceError = state.balanceError,
                    isDefault = state.isDefault,
                    nameFocusRequester = state.nameFocusRequester,
                    replacingDeletedAccountName = if (currentDeleted != null && state.isReplacingDeletedAccount) {
                        currentDeleted.name
                    } else null,
                    onNameChange = { if (it.length <= MaxAccountNameLength) state.accountName = it },
                    onTypeSelected = { state.selectedType = it },
                    onBalanceChange = { state.balanceText = it },
                    onDefaultChange = { state.isDefault = it },
                    onDone = clearFocus
                )
            }
        }
    }
}

// =========================================================================
// 2. СОСТОЯНИЕ ФОРМЫ (STATE HOLDER)
// =========================================================================

/**
 * Создаёт и синхронизирует [AddAccountState] для шторки [AddAccount].
 */
@Composable
private fun rememberAddAccountState(
    categories: List<Category>,
    accounts: List<Account>,
    deletedAccounts: List<Account>,
    initialDeletedAccount: Account?,
    initialIsViewingAllTransactions: Boolean,
    initialIsRestoreSelected: Boolean,
    initialAccountName: String,
    initialBalanceText: String,
    initialSubmitted: Boolean,
    initialDeletedTransactions: List<Transaction>,
    onAddClick: (name: String, initialBalance: Double, isDefault: Boolean, type: AccountType) -> Unit,
    onCheckDeletedAccount: (suspend (String) -> Account?)?,
    onRestoreDeletedAccount: ((String) -> Unit)?,
    onPurgeDeletedAccount: ((String) -> Unit)?,
    onGetAccountTransactions: (suspend (String) -> List<Transaction>)?,
): AddAccountState {
    val coroutineScope = rememberCoroutineScope()
    val nameFocusRequester = remember { FocusRequester() }

    val accountNameState = rememberSaveable { mutableStateOf(initialAccountName) }
    val balanceTextState = rememberSaveable { mutableStateOf(initialBalanceText) }
    val submittedState = rememberSaveable { mutableStateOf(initialSubmitted) }
    val isDefaultState = rememberSaveable { mutableStateOf(false) }
    val selectedTypeState = rememberSaveable { mutableStateOf(AccountType.CARD) }
    val deletedAccountNameState = rememberSaveable { mutableStateOf<String?>(null) }
    val isRestoreSelectedState = rememberSaveable { mutableStateOf(initialIsRestoreSelected) }
    val isViewingAllTransactionsState = rememberSaveable {
        mutableStateOf(initialIsViewingAllTransactions)
    }


    val state = remember {
        AddAccountState(
            coroutineScope = coroutineScope,
            nameFocusRequester = nameFocusRequester,
            accountNameState = accountNameState,
            balanceTextState = balanceTextState,
            submittedState = submittedState,
            isDefaultState = isDefaultState,
            selectedTypeState = selectedTypeState,
            deletedAccountNameState = deletedAccountNameState,
            isRestoreSelectedState = isRestoreSelectedState,
            isViewingAllTransactionsState = isViewingAllTransactionsState,
            initialDeletedAccount = initialDeletedAccount,
            initialCategories = categories,
            initialAccounts = accounts,
            initialDeletedAccounts = deletedAccounts,
            initialDeletedTransactions = initialDeletedTransactions,
            onAddClick = onAddClick,
            onCheckDeletedAccount = onCheckDeletedAccount,
            onRestoreDeletedAccount = onRestoreDeletedAccount,
            onPurgeDeletedAccount = onPurgeDeletedAccount,
            onGetAccountTransactions = onGetAccountTransactions,
        )
    }

    state.categories = categories
    state.accounts = accounts
    state.deletedAccounts = deletedAccounts
    state.onAddClick = onAddClick
    state.onCheckDeletedAccount = onCheckDeletedAccount
    state.onRestoreDeletedAccount = onRestoreDeletedAccount
    state.onPurgeDeletedAccount = onPurgeDeletedAccount
    state.onGetAccountTransactions = onGetAccountTransactions

    // После поворота экрана восстанавливаем найденный удалённый счёт из БД, если он не передан напрямую
    LaunchedEffect(state.deletedAccountName) {
        state.restoreDeletedAccountAfterRecreation()
    }

    // Загружаем операции выбранного счёта из корзины при его появлении
    LaunchedEffect(state.currentDeleted?.id) {
        state.loadDeletedTransactionsIfNeeded()
    }

    return state
}

/**
 * Держатель состояния и бизнес-логики формы создания счёта, поиска в корзине
 * и просмотра истории удалённого счёта.
 */
@Stable
private class AddAccountState(
    private val coroutineScope: CoroutineScope,
    val nameFocusRequester: FocusRequester,
    private val accountNameState: MutableState<String>,
    private val balanceTextState: MutableState<String>,
    private val submittedState: MutableState<Boolean>,
    private val isDefaultState: MutableState<Boolean>,
    private val selectedTypeState: MutableState<AccountType>,
    private val deletedAccountNameState: MutableState<String?>,
    private val isRestoreSelectedState: MutableState<Boolean>,
    private val isViewingAllTransactionsState: MutableState<Boolean>,
    initialDeletedAccount: Account?,
    initialCategories: List<Category>,
    initialAccounts: List<Account>,
    initialDeletedAccounts: List<Account>,
    initialDeletedTransactions: List<Transaction>,
    onAddClick: (String, Double, Boolean, AccountType) -> Unit,
    onCheckDeletedAccount: (suspend (String) -> Account?)?,
    onRestoreDeletedAccount: ((String) -> Unit)?,
    onPurgeDeletedAccount: ((String) -> Unit)?,
    onGetAccountTransactions: (suspend (String) -> List<Transaction>)?,
) {
    // --- Внешние данные и колбэки ---
    var categories by mutableStateOf(initialCategories)
    var accounts by mutableStateOf(initialAccounts)
    var deletedAccounts by mutableStateOf(initialDeletedAccounts)
    var onAddClick by mutableStateOf(onAddClick)
    var onCheckDeletedAccount by mutableStateOf(onCheckDeletedAccount)
    var onRestoreDeletedAccount by mutableStateOf(onRestoreDeletedAccount)
    var onPurgeDeletedAccount by mutableStateOf(onPurgeDeletedAccount)
    var onGetAccountTransactions by mutableStateOf(onGetAccountTransactions)
    var requestDismiss: () -> Unit = {}

    // --- Поля формы (переживают поворот экрана через rememberSaveable) ---
    var accountName: String
        get() = accountNameState.value
        set(value) { accountNameState.value = value }

    var balanceText: String
        get() = balanceTextState.value
        set(value) { balanceTextState.value = value }

    var submitted: Boolean
        get() = submittedState.value
        set(value) { submittedState.value = value }

    var isDefault: Boolean
        get() = isDefaultState.value
        set(value) { isDefaultState.value = value }

    var selectedType: AccountType
        get() = selectedTypeState.value
        set(value) { selectedTypeState.value = value }

    var deletedAccountName: String?
        get() = deletedAccountNameState.value
        set(value) { deletedAccountNameState.value = value }

    var isRestoreSelected: Boolean
        get() = isRestoreSelectedState.value
        set(value) { isRestoreSelectedState.value = value }

    var isViewingAllTransactions: Boolean
        get() = isViewingAllTransactionsState.value
        set(value) { isViewingAllTransactionsState.value = value }

    // --- Асинхронное состояние счёта из корзины и отправки ---
    private var deletedAccount by mutableStateOf(initialDeletedAccount)
    var deletedTransactions by mutableStateOf(initialDeletedTransactions)
        private set
    var isLoadingTransactions by mutableStateOf(false)
        private set
    var isSubmitting by mutableStateOf(false)
        private set
    private var loadedTransactionsAccountId: String? =
        initialDeletedAccount?.id?.takeIf { initialDeletedTransactions.isNotEmpty() }

    // --- Вычисляемые свойства ---
    val currentDeleted: Account? by derivedStateOf {
        deletedAccount ?: deletedAccountName?.trim()?.let { name ->
            deletedAccounts.find { it.name.trim().equals(name, ignoreCase = true) }
        }
    }

    val isNameValid: Boolean by derivedStateOf { accountName.trim().isNotEmpty() }

    val nameError: Boolean by derivedStateOf { submitted && !isNameValid }

    private val parsedBalance: Double? by derivedStateOf {
        if (balanceText.isBlank()) {
            0.0
        } else {
            balanceText.toAmountOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
        }
    }

    val isBalanceValid: Boolean by derivedStateOf { parsedBalance != null }

    val balanceError: Boolean by derivedStateOf { submitted && !isBalanceValid }

    val isReplacingDeletedAccount: Boolean by derivedStateOf {
        val deletedName = currentDeleted?.name?.trim() ?: return@derivedStateOf false
        accountName.trim().equals(deletedName, ignoreCase = true)
    }

    val categoriesMap: Map<String, Category> by derivedStateOf {
        categories.associateBy { it.id }
    }

    val accountsMap: Map<String, Account> by derivedStateOf {
        buildMap {
            accounts.forEach { put(it.id, it) }
            deletedAccounts.forEach { put(it.id, it) }
            currentDeleted?.let { put(it.id, it) }
        }
    }

    // --- Загрузка и обработка действий ---
    suspend fun restoreDeletedAccountAfterRecreation() {
        val name = deletedAccountName ?: return
        if (deletedAccount != null) return
        val found = onCheckDeletedAccount?.invoke(name) ?: return
        deletedAccount = found
    }

    suspend fun loadDeletedTransactionsIfNeeded() {
        val id = currentDeleted?.id
        if (id == null) {
            deletedTransactions = emptyList()
            loadedTransactionsAccountId = null
            isLoadingTransactions = false
            return
        }
        if (loadedTransactionsAccountId == id) return

        isLoadingTransactions = true
        try {
            deletedTransactions = onGetAccountTransactions?.invoke(id).orEmpty()
            loadedTransactionsAccountId = id
        } catch (e: CancellationException) {
            throw e
        } finally {
            isLoadingTransactions = false
        }
    }

    fun handlePrimaryClick(): Boolean {
        if (isSubmitting) return false

        if (isViewingAllTransactions) {
            isViewingAllTransactions = false
            return false
        }

        val deleted = currentDeleted
        return if (deleted == null) {
            submitted = true
            val validBalance = parsedBalance
            if (isNameValid && validBalance != null) {
                checkTrashAndCreateAccount(accountName.trim(), validBalance)
            }
            false
        } else if (isRestoreSelected) {
            isSubmitting = true
            onRestoreDeletedAccount?.invoke(deleted.id)
            true
        } else {
            submitted = true
            val validBalance = parsedBalance ?: return false
            if (!isNameValid) return false

            val trimmedName = accountName.trim()
            if (isReplacingDeletedAccount) {
                isSubmitting = true
                onPurgeDeletedAccount?.invoke(deleted.id)
                onAddClick(trimmedName, validBalance, isDefault, selectedType)
                true
            } else {
                // Пользователь изменил имя в «Создать новый» — не удаляем чужой счёт из корзины
                checkTrashAndCreateAccount(trimmedName, validBalance)
                false
            }
        }
    }

    private fun checkTrashAndCreateAccount(trimmedName: String, balance: Double) {
        isSubmitting = true
        coroutineScope.launch {
            try {
                val found = onCheckDeletedAccount?.invoke(trimmedName)
                if (found != null) {
                    val txs = onGetAccountTransactions?.invoke(found.id).orEmpty()
                    deletedTransactions = txs
                    loadedTransactionsAccountId = found.id
                    deletedAccount = found
                    deletedAccountName = found.name
                    isRestoreSelected = true
                } else {
                    onAddClick(trimmedName, balance, isDefault, selectedType)
                    requestDismiss()
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                isSubmitting = false
            }
        }
    }
}

// =========================================================================
// 3. ВСПОМОГАТЕЛЬНЫЕ ПОДКОМПОНЕНТЫ (SUB-COMPOSABLES)
// =========================================================================

/**
 * Экран полной истории операций удалённого счёта внутри шторки (режим «Все операции»).
 *
 * @param account Удалённый счёт, чьи операции просматриваются.
 * @param transactions Список операций по этому счёту.
 * @param categoriesMap Карта категорий по `id`.
 * @param accountsMap Карта всех счетов для разрешения отправителей и получателей переводов.
 */
@Composable
private fun DeletedAccountTransactionsContent(
    account: Account,
    transactions: List<Transaction>,
    categoriesMap: Map<String, Category>,
    accountsMap: Map<String, Account>
) {
    val deletedSubtitle = remember(account.deletedAt) {
        account.deletedAt?.let { millis ->
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            "Удалён: ${date.format(DeletedAccountDateFormatter)}"
        }
    }
    // Карточка баланса удалённого счёта
    AccountBalanceCard(
        title = "Текущий баланс · ${account.type.title}",
        balance = account.balance,
        subtitle = deletedSubtitle,
        trailingContent = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "В корзине",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    )
    // Информационный баннер корзины
    DeletedAccountBanner()
    // Заголовок ленты операций
    Text(
        text = "Операции",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    val groupedDays = remember(transactions) {
        groupTransactionByDay(transactions)
    }
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "По этому счёту пока нет операций",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        groupedDays.forEach { dayGroup ->
            // Передаём account = null, чтобы оба участника перевода разрешались из accountsMap
            TransactionDayGroupView(
                dayGroup = dayGroup,
                categoriesMap = categoriesMap,
                account = null,
                accountsMap = accountsMap,
                headerHorizontalPadding = 0.dp
            )
        }
    }
}

/**
 * Секция восстановления найденного в корзине счёта с карточкой последних операций.
 *
 * @param account Найденный в корзине счёт.
 * @param transactions Загруженные операции по счёту.
 * @param isLoadingTransactions Флаг текущей загрузки операций из БД.
 * @param categoriesMap Карта категорий по `id`.
 * @param accountsMap Карта всех счетов по `id`.
 * @param onViewAllTransactions Переход к полному списку операций счёта.
 */
@Composable
private fun DeletedAccountRestoreSection(
    account: Account,
    transactions: List<Transaction>,
    isLoadingTransactions: Boolean,
    categoriesMap: Map<String, Category>,
    accountsMap: Map<String, Account>,
    onViewAllTransactions: () -> Unit
) {
    val formattedBalance = remember(account.balance) {
        AmountFormatter.formatForInput(account.balance)
    }
    val recentTransactions = remember(transactions) {
        transactions.take(RecentDeletedTransactionsLimit)
    }
    // Заблокированное название
    AppTextField(
        value = account.name,
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
        value = formattedBalance,
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
    when {
        isLoadingTransactions -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        transactions.isEmpty() -> {
            Text(
                text = "Операций по счёту не найдено",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        else -> {
            Text(
                text = "Последние операции:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    recentTransactions.forEach { tx ->
                        HomeTransactionRow(
                            transaction = tx,
                            category = categoriesMap[tx.categoryId],
                            account = accountsMap[tx.accountId] ?: account,
                            targetAccount = accountsMap[tx.targetAccountId],
                            onClick = {}
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onViewAllTransactions)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Все операции (${transactions.size})",
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

/**
 * Форма ввода параметров нового счёта.
 *
 * @param accountName Введённое название счёта.
 * @param isNameError Показывать ли ошибку пустого названия.
 * @param selectedType Выбранный тип счёта.
 * @param balanceText Введённая строка начального баланса.
 * @param isBalanceError Показывать ли ошибку некорректного баланса.
 * @param isDefault Назначить ли счёт счётом по умолчанию.
 * @param nameFocusRequester Контроллер фокуса для поля названия.
 * @param replacingDeletedAccountName Имя счёта из корзины, который будет удалён при перезаписи, или `null`.
 * @param onNameChange Изменение названия счёта.
 * @param onTypeSelected Выбор типа счёта.
 * @param onBalanceChange Изменение текста баланса.
 * @param onDefaultChange Переключение флага «по умолчанию».
 * @param onDone Действие по кнопке «Готово» на клавиатуре.
 */
@Composable
private fun NewAccountFormSection(
    accountName: String,
    isNameError: Boolean,
    selectedType: AccountType,
    balanceText: String,
    isBalanceError: Boolean,
    isDefault: Boolean,
    nameFocusRequester: FocusRequester,
    replacingDeletedAccountName: String?,
    onNameChange: (String) -> Unit,
    onTypeSelected: (AccountType) -> Unit,
    onBalanceChange: (String) -> Unit,
    onDefaultChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    AppTextField(
        value = accountName,
        onValueChange = onNameChange,
        label = "Название счёта",
        placeholder = "Например, Наличные",
        isError = isNameError,
        supportingText = {
            if (isNameError) {
                Text("Введите название счёта")
            } else {
                Text("${accountName.length}/$MaxAccountNameLength")
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        focusRequester = nameFocusRequester
    )
    AccountTypeSelector(
        selectedType = selectedType,
        onTypeSelected = onTypeSelected
    )
    AmountInputField(
        value = balanceText,
        onValueChange = onBalanceChange,
        label = "Начальный баланс",
        isError = isBalanceError,
        errorMessage = "Введите число больше нуля",
        onDone = onDone
    )
    DefaultAccountSwitchRow(
        isDefault = isDefault,
        onCheckedChange = onDefaultChange
    )
    if (!replacingDeletedAccountName.isNullOrBlank()) {
        PurgeDeletedAccountWarningBanner(accountName = replacingDeletedAccountName)
    }
}

/**
 * Строка с переключателем статуса «Счёт по умолчанию».
 */
@Composable
private fun DefaultAccountSwitchRow(
    isDefault: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isDefault) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Счёт по умолчанию",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Будет автоматически выбираться для новых операций",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = isDefault,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

/**
 * Предупреждающий баннер о безвозвратном удалении счёта из корзины при создании счёта с тем же именем.
 */
@Composable
private fun PurgeDeletedAccountWarningBanner(accountName: String) {
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
                text = "Старый счёт «$accountName» будет безвозвратно удалён из корзины",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

// =========================================================================
// 4. PREVIEWS & ТЕСТОВЫЕ ДАННЫЕ
// =========================================================================

private val previewCategories = listOf(
    Category(id = "cat1", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "ic_category_groceries"),
    Category(id = "cat2", name = "Одежда", type = CategoryType.EXPENSE, iconRes = "ic_category_clothing"),
    Category(id = "cat3", name = "Зарплата", type = CategoryType.INCOME, iconRes = "ic_category_salary")
)

private val previewDeletedAccount = Account(
    id = "del_1",
    name = "наличка",
    balance = 4000.0,
    type = AccountType.CASH,
    isDeleted = true,
    deletedAt = System.currentTimeMillis() - 86400000L * 3
)

private val previewActiveAccount = Account(
    id = "acc_1",
    name = "Основная карта",
    balance = 50000.0
)

private val previewTransactions = listOf(
    Transaction(
        id = "tx1",
        amount = 500.0,
        type = TransactionType.EXPENSE,
        categoryId = "cat2",
        accountId = "del_1",
        dateTime = System.currentTimeMillis(),
        note = "Футболка"
    ),
    Transaction(
        id = "tx2",
        amount = 500.0,
        type = TransactionType.EXPENSE,
        categoryId = "cat1",
        accountId = "del_1",
        dateTime = System.currentTimeMillis() - 86400000L,
        note = "Магазин"
    ),
    Transaction(
        id = "tx3",
        amount = 2000.0,
        type = TransactionType.TRANSFER,
        categoryId = "",
        accountId = "acc_1",
        targetAccountId = "del_1",
        dateTime = System.currentTimeMillis() - 86400000L * 2,
        note = "Снятие наличных"
    )
)

// 1. Обычное создание нового счёта (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "1. Новый счёт - Светлая", showBackground = true)
@Composable
private fun AddAccountNewPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> }
        )
    }
}

// 2. Обычное создание нового счёта (Тёмная тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "2. Новый счёт - Тёмная", showBackground = true)
@Composable
private fun AddAccountNewDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> }
        )
    }
}

// 3. Ошибки валидации названия и баланса (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "3. Ошибки валидации", showBackground = true)
@Composable
private fun AddAccountValidationErrorsPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> },
            initialAccountName = "",
            initialBalanceText = ".",
            initialSubmitted = true
        )
    }
}

// 4. Режим Дежавю: восстановление с историей операций и переводом
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "4. Дежавю: Восстановить (с операциями)", showBackground = true)
@Composable
private fun AddAccountDejaVuPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            initialDeletedTransactions = previewTransactions,
            categories = previewCategories,
            accounts = listOf(previewActiveAccount),
            onGetAccountTransactions = { previewTransactions }
        )
    }
}

// 5. Режим Дежавю: восстановление без операций
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "5. Дежавю: Восстановить (пусто)", showBackground = true)
@Composable
private fun AddAccountDejaVuEmptyPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            initialDeletedTransactions = emptyList(),
            categories = previewCategories,
            accounts = listOf(previewActiveAccount)
        )
    }
}

// 6. Режим Дежавю: перезапись («Создать новый» с баннером удаления из корзины)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "6. Дежавю: Перезапись (баннер)", showBackground = true)
@Composable
private fun AddAccountDejaVuOverwritePreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            initialIsRestoreSelected = false,
            initialAccountName = "наличка",
            initialBalanceText = "1000",
            accounts = listOf(previewActiveAccount)
        )
    }
}

// 7. Режим «Все операции» (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "7. Все операции - Светлая", showBackground = true)
@Composable
private fun AddAccountAllTransactionsPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            initialIsViewingAllTransactions = true,
            initialDeletedTransactions = previewTransactions,
            categories = previewCategories,
            accounts = listOf(previewActiveAccount),
            onGetAccountTransactions = { previewTransactions }
        )
    }
}

// 8. Режим «Все операции» (Тёмная тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "8. Все операции - Тёмная", showBackground = true)
@Composable
private fun AddAccountAllTransactionsDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            initialIsViewingAllTransactions = true,
            initialDeletedTransactions = previewTransactions,
            categories = previewCategories,
            accounts = listOf(previewActiveAccount),
            onGetAccountTransactions = { previewTransactions }
        )
    }
}

// 9. Режим «Все операции» без операций (пустое состояние)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "9. Все операции - Пусто", showBackground = true)
@Composable
private fun AddAccountAllTransactionsEmptyPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AddAccountContent(
            onDismiss = {},
            onAddClick = { _, _, _, _ -> },
            initialDeletedAccount = previewDeletedAccount,
            initialIsViewingAllTransactions = true,
            initialDeletedTransactions = emptyList(),
            categories = previewCategories,
            accounts = listOf(previewActiveAccount)
        )
    }
}