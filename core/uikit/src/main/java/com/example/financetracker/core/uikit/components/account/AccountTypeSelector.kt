package com.example.financetracker.core.uikit.components.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme

/**
 * Универсальный селектор типа счёта (горизонтальный ряд карточек-чипов с иконками).
 * Используется в шторках создания и редактирования счёта.
 */
@Composable
fun AccountTypeSelector(
    selectedType: AccountType,
    onTypeSelected: (AccountType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Тип счёта",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountType.entries.forEach { type ->
                val isSelected = selectedType == type
                Card(
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.width(90.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .height(84.dp)
                            .width(90.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(type.getIconRes()),
                            contentDescription = type.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = type.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun AccountTypeSelectorLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        AccountTypeSelector(
            selectedType = AccountType.CARD,
            onTypeSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun AccountTypeSelectorDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        AccountTypeSelector(
            selectedType = AccountType.CARD,
            onTypeSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}