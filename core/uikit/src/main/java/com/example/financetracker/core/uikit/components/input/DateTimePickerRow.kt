package com.example.financetracker.core.uikit.components.input

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.components.category.CategoryIcon
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Универсальный компонент строки для выбора даты и времени.
 * Отображает иконку календаря, подпись, текущее значение и стрелочку перехода.
 */
@Composable
fun DateTimePickerRow(
    dateTime: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Дата и время"
) {
    val dateText = remember(dateTime) {
        formatDateTime(dateTime)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = Icons.Default.DateRange,
                backgroundColor = PrimaryTint,
                iconTint = Primary,
                size = 36.dp
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Преобразует миллисекунды в понятную строку: «Сегодня, 14:20», «Вчера, 18:00» или «2 ноября, 15:30».
 */
private fun formatDateTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru"))
    val timeString = timeFormat.format(Date(timestamp))
    return when {
        now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> {
            "Сегодня, $timeString"
        }
        now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1 -> {
            "Вчера, $timeString"
        }
        else -> {
            val fullFormat = SimpleDateFormat("d MMMM, HH:mm", Locale.forLanguageTag("ru"))
            fullFormat.format(Date(timestamp))
        }
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun DateTimePickerRowLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        DateTimePickerRow(
            dateTime = System.currentTimeMillis(),
            onClick = {}
        )
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun DateTimePickerRowDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DateTimePickerRow(
                dateTime = System.currentTimeMillis(),
                onClick = {}
            )
        }
    }
}