package com.example.financetracker.core.uikit.components.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppFormBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    primaryButtonText: String? = null,
    onPrimaryClick: (() -> Boolean)? = null,
    isPrimaryEnabled: Boolean = true,
    cancelButtonText: String? = "Отмена",
    onSheetOpened: (() -> Unit)? = null,
    content: @Composable ColumnScope.(clearFocus: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnSheetOpened by rememberUpdatedState(onSheetOpened)

    // Флаг для предотвращения повторных вызовов анимации закрытия
    var isDismissing by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isDismissing) {
                currentOnDismiss()
            }
        },
        modifier = modifier
            .statusBarsPadding(),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
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

        val clearAllFocus: () -> Unit = remember(focusManager, keyboardController) {
            {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            }
        }

        // Плавное закрытие шторки с анимацией спуска
        val animateDismiss: (onFinished: (() -> Unit)?) -> Unit = remember(sheetState, coroutineScope) {
            { onFinished ->
                if (!isDismissing) {
                    isDismissing = true
                    clearAllFocus()
                    coroutineScope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        onFinished?.invoke()
                        if (!sheetState.isVisible) {
                            currentOnDismiss()
                        }
                    }
                }
            }
        }

        // Колбэк после завершения анимации открытия шторки (для безопасного автофокуса)
        LaunchedEffect(sheetState) {
            snapshotFlow { sheetState.currentValue == SheetValue.Expanded && !sheetState.isAnimationRunning }
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
                // Автоматически возвращаем скролл наверх к шапке при скрытии клавиатуры
                if (scrollState.value > 0) {
                    scrollState.animateScrollTo(0)
                }
            }
        }

        // Скрываем клавиатуру сразу при переходе шторки в закрытие
        LaunchedEffect(sheetState) {
            snapshotFlow { sheetState.targetValue }
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    clearAllFocus()
                }
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Контент формы
            content(clearAllFocus)

            // Основная кнопка действия
            if (primaryButtonText != null) {
                Button(
                    onClick = {
                        clearAllFocus()
                        val shouldDismiss = onPrimaryClick?.invoke() ?: true
                        if (shouldDismiss) {
                            animateDismiss(null)
                        }
                    },
                    enabled = isPrimaryEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(primaryButtonText)
                }
            }

            // Кнопка отмены
            if (cancelButtonText != null) {
                TextButton(
                    onClick = {
                        animateDismiss(null)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(cancelButtonText)
                }
            }
        }
    }
}
