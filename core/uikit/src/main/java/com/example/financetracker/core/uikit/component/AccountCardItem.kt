package com.example.financetracker.core.uikit.component

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.core.uikit.theme.InkPrimary
import com.example.financetracker.core.uikit.theme.InkSecondary
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint
import com.example.financetracker.core.uikit.theme.SurfaceLight
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Карточка счёта для горизонтальной карусели на главном экране.
 */
@Composable
fun AccountCardItem(
    accountName: String, //Текстовое название счёта
    balance: Double, //Числовое значение баланса
    accountIcon: ImageVector, //Иконка, которая будет отображаться в левом верхнем углу карточки
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceLight, //Цвет фона самой карточки
    onClick: () -> Unit //Функция обратного вызова
) {
    //отвечает только за внешний вид знаков
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("ru")).apply {
        groupingSeparator = ' ' //разделять тысячи пробелом
        decimalSeparator = ',' //в качестве разделителя целой и дробной части использовать ,
    }
    //оформление чисел по шаблону (структура числа)
    val decimalFormat = DecimalFormat("#,##0.00", symbols)
    //применение к балансу
    val formattedBalance = decimalFormat.format(balance)

    Card(
        onClick = onClick, //делает иинтерактивной
        modifier = modifier
            .width(160.dp) //фикс размеры
            .height(100.dp),
        shape = RoundedCornerShape(16.dp), //скругление кглов
        colors = CardDefaults.cardColors(containerColor = containerColor), //цвет фона
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() //все пространство занять
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween //распределить расстояние между 1 и послежним компонентом
        ) {
            // Иконка в левом верхнем углу
            CategoryIcon(
                icon = accountIcon,
                backgroundColor = PrimaryTint,
                iconTint = Primary, //цвет иконки
                size = 32.dp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp) //отступ между названием и цифрами
            ) {
                Text(
                    text = accountName,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = InkSecondary,
                        fontWeight = FontWeight.Normal //без жирности
                    ),
                    maxLines = 1 //текст занимает не больше 1 строки
                )
                Text(
                    text = "$formattedBalance ₽", //передаем строку с балансом
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, //жирный
                        color = InkPrimary
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(name = "Account Card Preview", showBackground = true, backgroundColor = 0xFFE0E0E0)
@Composable
fun AccountCardItemPreview() {
    AccountCardItem(
        accountName = "Основная карта",
        balance = 86780.0,
        accountIcon = Icons.Default.CreditCard,
        onClick = {}
    )
}