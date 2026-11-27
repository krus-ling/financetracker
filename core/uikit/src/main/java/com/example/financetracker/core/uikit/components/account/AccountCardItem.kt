package com.example.financetracker.core.uikit.components.account

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import com.example.financetracker.core.uikit.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.core.uikit.components.icon.AppCircleIcon
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint
import com.example.financetracker.core.uikit.theme.StarGold
import com.example.financetracker.core.uikit.util.AmountFormatter


/**
 * Карточка счёта для горизонтальной карусели на главном экране.
 */
@Composable
fun AccountCardItem(
    title: String, // Текстовое название счёта
    balance: Double, // Числовое значение баланса
    modifier: Modifier = Modifier.width(160.dp),
    @DrawableRes accountIcon: Int = R.drawable.ic_account_card,
    isSelected: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surface, // Цвет фона самой карточки
    contentColor: Color? = null, // параметр для цвета текста
    onClick: () -> Unit, // Функция обратного вызова
    isDefault: Boolean = false,
) {
    val formattedBalance = remember(balance) {
        AmountFormatter.format(balance)
    }

    // Если цвет текста не передан явно, берем стандартный из темы
    val resolvedTitleColor = contentColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    val resolvedBalanceColor = contentColor ?: MaterialTheme.colorScheme.onSurface

    Card(
        onClick = onClick, // делает интерактивной
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp), // скругление углов
        colors = CardDefaults.cardColors(containerColor = containerColor), // цвет фона
        // Добавляем обводку (рамку), если карточка выбрана
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            // Если не выбрана, можно сделать тонкую  рамку
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // все пространство занять
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween // распределить расстояние между 1 и последним компонентом
        ) {
            // Делаем фон иконки темнее/насыщеннее, если карточка выбрана, чтобы она не сливалась
            val iconBgColor = if (isSelected) {
                Primary.copy(alpha = 0.2f) // зелёный оттенок для выбранной карточки
            } else {
                PrimaryTint
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка счета
                AppCircleIcon(
                    iconRes = accountIcon,
                    backgroundColor = iconBgColor,
                    iconTint = Primary, // цвет иконки
                    size = 32.dp
                )

                // Звездочка для счета по умолчанию
                if (isDefault) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star_filled),
                        contentDescription = "Счёт по умолчанию",
                        tint = StarGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }


            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp) // отступ между названием и цифрами
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = resolvedTitleColor, // Применяем цвет
                        fontWeight = FontWeight.Normal // без жирности
                    ),
                    maxLines = 1 // текст занимает не больше 1 строки
                )
                Text(
                    text = "$formattedBalance ₽", // передаем строку с балансом
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, // жирный
                        color = resolvedBalanceColor // Применяем цвет
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
fun AccountCardItemLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AccountCardItem(
            title = "Основная карта",
            balance = -86780.0,
            accountIcon = R.drawable.ic_account_card,
            isDefault = true,
            onClick = {}
        )
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
fun AccountCardItemDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        AccountCardItem(
            title = "Основная карта",
            balance = 86780.05,
            accountIcon = R.drawable.ic_account_savings,
            onClick = {}
        )
    }
}
