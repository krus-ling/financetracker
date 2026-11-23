package com.example.financetracker.core.uikit.components.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme

@Composable
fun DeletedAccountBanner(
    modifier: Modifier = Modifier,
    text: String = "Счёт находится в корзине. Операции доступны только для просмотра."
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "DeletedAccountBanner - Light", showBackground = true)
@Composable
private fun DeletedAccountBannerLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        DeletedAccountBanner(modifier = Modifier.padding(16.dp))
    }
}
@Preview(name = "DeletedAccountBanner - Dark", showBackground = true)
@Composable
private fun DeletedAccountBannerDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        DeletedAccountBanner(modifier = Modifier.padding(16.dp))
    }
}