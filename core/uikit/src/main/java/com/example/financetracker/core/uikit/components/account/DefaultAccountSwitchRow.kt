package com.example.financetracker.core.uikit.components.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
/**
 * Универсальная строка с переключателем статуса «Счёт по умолчанию».
 * Используется в шторках создания ([AddAccount]) и редактирования ([EditAccountBottomSheet]) счёта.
 *
 * @param isDefault Текущее состояние переключателя.
 * @param onCheckedChange Вызывается при нажатии на строку или сам [Switch].
 * @param modifier Модификатор контейнера строки.
 */
@Composable
fun DefaultAccountSwitchRow(
    isDefault: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isDefault) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Счёт по умолчанию",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Будет автоматически выбираться для новых операций",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = isDefault,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
@Preview(name = "Light Theme (Checked)", showBackground = true)
@Composable
private fun DefaultAccountSwitchRowLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        DefaultAccountSwitchRow(
            isDefault = true,
            onCheckedChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
@Preview(name = "Dark Theme (Unchecked)", showBackground = true)
@Composable
private fun DefaultAccountSwitchRowDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        DefaultAccountSwitchRow(
            isDefault = false,
            onCheckedChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}