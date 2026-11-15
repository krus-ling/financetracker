package com.example.financetracker.core.uikit.components.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint

/**
 * Чип категории для сетки выбора в BottomSheet (по макету).
 *
 * @param iconRes Строковый ключ иконки из базы данных (например, "ic_category_groceries").
 * @param title Название категории.
 * @param isSelected Выбрана ли категория пользователем.
 * @param onClick Обработчик клика.
 */
@Composable
fun CategoryIconChip(
    iconRes: String?,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = remember(iconRes) { CategoryStyleRegistry.getStyle(iconRes) }
    CategoryIconChip(
        style = style,
        title = title,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Перегрузка с готовым [CategoryStyle].
 */
@Composable
fun CategoryIconChip(
    style: CategoryStyle,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Фоновая подложка выделенной карточки
    val containerBg = if (isSelected) PrimaryTint else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerBg)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        // Контейнер кружка с обводкой при выборе
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = Primary,
                            shape = CircleShape
                        )
                    } else if (style.backgroundColor == Color.Transparent) {
                        // Для кнопки "Настроить" показываем пунктирную/тонкую границу
                        Modifier.border(
                            width = 1.dp,
                            color = style.iconColor.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(
                style = style,
                size = 46.dp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Название категории
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Перегрузка для обратной совместимости с ImageVector.
 */
@Composable
fun CategoryIconChip(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CategoryIconChip(
        style = CategoryStyleRegistry.Groceries,
        title = title,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}

@Preview(name = "Сетка чипов — Светлая тема", showBackground = true)
@Composable
private fun CategoryIconChipGridLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(360.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Выбор категории",
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CategoryIconChip(
                        iconRes = "ic_category_groceries",
                        title = "Продукты",
                        isSelected = true,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_cafe",
                        title = "Кафе",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_transport",
                        title = "Транспорт",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_housing",
                        title = "Жильё",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_health",
                        title = "Здоровье",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_entertainment",
                        title = "Развлечения",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_salary",
                        title = "Зарплата",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        style = CategoryStyleRegistry.Settings,
                        title = "Настроить",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Сетка чипов — Тёмная тема", showBackground = true)
@Composable
private fun CategoryIconChipGridDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(360.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Выбор категории (Dark)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CategoryIconChip(
                        iconRes = "ic_category_groceries",
                        title = "Продукты",
                        isSelected = true,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_cafe",
                        title = "Кафе",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_transport",
                        title = "Транспорт",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_housing",
                        title = "Жильё",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_health",
                        title = "Здоровье",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_entertainment",
                        title = "Развлечения",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        iconRes = "ic_category_salary",
                        title = "Зарплата",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                    CategoryIconChip(
                        style = CategoryStyleRegistry.Settings,
                        title = "Настроить",
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.width(76.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Состояния чипа (Выбран / Обычный / Настройки)", showBackground = true)
@Composable
private fun CategoryIconChipStatesPreview() {
    FinancetrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .width(280.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryIconChip(
                    iconRes = "ic_category_groceries",
                    title = "Выбран",
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CategoryIconChip(
                    iconRes = "ic_category_groceries",
                    title = "Обычный",
                    isSelected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CategoryIconChip(
                    style = CategoryStyleRegistry.Settings,
                    title = "Настроить",
                    isSelected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

