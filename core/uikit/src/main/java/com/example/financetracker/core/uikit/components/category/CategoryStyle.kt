package com.example.financetracker.core.uikit.components.category

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.financetracker.core.uikit.R

/**
 * Описание визуального стиля категории: иконка, цвет подложки и цвет контура.
 */
data class CategoryStyle(
    @DrawableRes val iconResId: Int,
    val backgroundColor: Color,
    val iconColor: Color
)

/**
 * Реестр стилей всех системных категорий.
 * Возвращает стиль по строковому ключу иконки (или дефолтный для неизвестных).
 */
object CategoryStyleRegistry {

    // Стили по умолчанию
    val Groceries = CategoryStyle(
        iconResId = R.drawable.ic_category_groceries,
        backgroundColor = Color(0xFFD3E7E0),
        iconColor = Color(0xFF0F5C4A)
    )
    val Cafe = CategoryStyle(
        iconResId = R.drawable.ic_category_cafe,
        backgroundColor = Color(0xFFFAEEE1),
        iconColor = Color(0xFFE0892B)
    )
    val Transport = CategoryStyle(
        iconResId = R.drawable.ic_category_transport,
        backgroundColor = Color(0xFFE7EEFA),
        iconColor = Color(0xFF3A74D6)
    )
    val Housing = CategoryStyle(
        iconResId = R.drawable.ic_category_housing,
        backgroundColor = Color(0xFFF1EBF8),
        iconColor = Color(0xFF8B5CC7)
    )
    val Internet = CategoryStyle(
        iconResId = R.drawable.ic_category_internet,
        backgroundColor = Color(0xFFE4F3F5),
        iconColor = Color(0xFF1E9BB0)
    )
    val Health = CategoryStyle(
        iconResId = R.drawable.ic_category_health,
        backgroundColor = Color(0xFFF9EBE9),
        iconColor = Color(0xFFD4594D)
    )
    val Entertainment = CategoryStyle(
        iconResId = R.drawable.ic_category_entertainment,
        backgroundColor = Color(0xFFF7EAF1),
        iconColor = Color(0xFFC4508F)
    )
    val Clothing = CategoryStyle(
        iconResId = R.drawable.ic_category_clothing,
        backgroundColor = Color(0xFFF6F0DC),
        iconColor = Color(0xFFC9A227)
    )
    val Gifts = CategoryStyle(
        iconResId = R.drawable.ic_category_gifts,
        backgroundColor = Color(0xFFEAEDE3),
        iconColor = Color(0xFF6D7F3A)
    )
    val Other = CategoryStyle(
        iconResId = R.drawable.ic_category_other,
        backgroundColor = Color(0xFFECEEED),
        iconColor = Color(0xFF7C8784)
    )
    val Salary = CategoryStyle(
        iconResId = R.drawable.ic_category_salary,
        backgroundColor = Color(0xFFE2EFEB),
        iconColor = Color(0xFF0F7A5F)
    )
    val Freelance = CategoryStyle(
        iconResId = R.drawable.ic_category_freelance,
        backgroundColor = Color(0xFFE7EEFA),
        iconColor = Color(0xFF3A74D6)
    )
    val Percent = CategoryStyle(
        iconResId = R.drawable.ic_category_percent,
        backgroundColor = Color(0xFFE4F3F5),
        iconColor = Color(0xFF1E9BB0)
    )
    val Cashback = CategoryStyle(
        iconResId = R.drawable.ic_category_cashback,
        backgroundColor = Color(0xFFF6F0DC),
        iconColor = Color(0xFFC9A227)
    )
    val Settings = CategoryStyle(
        iconResId = R.drawable.ic_category_settings,
        backgroundColor = Color.Transparent,
        iconColor = Color(0xFF8A928F)
    )

    /**
     * Поиск стиля по строковому идентификатору iconRes (сохраняется в БД).
     * Поддерживает как новые имена (ic_category_*), так и старые (shopping_cart, restaurant).
     */
    fun getStyle(iconRes: String?): CategoryStyle {
        return when (iconRes) {
            "ic_category_groceries", "shopping_cart" -> Groceries
            "ic_category_cafe", "restaurant" -> Cafe
            "ic_category_transport", "directions_bus" -> Transport
            "ic_category_housing", "home" -> Housing
            "ic_category_internet", "wifi" -> Internet
            "ic_category_health", "favorite", "health" -> Health
            "ic_category_entertainment", "theater_comedy" -> Entertainment
            "ic_category_clothing", "shirt" -> Clothing
            "ic_category_gifts", "gift" -> Gifts
            "ic_category_salary", "work" -> Salary
            "ic_category_freelance", "laptop" -> Freelance
            "ic_category_percent" -> Percent
            "ic_category_cashback", "coins" -> Cashback
            "ic_category_settings" -> Settings
            "ic_category_other", "more_horiz", "other" -> Other
            else -> Other
        }
    }
}