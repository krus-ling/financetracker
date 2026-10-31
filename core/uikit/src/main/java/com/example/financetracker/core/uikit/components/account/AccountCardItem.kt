package com.example.financetracker.core.uikit.components.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.core.uikit.components.category.CategoryIcon
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val RuAccountSymbols = DecimalFormatSymbols(Locale.forLanguageTag("ru")).apply {
    groupingSeparator = ' ' // разделять тысячи пробелом
    decimalSeparator = ',' // в качестве разделителя целой и дробной части использовать ,
}

/**
 * Карточка счёта для горизонтальной карусели на главном экране.
 */
@Composable
fun AccountCardItem(
    title: String, // Текстовое название счёта
    balance: Double, // Числовое значение баланса
    accountIcon: ImageVector, // Иконка, которая будет отображаться в левом верхнем углу карточки
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface, // Цвет фона самой карточки
    onClick: () -> Unit // Функция обратного вызова
) {
    // Оформление чисел по шаблону: если после запятой "00", скрываем их
    val formattedBalance = remember(balance) {
        val decimalFormat = DecimalFormat("#,##0.00", RuAccountSymbols)
        val formatted = decimalFormat.format(balance)
        if (formatted.endsWith(",00")) {
            formatted.removeSuffix(",00")
        } else {
            formatted
        }
    }

    Card(
        onClick = onClick, // делает интерактивной
        modifier = modifier
            .width(160.dp) // фикс размеры
            .height(100.dp),
        shape = RoundedCornerShape(16.dp), // скругление углов
        colors = CardDefaults.cardColors(containerColor = containerColor), // цвет фона
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // все пространство занять
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween // распределить расстояние между 1 и последним компонентом
        ) {
            // Иконка в левом верхнем углу
            CategoryIcon(
                icon = accountIcon,
                backgroundColor = PrimaryTint,
                iconTint = Primary, // цвет иконки
                size = 32.dp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp) // отступ между названием и цифрами
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal // без жирности
                    ),
                    maxLines = 1 // текст занимает не больше 1 строки
                )
                Text(
                    text = "$formattedBalance ₽", // передаем строку с балансом
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, // жирный
                        color = MaterialTheme.colorScheme.onSurface
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
            balance = 86780.0,
            accountIcon = Icons.Default.CreditCard,
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
            accountIcon = Icons.Default.CreditCard,
            onClick = {}
        )
    }
}
