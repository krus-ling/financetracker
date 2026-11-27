package com.example.financetracker.core.uikit.components.sheet

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.example.financetracker.core.uikit.components.button.AppPrimaryButton
import com.example.financetracker.core.uikit.components.button.AppSecondaryButton
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Оптимизированная универсальная шторка (bottom sheet) для форм ввода данных.
 *
 * Особенности и архитектура:
 * - Безопасный автофокус без фризов через колбэк [onSheetOpened] (запрос фокуса строго
 *   после завершения анимации раскрытия шторки);
 * - Защита от удара о верхний край экрана: отступ [statusBarsPadding] + 16.dp гарантирует,
 *   что шторка никогда не бьётся о строку состояния и сохраняет скруглённые углы и затемнение сверху;
 * - Плавное программное закрытие: кнопки «Отмена» и действие формы [onPrimaryClick]
 *   закрывают шторку с естественной плавной анимацией скольжения вниз;
 * - Мгновенное скрытие клавиатуры при начале свайпа вниз (за ручку шторки или контент);
 * - Компактные отступы (12.dp) для комфортного размещения формы без растягивания во весь экран;
 * - Автоматический сброс скролла наверх к заголовку при скрытии клавиатуры;
 * - Снятие фокуса при скрытии клавиатуры по системной кнопке «Назад».
 */

/**
 * Колбэк плавного программного закрытия текущей [AppFormBottomSheet].
 */
val LocalAppFormSheetDismiss = staticCompositionLocalOf<() -> Unit> { {} }
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppFormBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState? = null,
    dismissThresholdFraction: Float = 0.20f,
    primaryButtonText: String? = null,
    onPrimaryClick: (() -> Boolean)? = null,
    isPrimaryEnabled: Boolean = true,
    cancelButtonText: String? = "Отмена",
    onSheetOpened: (() -> Unit)? = null,
    titleLeadingContent: @Composable (RowScope.() -> Unit)? = null,
    titleTrailingContent: @Composable (RowScope.() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.(clearFocus: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Если мы в окне Preview Android Studio — рендерим как Surface,
    // так как LayoutLib Студии не поддерживает отображение ModalBottomSheetDialog
    if (LocalInspectionMode.current) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = containerColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Полоска шторки
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BottomSheetDefaults.DragHandle()
                }

                // Заголовок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        titleLeadingContent?.invoke(this)
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    titleTrailingContent?.invoke(this)
                }

                // Контент формы
                content {}

                // Кнопки шторки
                val hasCancel = cancelButtonText != null
                val hasPrimary = primaryButtonText != null
                if (hasCancel || hasPrimary) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (hasCancel) {
                            AppSecondaryButton(
                                text = cancelButtonText,
                                onClick = {},
                                modifier = if (hasPrimary) Modifier.weight(1f) else Modifier.fillMaxWidth()
                            )
                        }
                        if (hasPrimary) {
                            AppPrimaryButton(
                                text = primaryButtonText,
                                onClick = {},
                                enabled = isPrimaryEnabled,
                                modifier = if (hasCancel) Modifier.weight(1.3f) else Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        return
    }

    val scrollState = rememberScrollState()

    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnSheetOpened by rememberUpdatedState(onSheetOpened)

    // Флаг для предотвращения повторных вызовов анимации закрытия
    var isDismissing by remember { mutableStateOf(false) }

    // Реальная высота контента шторки в пикселях для вычисления 20% порога
    var sheetHeightPx by remember { mutableFloatStateOf(1000f) }

    val sheetStateRef = remember { mutableStateOf<SheetState?>(null) }
    val currentThresholdFraction by rememberUpdatedState(dismissThresholdFraction)

    val confirmValueChange: (SheetValue) -> Boolean = remember {
        { targetValue ->
            if (targetValue == SheetValue.Hidden) {
                if (isDismissing) {
                    true // По кнопке Отмена или по сохранению закрываем всегда
                } else {
                    val currentOffset = runCatching {
                        sheetStateRef.value?.requireOffset()
                    }.getOrNull() ?: Float.MAX_VALUE
                    // Закрываем свайпом ТОЛЬКО если пользователь протянул шторку вниз минимум на 20% высоты
                    currentOffset >= (sheetHeightPx * currentThresholdFraction)
                }
            } else {
                true
            }
        }
    }

    val defaultSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = confirmValueChange
    )
    val actualSheetState = sheetState ?: defaultSheetState
    sheetStateRef.value = actualSheetState

    ModalBottomSheet(
        onDismissRequest = {
            if (!isDismissing) {
                currentOnDismiss()
            }
        },
        modifier = modifier.statusBarsPadding(),
        sheetState = actualSheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }, // Фон шторки заходит под системный бар
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = containerColor,
        dragHandle = {
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            BottomSheetDefaults.DragHandle(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        }
                    )
                }
            )
        }
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        // Делаем системный бар снизу полностью прозрачным в окне диалога шторки
        val view = LocalView.current
        DisposableEffect(view) {
            var parent = view.parent
            while (parent != null) {
                if (parent is DialogWindowProvider) {
                    val window = parent.window
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    @Suppress("DEPRECATION")
                    run {
                        window.navigationBarColor = android.graphics.Color.TRANSPARENT
                        window.isNavigationBarContrastEnforced = false
                    }

                    break
                }
                parent = parent.parent
            }
            onDispose {  }
        }

        val clearAllFocus: () -> Unit = remember(focusManager, keyboardController) {
            {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            }
        }

        // Плавное закрытие шторки с анимацией спуска
        val animateDismiss: (onFinished: (() -> Unit)?) -> Unit = remember(actualSheetState, coroutineScope) {
            { onFinished ->
                if (!isDismissing) {
                    isDismissing = true
                    clearAllFocus()
                    coroutineScope.launch {
                        actualSheetState.hide()
                    }.invokeOnCompletion {
                        onFinished?.invoke()
                        if (!actualSheetState.isVisible) {
                            currentOnDismiss()
                        }
                    }
                }
            }
        }

        // Колбэк после завершения анимации открытия шторки (для безопасного автофокуса)
        LaunchedEffect(actualSheetState) {
            snapshotFlow { actualSheetState.currentValue == SheetValue.Expanded && !actualSheetState.isAnimationRunning }
                .filter { it }
                .first()
            currentOnSheetOpened?.invoke()
        }

        // Отслеживаем системное закрытие клавиатуры (например, по системной кнопке Назад)
        val isImeVisible = WindowInsets.isImeVisible
        var wasImeOpened by remember { mutableStateOf(false) }

        LaunchedEffect(isImeVisible) {
            if (isImeVisible) {
                wasImeOpened = true
            } else {
                if (wasImeOpened) {
                    focusManager.clearFocus(force = true)
                    wasImeOpened = false
                }
            }
        }

        // Скрываем клавиатуру сразу при переходе шторки в закрытие
        LaunchedEffect(actualSheetState) {
            snapshotFlow { actualSheetState.targetValue }
                .collect { target ->
                    if (target == SheetValue.Hidden) {
                        clearAllFocus()
                    }
                }
        }

        // Перехват жеста смахивания контента вниз для мгновенного скрытия клавиатуры
        val nestedScrollConnection = remember(clearAllFocus) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y > 0 && source == NestedScrollSource.UserInput) {
                        clearAllFocus()
                    }
                    return Offset.Zero
                }
            }
        }

        // Гарантированное скрытие клавиатуры при уничтожении шторки
        DisposableEffect(clearAllFocus) {
            onDispose {
                clearAllFocus()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .pointerInput(clearAllFocus) {
                    detectTapGestures(onTap = { clearAllFocus() })
                }
                .onSizeChanged { size ->
                    if (size.height > 0) {
                        sheetHeightPx = size.height.toFloat()
                    }
                }
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    titleLeadingContent?.invoke(this)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                titleTrailingContent?.invoke(this)
            }

            // Контент формы
            val dismissSheet = remember(animateDismiss) { { animateDismiss(null) } }
            CompositionLocalProvider(LocalAppFormSheetDismiss provides dismissSheet) {
                content(clearAllFocus)
            }

            // Подвал шторки: кнопки «Назад» и действия
            val hasCancel = cancelButtonText != null
            val hasPrimary = primaryButtonText != null
            if (hasCancel || hasPrimary) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Кнопка отмены
                    if (hasCancel) {
                        AppSecondaryButton(
                            text = cancelButtonText,
                            onClick = { animateDismiss(null) },
                            modifier = if (hasPrimary) Modifier.weight(1f) else Modifier.fillMaxWidth()
                        )
                    }

                    // Кнопка действия
                    if (hasPrimary) {
                        AppPrimaryButton(
                            text = primaryButtonText,
                            onClick = {
                                clearAllFocus()
                                val shouldDismiss = onPrimaryClick?.invoke() ?: true
                                if (shouldDismiss) {
                                    animateDismiss(null)
                                }
                            },
                            enabled = isPrimaryEnabled,
                            modifier = if (hasCancel) Modifier.weight(1.3f) else Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
