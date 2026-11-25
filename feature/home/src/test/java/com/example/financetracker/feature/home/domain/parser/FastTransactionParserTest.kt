package com.example.financetracker.feature.home.domain.parser


import com.example.financetracker.core.model.Account
import com.example.financetracker.core.model.AccountType
import com.example.financetracker.core.model.Category
import com.example.financetracker.core.model.CategoryType
import com.example.financetracker.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class FastTransactionParserTest {

    private val testAccounts = listOf(
        Account(id = "acc_tbank", name = "Тинькофф", balance = 10000.0, type = AccountType.CARD, isDefault = true),
        Account(id = "acc_sber", name = "Сбербанк", balance = 5000.0, type = AccountType.CARD),
        Account(id = "acc_sovkom", name = "Совкомбанк", balance = 2000.0, type = AccountType.CARD),
        Account(id = "acc_cash", name = "Наличные", balance = 1500.0, type = AccountType.CASH)
    )

    private val testCategories = listOf(
        Category(id = "cat_food", name = "Продукты", type = CategoryType.EXPENSE, iconRes = "ic_food"),
        Category(id = "cat_transport", name = "Транспорт", type = CategoryType.EXPENSE, iconRes = "ic_transport"),
        Category(id = "cat_cafe", name = "Кафе", type = CategoryType.EXPENSE, iconRes = "ic_cafe"),
        Category(id = "cat_sport", name = "Спорт", type = CategoryType.EXPENSE, iconRes = "ic_sport"),
        Category(id = "cat_salary", name = "Зарплата", type = CategoryType.INCOME, iconRes = "ic_salary"),
        Category(id = "cat_freelance", name = "Фриланс", type = CategoryType.INCOME, iconRes = "ic_freelance")
    )

    @Test
    fun `date number is not confused with transaction amount`() {
        val draft = FastTransactionParser.parse(
            text = "24 октября продукты 1500",
            categories = testCategories,
            accounts = testAccounts,
            defaultAccountId = "acc_tbank"
        )

        assertEquals(1500.0, draft.amount!!, 0.01)
        assertEquals("cat_food", draft.categoryId)
        assertEquals("", draft.note)
        assertNotNull(draft.date)

        val cal = Calendar.getInstance().apply { timeInMillis = draft.date!! }
        assertEquals(24, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH))
    }

    @Test
    fun `synonym and merchant words are preserved in note while direct category name is removed`() {
        val taxiDraft = FastTransactionParser.parse(
            text = "Такси вчера 500 с карты",
            categories = testCategories,
            accounts = testAccounts
        )
        assertEquals("cat_transport", taxiDraft.categoryId)
        assertEquals(500.0, taxiDraft.amount!!, 0.01)
        assertEquals("Такси", taxiDraft.note)
        assertNotNull(taxiDraft.date)

        val directDraft = FastTransactionParser.parse(
            text = "Транспорт 500",
            categories = testCategories,
            accounts = testAccounts
        )
        assertEquals("cat_transport", directDraft.categoryId)
        assertEquals(500.0, directDraft.amount!!, 0.01)
        assertEquals("", directDraft.note)
    }

    @Test
    fun `grocery phrase retains merchant and items in note`() {
        val draft = FastTransactionParser.parse(
            text = "Пятёрочка сыр и кофе 1200",
            categories = testCategories,
            accounts = testAccounts
        )

        assertEquals("cat_food", draft.categoryId)
        assertEquals(1200.0, draft.amount!!, 0.01)
        assertEquals("Пятёрочка сыр и кофе", draft.note)
    }

    @Test
    fun `typos in category and bank names are resolved by fuzzy matching`() {
        val draft = FastTransactionParser.parse(
            text = "прадукты 800 с савкомбанка",
            categories = testCategories,
            accounts = testAccounts
        )

        assertEquals("cat_food", draft.categoryId)
        assertEquals("acc_sovkom", draft.accountId)
        assertEquals(800.0, draft.amount!!, 0.01)
    }

    @Test
    fun `bank slang resolves correctly and consumes prepositions`() {
        val draft = FastTransactionParser.parse(
            text = "кофе 300 с совка",
            categories = testCategories,
            accounts = testAccounts
        )

        assertEquals("acc_sovkom", draft.accountId)
        assertEquals("cat_cafe", draft.categoryId)
        assertEquals(300.0, draft.amount!!, 0.01)
        assertEquals("Кофе", draft.note)
    }

    @Test
    fun `thousand separators and composite word numbers are parsed properly`() {
        val salaryDraft = FastTransactionParser.parse(
            text = "20.000 зарплата на сбер",
            categories = testCategories,
            accounts = testAccounts
        )
        assertEquals(20000.0, salaryDraft.amount!!, 0.01)
        assertEquals(TransactionType.INCOME, salaryDraft.type)
        assertEquals("cat_salary", salaryDraft.categoryId)
        assertEquals("acc_sber", salaryDraft.accountId)

        val wordsDraft = FastTransactionParser.parse(
            text = "купил наушники за полторы тысячи",
            categories = testCategories,
            accounts = testAccounts
        )
        assertEquals(1500.0, wordsDraft.amount!!, 0.01)
        assertEquals(TransactionType.EXPENSE, wordsDraft.type)
        assertEquals("Купил наушники", wordsDraft.note)
    }

    @Test
    fun `numeric date and weekday formats are supported`() {
        val numericDateDraft = FastTransactionParser.parse(
            text = "24.10 бензин 2500",
            categories = testCategories,
            accounts = testAccounts
        )
        assertEquals(2500.0, numericDateDraft.amount!!, 0.01)
        assertEquals("cat_transport", numericDateDraft.categoryId)
        assertEquals("Бензин", numericDateDraft.note)
        val cal = Calendar.getInstance().apply { timeInMillis = numericDateDraft.date!! }
        assertEquals(24, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH))

        val weekdayDraft = FastTransactionParser.parse(
            text = "в прошлую пятницу кино 800",
            categories = testCategories,
            accounts = testAccounts
        )
        assertEquals(800.0, weekdayDraft.amount!!, 0.01)
        assertEquals("Кино", weekdayDraft.note)
        assertNotNull(weekdayDraft.date)
    }

    @Test
    fun `custom income category switches transaction type automatically`() {
        val draft = FastTransactionParser.parse(
            text = "фриланс 15000",
            categories = testCategories,
            accounts = testAccounts
        )

        assertEquals(TransactionType.INCOME, draft.type)
        assertEquals("cat_freelance", draft.categoryId)
        assertEquals(15000.0, draft.amount!!, 0.01)
        assertEquals("", draft.note)
    }

    @Test
    fun `custom category matches via embedding or synonym`() {
        val draft = FastTransactionParser.parse(
            text = "спортзал 2500",
            categories = testCategories,
            accounts = testAccounts
        )

        assertEquals("cat_sport", draft.categoryId)
        assertEquals(2500.0, draft.amount!!, 0.01)
        assertEquals("Спортзал", draft.note)
    }

    @Test
    fun `empty input returns empty draft`() {
        val draft = FastTransactionParser.parse(
            text = "   ",
            categories = testCategories,
            accounts = testAccounts
        )
        assertNull(draft.amount)
        assertNull(draft.categoryId)
        assertEquals("", draft.note)
    }
}