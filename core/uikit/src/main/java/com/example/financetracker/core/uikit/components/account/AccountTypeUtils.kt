package com.example.financetracker.core.uikit.components.account

import androidx.annotation.DrawableRes
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.uikit.R

/**
 * Возвращает идентификатор векторного ресурса иконки для соответствующего типа счёта.
 */
@DrawableRes
fun AccountType.getIconRes(): Int = when(this) {
    AccountType.CARD -> R.drawable.ic_account_card
    AccountType.CASH -> R.drawable.ic_account_cash
    AccountType.SAVINGS -> R.drawable.ic_account_savings
    AccountType.INVESTMENT -> R.drawable.ic_account_investment
    AccountType.OTHER -> R.drawable.ic_other
}