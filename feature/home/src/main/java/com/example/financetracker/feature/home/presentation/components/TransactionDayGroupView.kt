package com.example.financetracker.feature.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.Transaction
import com.example.financetracker.core.model.TransactionDayGroup
import com.example.financetracker.core.model.TransactionType
import com.example.financetracker.core.uikit.components.text.DateHeader
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme
import kotlin.collections.get

/**
 * Вычисляет скругление углов для строки операции внутри группы дня:
 * - 1 операция: скруглены все углы (20.dp)
 * - Первая операция: скруглены верхние углы
 * - Последняя операция: скруглены нижние углы
 * - Промежуточные: прямоугольные
 */
private val SingleItemShape = RoundedCornerShape(20.dp)
private val TopItemShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
private val BottomItemShape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
private fun getTransactionCardShape(index: Int, totalCount: Int): Shape = when {
    totalCount == 1 -> SingleItemShape
    index == 0 -> TopItemShape
    index == totalCount - 1 -> BottomItemShape
    else -> RectangleShape
}

/**
 * Для отображения группы дня внутри обычного скроллируемого Column (например, в шторках).
 */
@Composable
fun TransactionDayGroupView(
    dayGroup: TransactionDayGroup,
    categoriesMap: Map<String, Category>,
    account: Account?,
    modifier: Modifier = Modifier,
    targetAccount: Account? = null,
    accountsMap: Map<String, Account>? = null,
    headerHorizontalPadding: Dp = 0.dp,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Заголовок дня со сводкой за день
        DateHeader(
            title = dayGroup.dateTitle,
            dailyTotal = dayGroup.dailyTotal,
            horizontalPadding = headerHorizontalPadding
        )

        // Карточки операций
        dayGroup.transactions.forEachIndexed { index, transaction ->
            val shape = getTransactionCardShape(index, dayGroup.transactions.size)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                color = MaterialTheme.colorScheme.surface
            ) {
                val txAccount = account ?: accountsMap?.get(transaction.accountId)
                val txTargetAccount = targetAccount ?: accountsMap?.get(transaction.targetAccountId)
                val category = categoriesMap[transaction.categoryId]

                HomeTransactionRow(
                    transaction = transaction,
                    category = category,
                    account = txAccount,
                    targetAccount = txTargetAccount,
                    onClick = { onTransactionClick(transaction) }
                )
            }
        }
    }
}

/**
 * Для ленивого отображения группы дня внутри LazyColumn (на экранах HomeScreen и AccountDetailScreen).
 */
fun LazyListScope.transactionDayItems(
    dayGroup: TransactionDayGroup,
    categoriesMap: Map<String, Category>,
    accountsMap: Map<String, Account>,
    account: Account? = null,
    itemHorizontalPadding: Dp = 20.dp,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    item(
        key = "header_${dayGroup.dateTitle}"
    ) {
        DateHeader(
            title = dayGroup.dateTitle,
            dailyTotal = dayGroup.dailyTotal,
            horizontalPadding = itemHorizontalPadding
        )
    }

    itemsIndexed(
        items = dayGroup.transactions,
        key = { _, tx -> tx.id }
    ) { index, transaction ->
        val shape = getTransactionCardShape(index, dayGroup.transactions.size)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = itemHorizontalPadding),
            shape = shape,
            color = MaterialTheme.colorScheme.surface
        ) {
            val txAccount = account ?: accountsMap[transaction.accountId]
            val txTargetAccount = accountsMap[transaction.targetAccountId]
            val category = categoriesMap[transaction.categoryId]
            HomeTransactionRow(
                transaction = transaction,
                category = category,
                account = txAccount,
                targetAccount = txTargetAccount,
                onClick = { onTransactionClick(transaction) }
            )
        }
    }

    item(key = "spacer_${dayGroup.dateTitle}") {
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// --- ТЕСТОВЫЕ ДАННЫЕ ДЛЯ ПРЕВЬЮ ---
private val sampleAccount = Account(id = "1", name = "Основная карта", balance = 86700.0)
private val sampleTargetAccount = Account(id = "2", name = "Накопления", balance = 150000.0)
private val sampleCategories = mapOf(
    "cat1" to Category(id = "cat1", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "ic_category_groceries"),
    "cat2" to Category(id = "cat2", name = "Зарплата", type = CategoryType.INCOME, iconRes = "ic_category_salary"),
    "cat3" to Category(id = "cat3", name = "Кафе", type = CategoryType.EXPENSE, iconRes = "ic_category_cafe"),
    "cat4" to Category(id = "cat4", name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "ic_category_transport"),
)

private val sampleDays = listOf(
    // День 1: несколько операций (скругления верх/низ)
    TransactionDayGroup(
        dateTitle = "Сегодня",
        dailyTotal = 48500.0,
        transactions = listOf(
            Transaction(
                id = "1",
                amount = 1500.0,
                type = TransactionType.EXPENSE,
                categoryId = "cat1",
                accountId = "1",
                dateTime = System.currentTimeMillis(),
                note = "Супермаркет"
            ),
            Transaction(
                id = "2",
                amount = 50000.0,
                type = TransactionType.INCOME,
                categoryId = "cat2",
                accountId = "1",
                dateTime = System.currentTimeMillis(),
                note = "Аванс"
            )
        )
    ),
    // День 2: одиночная операция (скруглены все 4 угла)
    TransactionDayGroup(
        dateTitle = "Вчера",
        dailyTotal = -350.0,
        transactions = listOf(
            Transaction(
                id = "3",
                amount = 350.0,
                type = TransactionType.EXPENSE,
                categoryId = "cat3",
                accountId = "1",
                dateTime = System.currentTimeMillis() - 86400000L,
                note = "Кофе и круассан"
            )
        )
    ),
    // День 3: перевод на другой счёт и расход
    TransactionDayGroup(
        dateTitle = "20 ноября, пятница",
        dailyTotal = -10450.0,
        transactions = listOf(
            Transaction(
                id = "4",
                amount = 10000.0,
                type = TransactionType.TRANSFER,
                categoryId = "",
                accountId = "1",
                targetAccountId = "2",
                dateTime = System.currentTimeMillis() - 172800000L,
                note = "В инвесткопилку"
            ),
            Transaction(
                id = "5",
                amount = 450.0,
                type = TransactionType.EXPENSE,
                categoryId = "cat4",
                accountId = "1",
                dateTime = System.currentTimeMillis() - 172800000L,
                note = "Такси до дома"
            )
        )
    )
)
@Preview(name = "Light Theme - Multiple Days", showBackground = true)
@Composable
private fun TransactionDayGroupViewLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sampleDays.forEach { dayGroup ->
                    TransactionDayGroupView(
                        dayGroup = dayGroup,
                        categoriesMap = sampleCategories,
                        account = sampleAccount,
                        targetAccount = sampleTargetAccount,
                        headerHorizontalPadding = 0.dp
                    )
                }
            }
        }
    }
}
@Preview(name = "Dark Theme - Multiple Days", showBackground = true)
@Composable
private fun TransactionDayGroupViewDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sampleDays.forEach { dayGroup ->
                    TransactionDayGroupView(
                        dayGroup = dayGroup,
                        categoriesMap = sampleCategories,
                        account = sampleAccount,
                        targetAccount = sampleTargetAccount,
                        headerHorizontalPadding = 0.dp
                    )
                }
            }
        }
    }
}