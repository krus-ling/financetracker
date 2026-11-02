package com.example.financetracker.core.uikit.components.transaction

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.theme.PrimarySoft

/**
 * Сегментированный переключатель типа финансовой операции (Расход / Доход / Перевод).
 *
 * Представляет собой единую капсулу со скользящей пружинной анимацией активного индикатора.
 *
 * Архитектура и особенности:
 * - **Unidirectional Data Flow (UDF)**: компонент не хранит внутреннее состояние выбора,
 *   а принимает текущее значение [selectedType] и передаёт событие изменения через [onTypeSelected].
 * - **Адаптивная геометрия**: через [BoxWithConstraints] автоматически вычисляет ширину
 *   одного сегмента (`maxWidth / 3`) без захардкоженных размеров.
 * - **Физическая анимация**: смещение скользящей плашки анимируется пружиной [Spring]
 *   с мягким отскоком ([Spring.DampingRatioLowBouncy]).
 * - **Оптимизация производительности**: используется лямбда-оверлоад [Modifier.offset],
 *   благодаря чему чтение анимационного стейта отложено до фазы Layout (Placement),
 *   полностью исключая повторную рекомпозицию при движении плашки.
 * - **Плавная смена контраста**: цвет текста анимируется независимо через [animateColorAsState]
 *   для бесшовного перехода из приглушённого цвета темы в белый.
 *
 * @param selectedType Текущий выбранный тип транзакции ([TransactionType.EXPENSE], [TransactionType.INCOME], [TransactionType.TRANSFER]).
 * @param onTypeSelected Обработчик нажатия на сегмент с передачей нового выбранного типа.
 * @param modifier Модификатор для внешнего позиционирования и отступов.
 */
@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = remember {
        listOf(
            TransactionType.EXPENSE to "Расход",
            TransactionType.INCOME to "Доход",
            TransactionType.TRANSFER to "Перевод"
        )
    }
    val selectedIndex = types.indexOfFirst { it.first == selectedType }.coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(PrimarySoft)
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / types.size

        // Анимация движения плашки
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "indicatorOffset"
        )

        // Скользящая плашка
        Box(
            modifier = Modifier
                .width(tabWidth)
                .fillMaxHeight()
                .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Текстовые метки и кликабельные зоны поверх плашки
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            types.forEachIndexed { index, (type, label) ->
                val isSelected = selectedIndex == index

                // Плавное перетекание цвета текста
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 200),
                    label = "textColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            onTypeSelected(type)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}