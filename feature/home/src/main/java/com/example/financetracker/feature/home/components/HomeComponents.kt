package com.example.financetracker.feature.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.uikit.component.AccountCardItem
import com.example.financetracker.core.uikit.component.TransactionRowItem
import com.example.financetracker.core.uikit.component.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale

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
    val categoryName = category?.name ?: "Разное"
    val note = transaction.note?.trim()
    val categoryDetails = if (!note.isNullOrEmpty()) "$accountName · $note" else accountName

    val formattedTime = SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale).format(Date(transaction.dateTime))

    TransactionRowItem(
        categoryName = categoryName,
        categoryDetails = categoryDetails,
        amount = transaction.amount,
        type = transaction.type,
        time = formattedTime,
        categoryIcon = getCategoryIcon(category?.iconRes),
        onClick = onClick,
        modifier = modifier
    )
}