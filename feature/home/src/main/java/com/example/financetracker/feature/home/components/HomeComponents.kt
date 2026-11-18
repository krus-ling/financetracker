package com.example.financetracker.feature.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.account.AccountCardItem
import com.example.financetracker.core.uikit.components.transaction.TransactionRowItem
import com.example.financetracker.core.uikit.theme.TransferBlue
import com.example.financetracker.core.uikit.theme.TransferSoft

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
    modifier: Modifier = Modifier,
    targetAccount: Account? = null,
) {
    val accountName = account?.name ?: "Счёт удален"
    val targetAccountName = targetAccount?.name
    val note = transaction.note?.trim()

    val title: String
    val subtitle: String

    when (transaction.type) {

        TransactionType.INCOME -> {
            title = accountName
            subtitle = when {
                !note.isNullOrEmpty() && category != null -> "${category.name} · $note"
                !note.isNullOrEmpty() -> note
                category != null -> category.name
                else -> ""
            }

            TransactionRowItem(
                categoryName = title,
                categoryDetails = subtitle,
                amount = transaction.amount,
                type = transaction.type,
                iconRes = category?.iconRes ?: "ic_category_salary",
                onClick = onClick,
                modifier = modifier
            )
        }

        TransactionType.EXPENSE -> {
            title = category?.name ?: "Разное"
            subtitle = if (!note.isNullOrEmpty()) "$accountName · $note" else accountName

            TransactionRowItem(
                categoryName = title,
                categoryDetails = subtitle,
                amount = transaction.amount,
                type = transaction.type,
                iconRes = category?.iconRes ?: "ic_category_other",
                onClick = onClick,
                modifier = modifier
            )
        }

        TransactionType.TRANSFER -> {
            title = "Перевод"
            val transferRoute = if (!targetAccountName.isNullOrEmpty()) {
                "$accountName -> $targetAccountName"
            } else {
                accountName
            }
            subtitle = if (!note.isNullOrEmpty()) "$transferRoute · $note" else transferRoute

            TransactionRowItem(
                categoryName = title,
                categoryDetails = subtitle,
                amount = transaction.amount,
                type = transaction.type,
                categoryIcon = Icons.Default.SwapHoriz,
                iconBackgroundColor = TransferSoft,
                iconTint = TransferBlue,
                onClick = onClick,
                modifier = modifier
            )
        }
    }
}