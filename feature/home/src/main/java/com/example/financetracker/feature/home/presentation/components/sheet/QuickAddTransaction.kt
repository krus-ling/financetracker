package com.example.financetracker.feature.home.presentation.components.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.account.AccountCardItem
import com.example.financetracker.core.uikit.components.account.AddAccountCardItem
import com.example.financetracker.core.uikit.components.account.getIconRes
import com.example.financetracker.core.uikit.components.button.AppMiniActionButton
import com.example.financetracker.core.uikit.components.category.CategoryIconChip
import com.example.financetracker.core.uikit.components.input.AmountInputField
import com.example.financetracker.core.uikit.components.input.AppTextField
import com.example.financetracker.core.uikit.components.input.DateTimePickerRow
import com.example.financetracker.core.uikit.components.sheet.AppFormBottomSheet
import com.example.financetracker.core.uikit.components.sheet.LocalAppFormSheetDismiss
import com.example.financetracker.core.uikit.components.transaction.TransactionTypeSelector
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.InkPrimary
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint
import com.example.financetracker.core.uikit.util.AmountFormatter
import com.example.financetracker.core.uikit.util.rememberHalfWidth
import com.example.financetracker.core.uikit.util.toAmountOrNull
import com.example.financetracker.feature.home.domain.model.ParsedTransactionDraft
import com.example.financetracker.feature.home.presentation.components.fastinput.FastInputSection
import com.example.financetracker.feature.home.presentation.components.fastinput.FastInputTriggerChip
import java.util.Calendar
import java.util.TimeZone

/** Горизонтальный отступ контента шторки */
private val SheetHorizontalPadding = 20.dp

/** Расстояние между карточками в карусели счетов */
private val AccountCardSpacing = 10.dp

/** Расстояние между элементами в сетке категорий */
private val CategoryGridSpacing = 8.dp

/** Количество колонок на одной странице сетки категорий */
private const val CategoryGridColumns = 4

/** Количество рядов на одной странице сетки категорий */
private const val CategoryGridRows = 2

/** Количество категорий на одной странице */
private const val CategoryPageSize = CategoryGridColumns * CategoryGridRows

/** Максимальная длина текста заметки */
private const val MaxNoteLength = 100

// =========================================================================
// 1. ОСНОВНЫЕ КОМПОНЕНТЫ ЭКРАНА (ENTRY POINTS)
// =========================================================================

/**
 * Модальная шторка быстрого создания новой операции (Расход, Доход или Перевод).
 *
 * Включает:
 * - Панель текстового/голосового быстрого ввода с AI/NLP-разбором ([FastInputSection]) с приоритетом
 *   ручных правок пользователя над автозаполнением;
 * - Селектор типа операции и поле суммы с быстрым сохранением через мини-кнопку;
 * - Сетку категорий с постраничной прокруткой ([CategoryGridSection]) или выбор счёта списания
 *   («Откуда» для перевода);
 * - Карусель выбора счёта ([AccountSelectorSection]) и строку даты/заметки ([TransactionDateAndNoteRow]).
 *
 * @param categories Список всех категорий пользователя (фильтруется по выбранному типу операции).
 * @param accounts Список доступных активных счетов.
 * @param onDismiss Закрытие шторки.
 * @param onAddNewAccount Переход к созданию нового счёта при нажатии на карточку «+».
 * @param initialType Начальный тип операции при открытии шторки.
 * @param initialAccountId Предвыбранный счёт (например, при открытии с детального экрана счёта).
 * @param onAddClick Колбэк сохранения новой операции с валидированными полями.
 * @param learnedCategoryKeywords Персональный словарь ключевых слов категорий для быстрого ввода.
 * @param learnedAccountAliases Персональный словарь алиасов счетов для быстрого ввода.
 * @param onLearnFromFastInput Обратная связь обучения после сохранения операции, заполненной через быстрый ввод.
 * @param onParseFastInput Асинхронный парсер фразы быстрого ввода (NLP + локальный BERT-эмбеддер).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTransactionSheet(
    categories: List<Category>,
    accounts: List<Account> = emptyList(),
    onDismiss: () -> Unit,
    onAddNewAccount: () -> Unit,
    initialType: TransactionType = TransactionType.EXPENSE,
    initialAccountId: String? = null,
    onAddClick: (
        amount: Double,
        categoryId: String?,
        accountId: String,
        targetAccountId: String?,
        type: TransactionType,
        note: String,
        dateTime: Long
    ) -> Unit,
    learnedCategoryKeywords: Map<String, String> = emptyMap(),
    learnedAccountAliases: Map<String, String> = emptyMap(),
    onLearnFromFastInput: (
        rawText: String,
        categoryId: String?,
        accountId: String?,
        wasCategoryChanged: Boolean,
        wasAccountChanged: Boolean
    ) -> Unit = { _, _, _, _, _ -> },
    onParseFastInput: (suspend (text: String, defaultAccountId: String?) -> ParsedTransactionDraft)? = null,
) {
    val state = rememberQuickAddTransactionState(
        categories = categories,
        accounts = accounts,
        initialType = initialType,
        initialAccountId = initialAccountId,
        onAddClick = onAddClick,
        onLearnFromFastInput = onLearnFromFastInput
    )

    AppFormBottomSheet(
        title = "Новая операция",
        titleTrailingContent = {
            AnimatedVisibility(
                visible = !state.isFastInputExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FastInputTriggerChip(onClick = { state.isFastInputExpanded = true })
            }
        },
        primaryButtonText = "Добавить",
        cancelButtonText = "Назад",
        onPrimaryClick = { state.submit() },
        onDismiss = onDismiss,
        onSheetOpened = { state.amountFocusRequester.requestFocus() },
        modifier = Modifier.fillMaxHeight()
    ) { clearFocus ->
        val dismissSheet = LocalAppFormSheetDismiss.current

        // 0. Быстрый ввод
        FastInputSection(
            isExpanded = state.isFastInputExpanded,
            onExpandedChange = { state.isFastInputExpanded = it },
            categories = categories,
            accounts = accounts,
            defaultAccountId = state.effectiveDefaultAccountId,
            onDraftParsed = state::applyFastInputDraft,
            learnedCategoryKeywords = learnedCategoryKeywords,
            learnedAccountAliases = learnedAccountAliases,
            onParseFastInput = onParseFastInput,
        )

        // 1. Селектор типа операции
        TransactionTypeSelector(
            selectedType = state.selectedType,
            onTypeSelected = { state.selectedType = it }
        )

        // 2. Поле ввода суммы и кнопка быстрого сохранения
        TransactionAmountField(
            amountText = state.amountText,
            isAmountError = state.amountError,
            isAmountValid = state.isAmountValid,
            focusRequester = state.amountFocusRequester,
            onAmountChange = { state.amountText = it },
            onDone = clearFocus,
            onQuickSubmit = {
                if (state.submit()) {
                    clearFocus()
                    dismissSheet()
                }
            }
        )

        // 3. Сетка категорий (Расход/Доход) или счёт списания («Откуда»)
        TransactionCategoryOrSourceSection(
            selectedType = state.selectedType,
            categories = state.filteredCategories,
            selectedCategoryId = state.selectedCategoryId,
            accounts = accounts,
            selectedSourceAccountId = state.selectedAccountId,
            cardWidth = state.halfWidth,
            accountListState = state.accountListState,
            onCategoryClick = state::selectCategory,
            onSourceAccountSelected = state::selectSourceAccount,
            onAddNewAccount = onAddNewAccount
        )

        // 4. Основной счёт (Расход/Доход) или счёт зачисления «Куда» (Перевод)
        AccountSelectorSection(
            title = if (state.selectedType == TransactionType.TRANSFER) "Куда" else "Счёт",
            accounts = if (state.selectedType == TransactionType.TRANSFER) state.targetAccounts else accounts,
            selectedAccountId = if (state.selectedType == TransactionType.TRANSFER) {
                state.selectedTargetAccountId
            } else {
                state.selectedAccountId
            },
            cardWidth = state.halfWidth,
            onAccountSelected = { id ->
                if (state.selectedType == TransactionType.TRANSFER) {
                    state.selectTargetAccount(id)
                } else {
                    state.selectSourceAccount(id)
                }
            },
            onAddNewAccount = onAddNewAccount,
            lazyListState = if (state.selectedType == TransactionType.TRANSFER) {
                state.targetAccountListState
            } else {
                state.accountListState
            },
            errorMessage = state.accountErrorMessage
        )

        // 5. Дата и заметка
        TransactionDateAndNoteRow(
            selectedDateTime = state.selectedDateTime,
            note = state.note,
            dateWidth = state.halfWidth,
            onDateClick = { state.showDatePicker = true },
            onNoteChange = { if (it.length <= MaxNoteLength) state.note = it },
            onDone = clearFocus
        )
    }

    if (state.showDatePicker) {
        TransactionDatePickerDialog(
            currentDateTime = state.selectedDateTime,
            onDateSelected = state::selectDateFromPicker,
            onDismiss = { state.showDatePicker = false }
        )
    }
}

// =========================================================================
// 2. СОСТОЯНИЕ ФОРМЫ (STATE HOLDER)
// =========================================================================

/**
 * Создаёт и синхронизирует состояние формы [QuickAddTransactionSheet].
 */
@Composable
private fun rememberQuickAddTransactionState(
    categories: List<Category>,
    accounts: List<Account>,
    initialType: TransactionType,
    initialAccountId: String?,
    onAddClick: (
        amount: Double,
        categoryId: String?,
        accountId: String,
        targetAccountId: String?,
        type: TransactionType,
        note: String,
        dateTime: Long
    ) -> Unit,
    onLearnFromFastInput: (
        rawText: String,
        categoryId: String?,
        accountId: String?,
        wasCategoryChanged: Boolean,
        wasAccountChanged: Boolean
    ) -> Unit
): QuickAddTransactionState {
    val amountFocusRequester = remember { FocusRequester() }
    val accountListState = rememberLazyListState()
    val targetAccountListState = rememberLazyListState()
    val halfWidth = rememberHalfWidth(
        horizontalPadding = SheetHorizontalPadding,
        spacing = AccountCardSpacing
    )
    val initialDateTime = remember { System.currentTimeMillis() }
    val fastInputExpandedState = rememberSaveable { mutableStateOf(false) }
    val state = remember {
        QuickAddTransactionState(
            initialType = initialType,
            initialDateTime = initialDateTime,
            initialAccountId = initialAccountId,
            initialAccounts = accounts,
            isFastInputExpandedState = fastInputExpandedState,
            amountFocusRequester = amountFocusRequester,
            accountListState = accountListState,
            targetAccountListState = targetAccountListState,
            halfWidth = halfWidth,
            onAddClick = onAddClick,
            onLearnFromFastInput = onLearnFromFastInput
        )
    }
    // Актуализируем ссылки на внешние списки и колбэки без пересоздания state
    state.categories = categories
    state.accounts = accounts
    state.onAddClick = onAddClick
    state.onLearnFromFastInput = onLearnFromFastInput

    // Синхронизация выбранных счетов при обновлении списка из БД или смене счёта списания
    LaunchedEffect(accounts, state.selectedAccountId) {
        state.syncAccounts(preferredAccountId = initialAccountId)
    }

    // Установка категории расхода по умолчанию при первой загрузке справочника
    LaunchedEffect(categories) {
        state.syncCategories()
    }

    return state
}

/**
 * Держатель состояния и бизнес-логики формы быстрого добавления операции.
 */
@Stable
private class QuickAddTransactionState(
    initialType: TransactionType,
    private val initialDateTime: Long,
    initialAccountId: String?,
    initialAccounts: List<Account>,
    private val isFastInputExpandedState: MutableState<Boolean>,
    val amountFocusRequester: FocusRequester,
    val accountListState: LazyListState,
    val targetAccountListState: LazyListState,
    val halfWidth: Dp,
    onAddClick: (Double, String?, String, String?, TransactionType, String, Long) -> Unit,
    onLearnFromFastInput: (String, String?, String?, Boolean, Boolean) -> Unit
) {
    /** Внешние данные и колбэки */
    var categories by mutableStateOf<List<Category>>(emptyList())
    var accounts by mutableStateOf(initialAccounts)
    var onAddClick by mutableStateOf(onAddClick)
    var onLearnFromFastInput by mutableStateOf(onLearnFromFastInput)

    /* Поля формы */
    var selectedType by mutableStateOf(initialType)
    var amountText by mutableStateOf("")
    var note by mutableStateOf("")
    var selectedDateTime by mutableLongStateOf(initialDateTime)
    var selectedAccountId by mutableStateOf(
        resolvePreferredAccountId(initialAccounts, initialAccountId)
    )
    var selectedTargetAccountId by mutableStateOf(
        initialAccounts.firstOrNull { it.id != selectedAccountId }?.id
    )
    var selectedExpenseCategoryId by mutableStateOf<String?>(null)
    var selectedIncomeCategoryId by mutableStateOf<String?>(null)

    /** UI-состояния диалога и отправки */
    var showDatePicker by mutableStateOf(false)
    var submitted by mutableStateOf(false)
    private var isSubmitting by mutableStateOf(false)

    /** Состояние быстрого ввода и приоритета ручного выбора */
    var isFastInputExpanded: Boolean
        get() = isFastInputExpandedState.value
        set(value) {
            isFastInputExpandedState.value = value
        }

    private var lastFastInputText by mutableStateOf("")
    private var userChangedCategory by mutableStateOf(false)
    private var userChangedAccount by mutableStateOf(false)
    private var userChangedDate by mutableStateOf(false)
    private var isDateFromFastInput by mutableStateOf(false)

    // --- Вычисляемые свойства ---

    /** Счета зачисления для перевода (исключая выбранный счёт списания). */
    val targetAccounts: List<Account> by derivedStateOf {
        accounts.filter { it.id != selectedAccountId }
    }

    /** Категории, отфильтрованные под текущий тип операции. */
    val filteredCategories: List<Category> by derivedStateOf {
        when (selectedType) {
            TransactionType.EXPENSE -> categories.filter { it.type == CategoryType.EXPENSE }
            TransactionType.INCOME -> categories.filter { it.type == CategoryType.INCOME }
            TransactionType.TRANSFER -> emptyList()
        }
    }

    private val parsedAmount: Double? by derivedStateOf { amountText.toAmountOrNull() }

    /** Признак корректно введённой положительной суммы. */
    val isAmountValid: Boolean by derivedStateOf {
        parsedAmount?.let { it.isFinite() && it > 0.0 } == true
    }

    /** Показывать ли ошибку в поле суммы. */
    val amountError: Boolean by derivedStateOf { submitted && !isAmountValid }

    /** Сообщение об ошибке выбора счёта после попытки сохранения. */
    val accountErrorMessage: String? by derivedStateOf {
        if (!submitted) return@derivedStateOf null
        when {
            selectedType == TransactionType.TRANSFER && accounts.size < 2 ->
                "Для перевода нужно минимум два счёта"
            selectedType != TransactionType.TRANSFER && accounts.isEmpty() ->
                "Создайте хотя бы один счёт"
            else -> null
        }
    }

    /** Идентификатор выбранной категории с учётом активного типа операции. */
    val selectedCategoryId: String?
        get() = if (selectedType == TransactionType.INCOME) {
            selectedIncomeCategoryId
        } else {
            selectedExpenseCategoryId
        }

    /** Базовый счёт, используемый парсером быстрого ввода по умолчанию. */
    val effectiveDefaultAccountId: String?
        get() = selectedAccountId ?: accounts.firstOrNull()?.id

    // --- Синхронизация и пользовательские действия ---

    fun syncAccounts(preferredAccountId: String?) {
        if (selectedAccountId == null || accounts.none { it.id == selectedAccountId }) {
            selectedAccountId = resolvePreferredAccountId(accounts, preferredAccountId)
        }
        val currentTargetStillValid = accounts.any {
            it.id == selectedTargetAccountId && it.id != selectedAccountId
        }
        if (selectedTargetAccountId == null || !currentTargetStillValid) {
            selectedTargetAccountId = accounts.firstOrNull { it.id != selectedAccountId }?.id
        }
    }

    fun syncCategories() {
        val expenseList = categories.filter { it.type == CategoryType.EXPENSE }
        if (selectedExpenseCategoryId == null || expenseList.none { it.id == selectedExpenseCategoryId }) {
            selectedExpenseCategoryId = expenseList.firstOrNull()?.id
        }
    }

    fun selectCategory(categoryId: String?) {
        if (isFastInputExpanded || lastFastInputText.isNotBlank()) {
            userChangedCategory = true
        }
        if (selectedType == TransactionType.INCOME) {
            selectedIncomeCategoryId = categoryId
        } else {
            selectedExpenseCategoryId = categoryId
        }
    }

    fun selectSourceAccount(accountId: String) {
        if (isFastInputExpanded || lastFastInputText.isNotBlank()) {
            userChangedAccount = true
        }
        selectedAccountId = accountId
        if (selectedTargetAccountId == accountId) {
            selectedTargetAccountId = accounts.firstOrNull { it.id != accountId }?.id
        }
    }

    fun selectTargetAccount(accountId: String) {
        selectedTargetAccountId = accountId
    }

    fun selectDateFromPicker(newDateTime: Long) {
        selectedDateTime = newDateTime
        userChangedDate = true
        isDateFromFastInput = false
    }

    fun applyFastInputDraft(draft: ParsedTransactionDraft) {
        if (draft.rawText.isBlank()) {
            // При закрытии быстрого ввода (X) или очистке строки сохраняем уже выбранные поля,
            // но сбрасываем контекст обучения для предыдущей фразы
            lastFastInputText = ""
            userChangedCategory = false
            userChangedAccount = false
            return
        }
        lastFastInputText = draft.rawText
        draft.amount?.let { amt ->
            amountText = AmountFormatter.formatForInput(amt)
        }
        selectedType = draft.type
        if (!userChangedCategory) {
            when (draft.type) {
                TransactionType.INCOME -> selectedIncomeCategoryId = draft.categoryId
                TransactionType.EXPENSE -> draft.categoryId?.let { selectedExpenseCategoryId = it }
                TransactionType.TRANSFER -> Unit
            }
        }
        if (!userChangedAccount) {
            draft.accountId?.let { accountId ->
                selectedAccountId = accountId
                if (selectedTargetAccountId == accountId) {
                    selectedTargetAccountId = accounts.firstOrNull { it.id != accountId }?.id
                }
            }
        }
        if (!userChangedDate) {
            val parsedDate = draft.date
            if (parsedDate != null) {
                selectedDateTime = parsedDate
                isDateFromFastInput = true
            } else if (isDateFromFastInput) {
                // Пользователь стёр дату в быстром вводе — возвращаем сегодняшнюю
                selectedDateTime = initialDateTime
                isDateFromFastInput = false
            }
        }
        note = draft.note
    }

    /**
     * Валидирует форму, защищает от повторного нажатия и сохраняет операцию.
     *
     * @return `true`, если операция отправлена на сохранение и шторку можно закрывать.
     */
    fun submit(): Boolean {
        if (isSubmitting) return false
        submitted = true

        val validAmount = parsedAmount?.takeIf { it.isFinite() && it > 0.0 } ?: return false
        val chosenCategory = when (selectedType) {
            TransactionType.TRANSFER -> null
            TransactionType.INCOME -> selectedIncomeCategoryId
            TransactionType.EXPENSE -> selectedExpenseCategoryId
                ?: filteredCategories.firstOrNull()?.id
        }
        val chosenAccount = effectiveDefaultAccountId ?: return false
        val chosenTargetAccount =
            if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else null

        val isCategoryValid = selectedType != TransactionType.EXPENSE || chosenCategory != null
        val isTransferValid = selectedType != TransactionType.TRANSFER ||
                (chosenTargetAccount != null && chosenAccount != chosenTargetAccount)

        if (!isCategoryValid || !isTransferValid) return false
        isSubmitting = true
        onAddClick(
            validAmount,
            chosenCategory,
            chosenAccount,
            chosenTargetAccount,
            selectedType,
            note.trim(),
            selectedDateTime
        )
        if (lastFastInputText.isNotBlank()) {
            onLearnFromFastInput(
                lastFastInputText,
                chosenCategory,
                chosenAccount,
                userChangedCategory,
                userChangedAccount
            )
        }
        return true
    }
}

/**
 * Выбирает предпочтительный счёт по умолчанию: сначала [preferredAccountId] (если существует),
 * затем счёт с флагом `isDefault`, затем первый доступный.
 */
private fun resolvePreferredAccountId(
    accounts: List<Account>,
    preferredAccountId: String?
): String? =
    preferredAccountId?.takeIf { id -> accounts.any { it.id == id } }
        ?: accounts.find { it.isDefault }?.id
        ?: accounts.firstOrNull()?.id

// =========================================================================
// 3. ВСПОМОГАТЕЛЬНЫЕ ПОДКОМПОНЕНТЫ (SUB-COMPOSABLES)
// =========================================================================

/**
 * Постраничная сетка выбора категории (2 ряда по 4 элемента на странице).
 *
 * @param categories Отфильтрованный по типу операции список категорий.
 * @param selectedCategoryId Идентификатор выбранной категории или `null`.
 * @param isIncome Режим дохода (позволяет снять выбор повторным тапом).
 * @param onCategoryClick Вызывается при выборе или снятии категории.
 * @param modifier Модификатор контейнера секции.
 */
@Composable
private fun CategoryGridSection(
    categories: List<Category>,
    selectedCategoryId: String?,
    isIncome: Boolean,
    onCategoryClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CategoryGridSpacing)
    ) {
        Text(
            text = if (isIncome) "Категория (необязательно)" else "Категория",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Заранее группируем категории на страницы (8) и ряды (по 4)
        val pages = remember(categories) {
            categories
                .chunked(CategoryPageSize)
                .map { page -> page.chunked(CategoryGridColumns) }
        }

        if (pages.isNotEmpty()) {
            // Автоматический скролл к странице, где находится выбранная категория
            val selectedPageIndex = remember(categories, selectedCategoryId) {
                val index = categories.indexOfFirst { it.id == selectedCategoryId }
                if (index >= 0) index / CategoryPageSize else 0
            }

            key(isIncome) {
                val pagerState = rememberPagerState(
                    initialPage = selectedPageIndex.coerceIn(pages.indices),
                    pageCount = { pages.size }
                )

                // Прокручиваем только при явной смене выбранной категории (например, из FastInput)
                LaunchedEffect(selectedCategoryId) {
                    if (selectedPageIndex in pages.indices && pagerState.currentPage != selectedPageIndex) {
                        pagerState.animateScrollToPage(selectedPageIndex)
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    val pageRows = pages[pageIndex]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(CategoryGridSpacing)
                    ) {
                        repeat(CategoryGridRows) { rowIndex ->
                            val rowCategories = pageRows.getOrNull(rowIndex).orEmpty()
                            if (rowCategories.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(CategoryGridSpacing)
                                ) {
                                    rowCategories.forEach { category ->
                                        val isSelected = category.id == selectedCategoryId
                                        Box(modifier = Modifier.weight(1f)) {
                                            CategoryIconChip(
                                                iconRes = category.iconRes,
                                                title = category.name,
                                                isSelected = isSelected,
                                                onClick = {
                                                    if (isIncome) {
                                                        onCategoryClick(if (isSelected) null else category.id)
                                                    } else {
                                                        onCategoryClick(category.id)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    repeat(CategoryGridColumns - rowCategories.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            } else {
                                // Сохраняем фиксированную высоту второго ряда без кликов и TalkBack-фокуса
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(CategoryGridSpacing)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .alpha(0f)
                                            .clearAndSetSemantics {}
                                    ) {
                                        CategoryIconChip(
                                            iconRes = null,
                                            title = " ",
                                            isSelected = false,
                                            enabled = false,
                                            onClick = {}
                                        )
                                    }
                                    repeat(CategoryGridColumns - 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                if (pages.size > 1) {
                    CategoryPagerIndicator(
                        pageCount = pages.size,
                        currentPage = pagerState.currentPage
                    )
                }
            }
        }
    }
}

/**
 * Точки-индикаторы текущей страницы для сетки категорий.
 *
 * @param pageCount Общее количество страниц.
 * @param currentPage Индекс активной страницы.
 * @param modifier Модификатор контейнера индикатора.
 */
@Composable
private fun CategoryPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val isCurrentPage = currentPage == iteration
            val color = if (isCurrentPage) {
                Primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            }
            val indicatorWidth = if (isCurrentPage) 16.dp else 6.dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(6.dp)
                    .width(indicatorWidth)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * Горизонтальная карусель выбора счёта с кнопкой быстрого создания нового счёта.
 *
 * @param title Заголовок секции («Счёт», «Откуда» или «Куда»).
 * @param accounts Список отображаемых счетов.
 * @param selectedAccountId Идентификатор выбранного счёта.
 * @param cardWidth Рассчитанная ширина карточки счёта.
 * @param onAccountSelected Вызывается при выборе счёта из списка.
 * @param onAddNewAccount Вызывается при нажатии на карточку создания/добавления счёта.
 * @param modifier Модификатор контейнера секции.
 * @param lazyListState Состояние прокрутки горизонтального списка.
 * @param showAddAccountButton Показывать ли карточку добавления нового счёта в конце списка.
 * @param errorMessage Текст ошибки валидации под каруселью или `null`.
 */
@Composable
private fun AccountSelectorSection(
    title: String,
    accounts: List<Account>,
    selectedAccountId: String?,
    cardWidth: Dp,
    onAccountSelected: (String) -> Unit,
    onAddNewAccount: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    showAddAccountButton: Boolean = true,
    errorMessage: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (accounts.isEmpty()) {
            AddAccountCardItem(
                title = if (title == "Счёт") "Создать счёт" else "Добавить счёт",
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.width(cardWidth),
                onClick = onAddNewAccount
            )
        } else {
            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(AccountCardSpacing)
            ) {
                items(accounts, key = { it.id }) { account ->
                    val isSelected = account.id == selectedAccountId
                    AccountCardItem(
                        title = account.name,
                        balance = account.balance,
                        accountIcon = account.type.getIconRes(),
                        isSelected = isSelected,
                        isDefault = account.isDefault,
                        containerColor = if (isSelected) {
                            PrimaryTint
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        },
                        contentColor = if (isSelected) {
                            InkPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.width(cardWidth),
                        onClick = { onAccountSelected(account.id) }
                    )
                }
                if (showAddAccountButton) {
                    item(key = "add_account_button") {
                        AddAccountCardItem(
                            title = "Добавить счёт",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.width(cardWidth),
                            onClick = onAddNewAccount
                        )
                    }
                }
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Диалог выбора календарной даты операции с корректным преобразованием UTC <-> локальной даты
 * и сохранением текущего времени.
 *
 * @param currentDateTime Текущая выбранная дата и время операции (мс).
 * @param onDateSelected Возвращает обновлённый timestamp с новой датой.
 * @param onDismiss Закрытие диалога без сохранения.
 */
@Composable
private fun TransactionDatePickerDialog(
    currentDateTime: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialUtcDateMillis = remember(currentDateTime) {
        val localCal = Calendar.getInstance().apply { timeInMillis = currentDateTime }
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                localCal.get(Calendar.YEAR),
                localCal.get(Calendar.MONTH),
                localCal.get(Calendar.DAY_OF_MONTH)
            )
        }.timeInMillis
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialUtcDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { utcPickerMillis ->
                        val utcPicked = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = utcPickerMillis
                        }
                        val currentCalendar = Calendar.getInstance().apply {
                            timeInMillis = currentDateTime
                        }
                        currentCalendar.set(
                            utcPicked.get(Calendar.YEAR),
                            utcPicked.get(Calendar.MONTH),
                            utcPicked.get(Calendar.DAY_OF_MONTH)
                        )
                        onDateSelected(currentCalendar.timeInMillis)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * Строка выбора даты и ввода заметки. При фокусе на заметку кнопка даты плавно скрывается,
 * освобождая всё доступное пространство под текст.
 *
 * @param selectedDateTime Текущая дата и время операции (мс).
 * @param note Текущий текст заметки.
 * @param dateWidth Ширина блока выбора даты в свёрнутом состоянии заметки.
 * @param onDateClick Открытие диалога выбора даты.
 * @param onNoteChange Изменение текста заметки.
 * @param onDone Действие по кнопке «Готово» на клавиатуре.
 * @param modifier Модификатор контейнера строки.
 */
@Composable
private fun TransactionDateAndNoteRow(
    selectedDateTime: Long,
    note: String,
    dateWidth: Dp,
    onDateClick: () -> Unit,
    onNoteChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isNoteFocused by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (!isNoteFocused) AccountCardSpacing else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = !isNoteFocused,
            enter = expandHorizontally(animationSpec = tween(200)) +
                    fadeIn(animationSpec = tween(200)),
            exit = shrinkHorizontally(animationSpec = tween(200)) +
                    fadeOut(animationSpec = tween(50))
        ) {
            DateTimePickerRow(
                dateTime = selectedDateTime,
                onClick = onDateClick,
                modifier = Modifier
                    .width(dateWidth)
                    .padding(top = 8.dp)
            )
        }

        AppTextField(
            value = note,
            onValueChange = onNoteChange,
            label = "Заметка",
            placeholder = "Необязательно",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isNoteFocused = it.isFocused }
        )
    }
}

/**
 * Поле ввода суммы операции с анимированной кнопкой быстрого сохранения справа.
 * Вынесено отдельно, чтобы ввод каждой цифры не рекомпозировал всю шторку целиком.
 *
 * @param amountText Текущая строка суммы.
 * @param isAmountError Флаг отображения ошибки валидации.
 * @param isAmountValid Валидна ли сумма для быстрого сохранения.
 * @param focusRequester Контроллер фокуса для автофокуса при открытии шторки.
 * @param onAmountChange Вызывается при изменении текста суммы.
 * @param onDone Действие по кнопке «Готово» на клавиатуре.
 * @param onQuickSubmit Мгновенное сохранение и закрытие шторки по мини-кнопке.
 * @param modifier Модификатор контейнера поля.
 */
@Composable
private fun TransactionAmountField(
    amountText: String,
    isAmountError: Boolean,
    isAmountValid: Boolean,
    focusRequester: FocusRequester,
    onAmountChange: (String) -> Unit,
    onDone: () -> Unit,
    onQuickSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    AmountInputField(
        value = amountText,
        onValueChange = onAmountChange,
        isError = isAmountError,
        errorMessage = "Введите сумму больше нуля",
        focusRequester = focusRequester,
        onDone = onDone,
        modifier = modifier,
        trailingIcon = {
            AnimatedVisibility(
                visible = isAmountValid,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
            ) {
                AppMiniActionButton(
                    onClick = onQuickSubmit,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    )
}

/**
 * Адаптивная секция выбора категории (для Расхода/Дохода) либо счёта списания «Откуда» (для Перевода).
 *
 * @param selectedType Текущий тип операции.
 * @param categories Отфильтрованный список категорий для активного типа.
 * @param selectedCategoryId Идентификатор выбранной категории.
 * @param accounts Список активных счетов пользователя.
 * @param selectedSourceAccountId Идентификатор выбранного счёта списания.
 * @param cardWidth Ширина карточки счёта в карусели «Откуда».
 * @param accountListState Состояние скролла списка счетов.
 * @param onCategoryClick Выбор или снятие категории.
 * @param onSourceAccountSelected Выбор счёта списания.
 * @param onAddNewAccount Переход к созданию нового счёта.
 * @param modifier Модификатор контейнера секции.
 */
@Composable
private fun TransactionCategoryOrSourceSection(
    selectedType: TransactionType,
    categories: List<Category>,
    selectedCategoryId: String?,
    accounts: List<Account>,
    selectedSourceAccountId: String?,
    cardWidth: Dp,
    accountListState: LazyListState,
    onCategoryClick: (String?) -> Unit,
    onSourceAccountSelected: (String) -> Unit,
    onAddNewAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(CategoryGridSpacing)
    ) {
        if (selectedType == TransactionType.TRANSFER && accounts.isNotEmpty()) {
            AccountSelectorSection(
                title = "Откуда",
                accounts = accounts,
                selectedAccountId = selectedSourceAccountId,
                cardWidth = cardWidth,
                onAccountSelected = onSourceAccountSelected,
                onAddNewAccount = onAddNewAccount,
                lazyListState = accountListState,
                showAddAccountButton = false
            )
        } else if (selectedType != TransactionType.TRANSFER) {
            CategoryGridSection(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                isIncome = selectedType == TransactionType.INCOME,
                onCategoryClick = onCategoryClick
            )
        }
    }
}

// =========================================================================
// 4. PREVIEWS & ТЕСТОВЫЕ ДАННЫЕ
// =========================================================================

private val previewAccounts = listOf(
    Account(id = "1", name = "Основная карта", balance = 86700.0, isDefault = true),
    Account(id = "2", name = "Наличные", balance = 12500.0),
    Account(id = "3", name = "Накопления", balance = 250000.0)
)
private val previewCategories = listOf(
    Category(id = "cat1", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "ic_category_groceries"),
    Category(id = "cat2", name = "Кафе", type = CategoryType.EXPENSE, iconRes = "ic_category_cafe"),
    Category(id = "cat3", name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "ic_category_transport"),
    Category(id = "cat4", name = "Одежда", type = CategoryType.EXPENSE, iconRes = "ic_category_clothing"),
    Category(id = "cat5", name = "Зарплата", type = CategoryType.INCOME, iconRes = "ic_category_salary"),
    Category(id = "cat6", name = "Фриланс", type = CategoryType.INCOME, iconRes = "ic_category_freelance"),
    Category(id = "cat7", name = "Здоровье", type = CategoryType.EXPENSE, iconRes = "ic_category_health")
)
// 1. Режим «Расход» (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "1. Расход - Светлая", showBackground = true)
@Composable
private fun QuickAddExpenseLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = previewAccounts,
            initialType = TransactionType.EXPENSE,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}
// 2. Режим «Доход» (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "2. Доход - Светлая", showBackground = true)
@Composable
private fun QuickAddIncomeLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = previewAccounts,
            initialType = TransactionType.INCOME,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}
// 3. Режим «Перевод между счетами» (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "3. Перевод - Светлая", showBackground = true)
@Composable
private fun QuickAddTransferLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = previewAccounts,
            initialType = TransactionType.TRANSFER,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}
// 4. Режим «Расход» (Тёмная тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "4. Расход - Тёмная", showBackground = true)
@Composable
private fun QuickAddExpenseDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = previewAccounts,
            initialType = TransactionType.EXPENSE,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}
// 5. Состояние без счетов (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "5. Нет счетов - Светлая", showBackground = true)
@Composable
private fun QuickAddNoAccountsLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = previewCategories,
            accounts = emptyList(),
            initialType = TransactionType.EXPENSE,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}

// 6. Несколько страниц категорий (Светлая тема)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "6. Много категорий - Светлая", showBackground = true)
@Composable
private fun QuickAddMultiPageCategoriesPreview() {
    val manyCategories = previewCategories + listOf(
        Category(id = "cat8", name = "Дом", type = CategoryType.EXPENSE, iconRes = "ic_category_home"),
        Category(id = "cat9", name = "Питомцы", type = CategoryType.EXPENSE, iconRes = "ic_category_pets"),
        Category(id = "cat10", name = "Развлечения", type = CategoryType.EXPENSE, iconRes = "ic_category_entertainment"),
        Category(id = "cat11", name = "Связь", type = CategoryType.EXPENSE, iconRes = "ic_category_phone"),
        Category(id = "cat12", name = "Подарки", type = CategoryType.EXPENSE, iconRes = "ic_category_gift")
    )
    FinancetrackerTheme(darkTheme = false) {
        QuickAddTransactionSheet(
            categories = manyCategories,
            accounts = previewAccounts,
            initialType = TransactionType.EXPENSE,
            onDismiss = {},
            onAddClick = { _, _, _, _, _, _, _ -> },
            onAddNewAccount = {}
        )
    }
}