package com.example.financetracker.core.uikit.components.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.uikit.components.text.AmountText
import com.example.financetracker.core.uikit.theme.Primary
import com.example.financetracker.core.uikit.theme.PrimarySoft
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Унифицированная карточка удалённого счёта.
 * Используется в корзине, в деталях счёта и в шторке дежавю.
 */
@Composable
fun DeletedAccountCard(
    account: Account,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круглая иконка в соответствии с типом счёта
            val icon = when (account.type) {
                AccountType.CARD -> Icons.Default.CreditCard
                AccountType.CASH -> Icons.Default.AccountBalanceWallet
                AccountType.SAVINGS -> Icons.Default.Savings
                AccountType.OTHER -> Icons.Default.Add
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val resolvedSubtitle = subtitle ?: account.deletedAt?.let {
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    "Удалён: ${dateFormat.format(Date(it))}"
                } ?: "В корзине"

                Text(
                    text = resolvedSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AmountText(
                amount = account.balance,
                type = null,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            }
        }
    }
}

@Preview(name = "DeletedAccountCard - Light Theme", showBackground = true)
@Composable
private fun DeletedAccountCardLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            DeletedAccountCard(
                account = Account(
                    id = "1",
                    name = "Основная карта",
                    balance = 43500.0,
                    currency = "RUB",
                    type = AccountType.CARD,
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis() - 86400000L
                )
            )
        }
    }
}

@Preview(name = "DeletedAccountCard - Dark Theme", showBackground = true)
@Composable
private fun DeletedAccountCardDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            DeletedAccountCard(
                account = Account(
                    id = "1",
                    name = "Основная карта",
                    balance = 43500.0,
                    currency = "RUB",
                    type = AccountType.CARD,
                    isDeleted = true,
                    deletedAt = null
                )
            )
        }
    }
}
