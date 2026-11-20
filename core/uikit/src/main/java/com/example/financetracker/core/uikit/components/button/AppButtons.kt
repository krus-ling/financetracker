package com.example.financetracker.core.uikit.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimarySoft

/**
 * Основная кнопка действия (Primary) в форме округлой пилюли.
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Второстепенная кнопка (Secondary) в форме пилюли с мягким фоном.
 * По умолчанию используется для возврата («Назад»).
 */
@Composable
fun AppSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Назад",
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimarySoft, // Мягкий мятно-серый фон
            contentColor = Primary,       // Тёмно-зелёный текст
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Компактная кнопка действия (Mini Action) с иконкой и текстом.
 * Используется для быстрой цепочки действий (например, «Добавить»).
 */
@Composable
fun AppMiniActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Добавить",
    icon: ImageVector? = Icons.Default.Check,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        modifier = modifier.height(34.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(name = "AppPrimaryButton - Light Theme", showBackground = true)
@Composable
private fun AppPrimaryButtonLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppPrimaryButton(
                    text = "Сохранить",
                    onClick = {}
                )
                AppPrimaryButton(
                    text = "Сохранить",
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}

@Preview(name = "AppPrimaryButton - Dark Theme", showBackground = true)
@Composable
private fun AppPrimaryButtonDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppPrimaryButton(
                    text = "Сохранить",
                    onClick = {}
                )
                AppPrimaryButton(
                    text = "Сохранить",
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}

@Preview(name = "AppSecondaryButton - Light Theme", showBackground = true)
@Composable
private fun AppSecondaryButtonLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppSecondaryButton(
                    text = "Назад",
                    onClick = {}
                )
                AppSecondaryButton(
                    text = "Назад",
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}

@Preview(name = "AppSecondaryButton - Dark Theme", showBackground = true)
@Composable
private fun AppSecondaryButtonDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppSecondaryButton(
                    text = "Назад",
                    onClick = {}
                )
                AppSecondaryButton(
                    text = "Назад",
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}

@Preview(name = "AppMiniActionButton - Light Theme", showBackground = true)
@Composable
private fun AppMiniActionButtonLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppMiniActionButton(
                    onClick = {},
                    text = "Добавить"
                )
                AppMiniActionButton(
                    onClick = {},
                    text = "Без иконки",
                    icon = null
                )
                AppMiniActionButton(
                    onClick = {},
                    text = "Добавить",
                    enabled = false
                )
            }
        }
    }
}

@Preview(name = "AppMiniActionButton - Dark Theme", showBackground = true)
@Composable
private fun AppMiniActionButtonDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppMiniActionButton(
                    onClick = {},
                    text = "Добавить"
                )
                AppMiniActionButton(
                    onClick = {},
                    text = "Без иконки",
                    icon = null
                )
                AppMiniActionButton(
                    onClick = {},
                    text = "Добавить",
                    enabled = false
                )
            }
        }
    }
}
