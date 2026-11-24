package com.example.financetracker.core.uikit.components.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme

@Composable
fun AccountActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(name = "AccountActionButton - Light Theme", showBackground = true)
@Composable
private fun AccountActionButtonLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountActionButton(
                title = "Расход",
                icon = Icons.Default.ArrowDownward,
                onClick = {}
            )
            AccountActionButton(
                title = "Доход",
                icon = Icons.Default.ArrowUpward,
                onClick = {}
            )
            AccountActionButton(
                title = "Перевод",
                icon = Icons.Default.SwapHoriz,
                onClick = {}
            )
        }
    }
}

@Preview(name = "AccountActionButton - Dark Theme", showBackground = true)
@Composable
private fun AccountActionButtonDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountActionButton(
                title = "Расход",
                icon = Icons.Default.ArrowDownward,
                onClick = {}
            )
            AccountActionButton(
                title = "Доход",
                icon = Icons.Default.ArrowUpward,
                onClick = {}
            )
            AccountActionButton(
                title = "Перевод",
                icon = Icons.Default.SwapHoriz,
                onClick = {}
            )
        }
    }
}