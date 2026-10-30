package com.example.financetracker.core.uikit.components.sheet

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Универсальная шторка (bottom sheet) для форм ввода данных.
 *
 * Инкапсулирует:
 * - Вызов Material 3 ModalBottomSheet со скруглением 24.dp и drag-хэндлом;
 * - Плавную асимметричную анимацию подъёма контента вместе с клавиатурой (400 мс / 280 мс);
 * - Снятие фокуса при закрытии клавиатуры системной кнопкой/жестом «Назад»;
 * - Мгновенное скрытие клавиатуры при свайпе шторки вниз (nestedScroll и отслеживание сдвига);
 * - Гарантированное скрытие клавиатуры при dismiss/dispose;
 * - Снятие фокуса при тапе по фону;
 * - Скроллируемый контейнер Column с системными отступами;
 * - Заголовок и стандартные кнопки действий (основная и отмена).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppFormBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    primaryButtonText: String? = null,
    onPrimaryClick: (() -> Unit)? = null,
    isPrimaryEnabled: Boolean = true,
    cancelButtonText: String? = "Отмена",
    content: @Composable ColumnScope.(clearFocus: () -> Unit) -> Unit
) {
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        fun clearAllFocus() {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }

        // Отслеживаем системное закрытие клавиатуры (например, по кнопке Назад)
        val isImeVisible = WindowInsets.isImeVisible
        var wasImeOpened by remember { mutableStateOf(false) }

        LaunchedEffect(isImeVisible) {
            if (isImeVisible) {
                wasImeOpened = true
            } else if (wasImeOpened) {
                focusManager.clearFocus(force = true)
                wasImeOpened = false
            }
        }

        // Скрываем клавиатуру сразу же, как только шторка начинает закрываться
        LaunchedEffect(sheetState) {
            snapshotFlow { sheetState.targetValue }
                .collect { target ->
                    if (target == SheetValue.Hidden) {
                        clearAllFocus()
                    }
                }
        }

        // Отслеживаем сдвиг шторки вниз при свайпе за ручку или шапку
        var settledOffset by remember { mutableStateOf<Float?>(null) }

        LaunchedEffect(sheetState) {
            snapshotFlow {
                if (!sheetState.isAnimationRunning && sheetState.currentValue == SheetValue.Expanded) {
                    runCatching { sheetState.requireOffset() }.getOrNull()
                } else null
            }.collect { offset ->
                if (offset != null) {
                    settledOffset = offset
                }
            }
        }

        LaunchedEffect(sheetState) {
            snapshotFlow {
                runCatching { sheetState.requireOffset() }.getOrNull()
            }.collect { currentOffset ->
                val base = settledOffset
                if (currentOffset != null && base != null && currentOffset > base + 15f) {
                    clearAllFocus()
                }
            }
        }

        // Перехватываем свайп контента шторки вниз для мгновенного скрытия клавиатуры
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y > 0 && source == NestedScrollSource.UserInput) {
                        clearAllFocus()
                    }
                    return Offset.Zero
                }
            }
        }

        // Гарантированно скрываем клавиатуру при удалении шторки
        DisposableEffect(Unit) {
            onDispose {
                clearAllFocus()
            }
        }

        // Плавная анимация подъема контента вровень с клавиатурой
        val density = LocalDensity.current
        val imeBottom = WindowInsets.ime.getBottom(density)
        val imeBottomDp = with(density) { imeBottom.toDp() }
        val animatedImePadding by animateDpAsState(
            targetValue = imeBottomDp,
            animationSpec = if (imeBottomDp > 0.dp) {
                tween(
                    durationMillis = 400,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                )
            } else {
                tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                )
            },
            label = "formSheetImePadding"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(bottom = animatedImePadding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    clearAllFocus()
                }
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Специфический контент формы
            content(::clearAllFocus)

            // Основная кнопка действия
            if (primaryButtonText != null) {
                Button(
                    onClick = {
                        clearAllFocus()
                        onPrimaryClick?.invoke()
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
                        clearAllFocus()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(cancelButtonText)
                }
            }
        }
    }
}

