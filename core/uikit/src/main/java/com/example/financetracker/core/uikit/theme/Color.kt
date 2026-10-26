package com.example.financetracker.core.uikit.theme

import androidx.compose.ui.graphics.Color

/* ==========================================================================
   1. Brand & Primary Colors (Брендовые и основные цвета)
   ========================================================================== */
val Primary = Color(0xFF0F5C4A)          // Основной зеленый акцентный цвет (--color-primary)
val PrimaryStrong = Color(0xFF0A4537)    // Темно-зеленый для состояний hover/active (--color-primary-strong)
val PrimarySoft = Color(0xFFDDEEE7)      // Светлый зеленый для плашек, выбранных элементов и табов (--color-primary-soft)
val PrimaryTint = Color(0xFFEEF6F2)      // Очень светлый зеленый тинт для фонов иконки EmptyState (--color-primary-tint)
val OnPrimary = Color(0xFFFFFFFF)        // Белый текст/иконки на основном фоне (--color-on-primary)
val OnPrimary2 = Color(0xFFA9D3C4)       // Второстепенный текст на темном/брендовом фоне (--color-on-primary-2)

/* ==========================================================================
   2. Surfaces & Backgrounds (Фоны и поверхности - Светлая тема)
   ========================================================================== */
val SurfaceBgLight = Color(0xFFF6F5F1)   // Основной подложечный фон экранов (--color-bg)
val SurfaceLight = Color(0xFFFFFFFF)     // Фон карточек, диалогов и плашек (--color-color-surface)
val Surface2Light = Color(0xFFF1F0EB)   // Второстепенный фон (hover, второстепенные контейнеры) (--color-surface-2)
val Surface3Light = Color(0xFFE8E7E1)   // Фон отключенных элементов и трека прогресса (--color-surface-3)
val SurfaceStageLight = Color(0xFFE9EBE8)// Фон внешней подложки макета (--color-stage)

/* ==========================================================================
   3. Surfaces & Backgrounds (Фоны и поверхности - Темная тема)
   ========================================================================== */
val SurfaceBgDark = Color(0xFF121816)    // Основной фон экранов в темной теме
val SurfaceDark = Color(0xFF1B2220)      // Фон карточек и диалогов в темной теме
val Surface2Dark = Color(0xFF242C29)     // Второстепенный фон в темной теме
val Surface3Dark = Color(0xFF2E3733)     // Фон отключенных элементов в темной теме

/* ==========================================================================
   4. Text & Ink Colors (Цвета текста и иконок)
   ========================================================================== */
val TextPrimary = Color(0xFF1B2220)      // Основной цвет текста (также используется для расходов в AmountText)
val InkPrimary = Color(0xFF1B2220)       // Основной текст и заголовки (--color-ink)
val InkSecondary = Color(0xFF56605C)     // Второстепенный текст и подписи (--color-ink-2)
val InkTertiary = Color(0xFF8A928F)      // Третичный текст (плейсхолдеры, иконки) (--color-ink-3)
val InkDisabled = Color(0xFFB4B9B6)      // Текст и элементы в состоянии disabled (--color-ink-4)

// Аналоги для темной темы
val InkPrimaryDark = Color(0xFFE6E8E7)   // Основной текст в темной теме
val InkSecondaryDark = Color(0xFFA0A7A4) // Второстепенный текст в темной теме

/* ==========================================================================
   5. Borders & Lines (Границы и разделители)
   ========================================================================== */
val Line = Color(0xFFE7E5DF)             // Легкие границы и разделители (--color-line)
val LineStrong = Color(0xFFD4D2CB)       // Выраженные границы ввода (TextField) (--color-line-strong)
val LineDark = Color(0xFF2E3733)         // Разделители в темной теме

/* ==========================================================================
   6. Semantic Financial Colors (Семантические цвета финансовых операций)
   ========================================================================== */
// Доходы (Income)
val IncomeGreen = Color(0xFF1D8A57)      // Зеленый цвет для сумм и иконки дохода (INCOME) (--color-income)
val IncomeSoft = Color(0xFFE1F2E8)       // Светло-зеленый фон бейджей дохода (--color-income-soft)

// Расходы (Expense)
val Expense = Color(0xFFCF4237)          // Красный цвет для кнопок удаления (--color-expense)
val ExpenseSoft = Color(0xFFFBE8E6)      // Светло-красный фон бейджей и предупреждений (--color-expense-soft)

// Переводы (Transfer)
val TransferBlue = Color(0xFF2E6AD1)     // Синий цвет для операций перевода (TRANSFER) (--color-transfer)
val TransferSoft = Color(0xFFE4EDFB)     // Светло-синий фон бейджей перевода (--color-transfer-soft)

// Предупреждения (Warn)
val Warn = Color(0xFFD9821A)             // Оранжевый цвет предупреждений и оффлайн-индикатора (--color-warn)
val WarnSoft = Color(0xFFFCF0DD)         // Светло-оранжевый фон бейджей предупреждений (--color-warn-soft)

// Затемнение подложки диалогов
val Scrim = Color(0x6C121816)            // Полупрозрачное затемнение (42% opacity) (--color-scrim)

/* ==========================================================================
   7. Category Palette (Палитра категорий и сегментов аналитики)
   ========================================================================== */
val Category1 = Color(0xFF0F7A5F)        // Изумрудный (--color-cat-1)
val Category2 = Color(0xFFE0892B)        // Оранжевый (--color-cat-2)
val Category3 = Color(0xFF3A74D6)        // Синий (--color-cat-3)
val Category4 = Color(0xFF8B5CC7)        // Фиолетовый (--color-cat-4)
val Category5 = Color(0xFFD4594D)        // Кораллово-красный (--color-cat-5)
val Category6 = Color(0xFF1E9BB0)        // Бирюзовый (--color-cat-6)
val Category7 = Color(0xFFC9A227)        // Желтый / Золотистый (--color-cat-7)
val Category8 = Color(0xFFC4508F)        // Пурпурно-розовый (--color-cat-8)
val Category9 = Color(0xFF6D7F3A)        // Оливковый (--color-cat-9)
val Category10 = Color(0xFF7C8784)       // Серый (--color-cat-10)

/**
 * Список цветов категорий для динамического назначения по индексу в диаграммах и списках
 */
val CategoryPalette = listOf(
    Category1,
    Category2,
    Category3,
    Category4,
    Category5,
    Category6,
    Category7,
    Category8,
    Category9,
    Category10
)