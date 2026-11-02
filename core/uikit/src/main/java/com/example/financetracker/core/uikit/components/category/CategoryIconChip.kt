package com.example.financetracker.core.uikit.components.category

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimaryTint

@Composable
fun CategoryIconChip(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        // Кружок с иконкой и кольцом выделения
        Box(
            modifier = Modifier
                .size(54.dp) // внешний размер
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(
                icon = icon,
                backgroundColor = if (isSelected) { // Меняем цвета в зависимости от того, выбрана ли категория
                    Primary.copy(alpha = 0.25f) // Делаем кружок заметным и контрастным при выборе
                } else {
                    PrimaryTint
                },
                iconTint = Primary,
                size = 44.dp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun CategoryIconChipLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .width(180.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryIconChip(
                    icon = Icons.Default.ShoppingCart,
                    title = "Продукты",
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CategoryIconChip(
                    icon = Icons.Default.ShoppingCart,
                    title = "Продукты",
                    isSelected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun CategoryIconChipDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .width(180.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryIconChip(
                    icon = Icons.Default.ShoppingCart,
                    title = "Продукты",
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CategoryIconChip(
                    icon = Icons.Default.ShoppingCart,
                    title = "Продукты",
                    isSelected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

