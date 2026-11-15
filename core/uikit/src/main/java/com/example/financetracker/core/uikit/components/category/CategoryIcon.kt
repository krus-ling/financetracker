package com.example.financetracker.core.uikit.components.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme

/**
 * Основной компонент: отрисовывает круглую плашку категории по [CategoryStyle].
 */
@Composable
fun CategoryIcon(
    style: CategoryStyle,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    CategoryIcon(
        painter = painterResource(id = style.iconResId),
        backgroundColor = style.backgroundColor,
        iconTint = style.iconColor,
        modifier = modifier,
        size = size
    )
}

/**
 * Перегрузка по строковому ключу [iconRes].
 * Если цвета не указаны — берутся из [CategoryStyleRegistry].
 */
@Composable
fun CategoryIcon(
    iconRes: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    iconTint: Color? = null,
    size: Dp = 44.dp
) {
    val style = CategoryStyleRegistry.getStyle(iconRes)
    CategoryIcon(
        painter = painterResource(id = style.iconResId),
        backgroundColor = backgroundColor ?: style.backgroundColor,
        iconTint = iconTint ?: style.iconColor,
        modifier = modifier,
        size = size
    )
}

/**
 * Базовый компонент с [Painter] (для XML-векторов и картинок).
 */
@Composable
fun CategoryIcon(
    painter: Painter,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

/**
 * Перегрузка с [ImageVector] (для обратной совместимости с системными иконками Material Icons).
 */
@Composable
fun CategoryIcon(
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    CategoryIcon(
        painter = rememberVectorPainter(image = icon),
        backgroundColor = backgroundColor,
        iconTint = iconTint,
        modifier = modifier,
        size = size
    )
}

/**
 * Вспомогательный маппер для мест, где всё ещё требуется [ImageVector] (например, в дефолтных fallback).
 */
fun getCategoryIcon(iconRes: String?): ImageVector = when (iconRes) {
    "shopping_cart", "ic_category_groceries" -> Icons.Default.ShoppingCart
    "restaurant", "ic_category_cafe" -> Icons.Default.Restaurant
    "directions_bus", "ic_category_transport" -> Icons.Default.DirectionsBus
    "work", "ic_category_salary" -> Icons.Default.Work
    "swap_horiz" -> Icons.Default.SwapHoriz
    "home", "ic_category_housing" -> Icons.Default.Home
    "theater_comedy", "ic_category_entertainment" -> Icons.Default.TheaterComedy
    "favorite", "ic_category_health" -> Icons.Default.Favorite
    "more_horiz", "ic_category_other" -> Icons.Default.MoreHoriz
    else -> Icons.Default.ShoppingCart
}

@Preview(name = "Все категории — Светлая тема", showBackground = true)
@Composable
private fun AllCategoryIconsLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Все категории (Light)",
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryIcon(style = CategoryStyleRegistry.Groceries)
                    CategoryIcon(style = CategoryStyleRegistry.Cafe)
                    CategoryIcon(style = CategoryStyleRegistry.Transport)
                    CategoryIcon(style = CategoryStyleRegistry.Housing)
                    CategoryIcon(style = CategoryStyleRegistry.Internet)
                    CategoryIcon(style = CategoryStyleRegistry.Health)
                    CategoryIcon(style = CategoryStyleRegistry.Entertainment)
                    CategoryIcon(style = CategoryStyleRegistry.Clothing)
                    CategoryIcon(style = CategoryStyleRegistry.Gifts)
                    CategoryIcon(style = CategoryStyleRegistry.Salary)
                    CategoryIcon(style = CategoryStyleRegistry.Freelance)
                    CategoryIcon(style = CategoryStyleRegistry.Percent)
                    CategoryIcon(style = CategoryStyleRegistry.Cashback)
                    CategoryIcon(style = CategoryStyleRegistry.Settings)
                    CategoryIcon(style = CategoryStyleRegistry.Other)
                }
            }
        }
    }
}

@Preview(name = "Все категории — Тёмная тема", showBackground = true)
@Composable
private fun AllCategoryIconsDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Все категории (Dark)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryIcon(style = CategoryStyleRegistry.Groceries)
                    CategoryIcon(style = CategoryStyleRegistry.Cafe)
                    CategoryIcon(style = CategoryStyleRegistry.Transport)
                    CategoryIcon(style = CategoryStyleRegistry.Housing)
                    CategoryIcon(style = CategoryStyleRegistry.Internet)
                    CategoryIcon(style = CategoryStyleRegistry.Health)
                    CategoryIcon(style = CategoryStyleRegistry.Entertainment)
                    CategoryIcon(style = CategoryStyleRegistry.Clothing)
                    CategoryIcon(style = CategoryStyleRegistry.Gifts)
                    CategoryIcon(style = CategoryStyleRegistry.Salary)
                    CategoryIcon(style = CategoryStyleRegistry.Freelance)
                    CategoryIcon(style = CategoryStyleRegistry.Percent)
                    CategoryIcon(style = CategoryStyleRegistry.Cashback)
                    CategoryIcon(style = CategoryStyleRegistry.Settings)
                    CategoryIcon(style = CategoryStyleRegistry.Other)
                }
            }
        }
    }
}

@Preview(name = "Разные размеры иконки", showBackground = true)
@Composable
private fun CategoryIconSizesPreview() {
    FinancetrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIcon(style = CategoryStyleRegistry.Groceries, size = 32.dp)
                CategoryIcon(style = CategoryStyleRegistry.Groceries, size = 44.dp)
                CategoryIcon(style = CategoryStyleRegistry.Groceries, size = 56.dp)
                CategoryIcon(style = CategoryStyleRegistry.Groceries, size = 68.dp)
            }
        }
    }
}

@Preview(name = "По строковому ключу iconRes", showBackground = true)
@Composable
private fun CategoryIconByStringKeyPreview() {
    FinancetrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryIcon(iconRes = "ic_category_groceries")
                CategoryIcon(iconRes = "ic_category_cafe")
                CategoryIcon(iconRes = "ic_category_transport")
                CategoryIcon(iconRes = "ic_category_salary")
                CategoryIcon(iconRes = "unknown_key")
            }
        }
    }
}