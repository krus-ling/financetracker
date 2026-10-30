package com.example.financetracker.feature.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.uikit.components.account.AccountCardItem
import com.example.financetracker.core.uikit.components.transaction.TransactionRowItem
import com.example.financetracker.core.uikit.components.category.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import com.example.financetracker.core.model.TransactionType

@Composable
fun HomeAccountCard(
    account: Account,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AccountCardItem(
        title = account.name,
        balance = account.balance,
        accountIcon = Icons.Default.CreditCard,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun HomeTransactionRow(
    transaction: Transaction,
    category: Category?,
    account: Account?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountName = account?.name ?: "Основная карта"
    val note = transaction.note?.trim()
    val formattedTime = SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale).format(Date(transaction.dateTime))

    val title: String
    val subtitle: String
    val icon: ImageVector

    when (transaction.type) {
        TransactionType.INCOME -> {
            title = accountName
            subtitle = when {
                !note.isNullOrEmpty() && category != null -> "${category.name} · $note"
                !note.isNullOrEmpty() -> note
                category != null -> category.name
                else -> ""
            }

            icon = if (!category?.iconRes.isNullOrEmpty()) {
                getCategoryIcon(category.iconRes)
            } else {
                Icons.AutoMirrored.Filled.TrendingUp
            }
        }

        TransactionType.EXPENSE -> {
            title = category?.name ?: "Разное"
            subtitle = if (!note.isNullOrEmpty()) "$accountName · $note" else accountName
            icon = getCategoryIcon(category?.iconRes)
        }

        TransactionType.TRANSFER -> {
            title = category?.name ?: "Перевод"
            subtitle = if (!note.isNullOrEmpty()) "$accountName · $note" else accountName
            icon = Icons.Default.SwapHoriz
        }
    }

    TransactionRowItem(
        categoryName = title,
        categoryDetails = subtitle,
        amount = transaction.amount,
        type = transaction.type,
        time = formattedTime,
        categoryIcon = icon,
        onClick = onClick,
        modifier = modifier
    )
}