package com.example.financetracker.feature.profile.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.Expense
import com.example.financetracker.core.uikit.theme.ExpenseSoft
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.PrimarySoft

@Composable
fun ProfileItemCard(
    title: String, //заголовок пункта
    subtitle: String, //текст подзаголовка
    icon: ImageVector, //векторная иконка
    onClick: () -> Unit, //параметр, который выполнится при нажатии на карточку
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary, //цвет иконки по умолчанию
    iconBackgroundColor: Color = PrimarySoft, //цвет кругляшка-подложки за иконкой
    showDivider: Boolean = false //флаг: нужно ли рисовать тонкую разделительную линию под этой карточкой
) {
    Column(modifier = modifier.fillMaxWidth()) { //всё, что находится внутри него, выстраивается сверху вниз
        Row(
            modifier = Modifier
                .fillMaxWidth() //делает этот ряд на всю ширину
                .clickable(onClick = onClick) //делает всю строку интерактивной
                .padding(horizontal = 16.dp, vertical = 12.dp), //16dp слева/справа и 12dp сверху/снизу
            verticalAlignment = Alignment.CenterVertically, //по центру
            horizontalArrangement = Arrangement.spacedBy(14.dp) //между всеми элементами расстояние в 14dp
        ) {
            // Круглая подложка под иконку
            Box(
                modifier = Modifier
                    .size(42.dp) //размер подложки иконки: квадратик 42 на 42 dp
                    .clip(CircleShape) //обрезает этот квадратик по форме круга
                    .background(iconBackgroundColor), //круг цветом фона
                contentAlignment = Alignment.Center //центрирует иконку
            ) {
                Icon(
                    imageVector = icon, //передает саму иконку
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp) //задает размер рисунка иконки 22 на 22 dp
                )
            }

            // Текстовый блок: Заголовок и подзаголовок
            Column(
                modifier = Modifier.weight(1f) //Занимает всё оставшееся свободное пространство в строке между иконкой слева и стрелочкой справа
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium, //средний стиль заголовка
                    fontWeight = FontWeight.SemiBold, //полужирный текст
                    color = MaterialTheme.colorScheme.onSurface //задает цвет текста в зависимости от темы (в светлой теме — темно-серый/чёрный, в темной — светлый)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall, //более мелкий шрифт
                    color = MaterialTheme.colorScheme.onSurfaceVariant //красит подзаголовок в приглушенный цвет
                )
            }

            // Стрелочка вправо
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, //иконка стрелочки «вперед»
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), //цвет стрелочки из цвета второстепенного текста, но с прозрачностью 0.5f
                modifier = Modifier.size(14.dp) //фиксирует размер стрелочки 14 на 14 dp
            )
        }

        // Тонкий разделитель с отступом слева (чтобы не заходить под иконку)
        if (showDivider) { //если элемент не последний в группе, то рисуем полоску
            HorizontalDivider( //линия-разделитель
                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                //сдвигает линию на 72dp вправо от начала строки
                // Этот отступ равен ширине иконки с её отступами,
                // поэтому линия начинается под текстом и не пересекает иконку слева
                thickness = 1.dp, //линия толщиной в 1 пиксель
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) //цвет границы берется полупрозрачностью
            )
        }
    }
}

@Preview(name = "1. Категории (Светлая)", showBackground = true)
@Composable
private fun ProfileItemCategoriesPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ProfileItemCard(
                title = "Категории",
                subtitle = "Расходы и доходы",
                icon = Icons.Filled.Category,
                onClick = {}
            )
        }
    }
}

@Preview(name = "2. Корзина (Акцент удаления)", showBackground = true)
@Composable
private fun ProfileItemTrashPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ProfileItemCard(
                title = "Корзина",
                subtitle = "Удалённые счета и операции",
                icon = Icons.Filled.DeleteOutline,
                iconTint = Expense,
                iconBackgroundColor = ExpenseSoft,
                onClick = {}
            )
        }
    }
}

@Preview(
    name = "3. Уведомления (Тёмная тема)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileItemDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ProfileItemCard(
                title = "Уведомления",
                subtitle = "Бюджеты, синхронизация, напоминания",
                icon = Icons.Filled.Notifications,
                onClick = {}
            )
        }
    }
}

@Preview(name = "4. Группа в карточке (Как в макете)", showBackground = true)
@Composable
private fun ProfileItemGroupPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                ProfileItemCard(
                    title = "Счета",
                    subtitle = "4 счёта",
                    icon = Icons.Filled.AccountBalanceWallet,
                    onClick = {},
                    showDivider = true
                )
                ProfileItemCard(
                    title = "Категории и бюджеты",
                    subtitle = "Расходы и доходы",
                    icon = Icons.Filled.Category,
                    onClick = {},
                    showDivider = true
                )
                ProfileItemCard(
                    title = "Экспорт данных",
                    subtitle = "CSV или JSON",
                    icon = Icons.Filled.Download,
                    onClick = {},
                    showDivider = false
                )
            }
        }
    }
}