package com.example.financetracker.core.uikit.components.icon

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.financetracker.core.uikit.theme.PrimarySoft

/**
 * Базовый атомарный компонент дизайн-системы: круглая иконка с цветным фоном.
 * Используется в счетах, профиле, категориях, транзакциях и пикерах.
 *
 * @param painter Источник иконки (Painter).
 * @param backgroundColor Цвет подложки кружка.
 * @param iconTint Цвет обводки/заливки иконки.
 * @param size Внешний диаметр кружка (по умолчанию 44.dp).
 * @param iconScale Пропорция размера иконки к диаметру кружка (по умолчанию 0.52f).
 */
@Composable
fun AppCircleIcon(
    painter: Painter,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconScale: Float = 0.52f
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
            modifier = Modifier.size(size * iconScale)
        )
    }
}

/**
 * Перегрузка для системных векторных иконок [ImageVector].
 */
@Composable
fun AppCircleIcon(
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconScale: Float = 0.52f
) {
    AppCircleIcon(
        painter = rememberVectorPainter(image = icon),
        backgroundColor = backgroundColor,
        iconTint = iconTint,
        modifier = modifier,
        size = size,
        iconScale = iconScale
    )
}

/**
 * Перегрузка для XML-векторов из res/drawable (@DrawableRes).
 */
@Composable
fun AppCircleIcon(
    @DrawableRes iconRes: Int,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconScale: Float = 0.52f
) {
    AppCircleIcon(
        painter = painterResource(id = iconRes),
        backgroundColor = backgroundColor,
        iconTint = iconTint,
        modifier = modifier,
        size = size,
        iconScale = iconScale
    )
}

@Preview(name = "AppCircleIcon Preview", showBackground = true)
@Composable
private fun AppCircleIconPreview() {
    AppCircleIcon(
        icon = Icons.Default.AccountBalanceWallet,
        backgroundColor = PrimarySoft,
        iconTint = MaterialTheme.colorScheme.primary,
        size = 44.dp
    )
}