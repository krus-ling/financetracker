package com.example.financetracker.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.Category4
import com.example.financetracker.core.uikit.theme.OnPrimary

/**
 * Круглая плашка с иконкой категории для отображения в списках операций и на аналитике.
 *
 * @param icon Векторная иконка из Material Icons или кастомных ресурсов.
 * @param backgroundColor Цвет круглого фона плашки.
 * @param iconTint Цвет самой иконки.
 * @param modifier Модификатор компонента.
 * @param size Диаметр круга (по умолчанию 44.dp).
 */
@Composable
fun CategoryIcon(
    icon: ImageVector,           // Векторная иконка
    backgroundColor: Color,      // Цвет заднего фона круга
    iconTint: Color,             // Цвет заливки иконки
    modifier: Modifier = Modifier, // Внешний модификатор для настройки отступов и поведений
    size: Dp = 44.dp             // Диаметр круга (по умолчанию 44.dp)
) {
    Box(
        modifier = modifier
            .size(size) // Задает фиксированную ширину и высоту
            .clip(CircleShape) // Превращение квадратного контейнера в круг
            .background(backgroundColor), // Заливает этот круг цветом
        contentAlignment = Alignment.Center //Расположение иконки по центру
    ) {
        Icon(
            imageVector = icon, // Передаем саму векторную иконку
            contentDescription = null, // Описание для доступности
            tint = iconTint, // Применяем цвет заливки к иконке
            modifier = Modifier.size(size * 0.5f) // Иконка пропорционально занимает половину диаметра контейнера
        )
    }
}

@Preview(name = "Category Icon Preview", showBackground = true)
@Composable
fun CategoryIconPreview() {
    CategoryIcon(
        icon = Icons.Default.ShoppingCart, // Используем стандартную доступную иконку
        backgroundColor = Category4,       // Берем готовый изумрудный цвет из Color.kt
        iconTint = OnPrimary,              // Белый цвет иконки из Color.kt
        size = 44.dp
    )
}