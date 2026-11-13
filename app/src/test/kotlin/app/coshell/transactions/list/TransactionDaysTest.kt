package app.coshell.transactions.list

import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionDaysTest {
    private val zone = TimeZone.of("America/Argentina/Buenos_Aires")
    private val today = LocalDate(2026, 11, 8)
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val g = MoneyFormat.NARROW_NBSP
    private val nbsp = MoneyFormat.NBSP
    private val accounts = listOf(Account("card", "Карта", ars), Account("cash", "Наличные", usd)).associateBy { it.id }
    private val categories = listOf(Category("cafe", "Кафе", CategoryType.EXPENSE, icon = "cafe", color = 0xFF795548)).associateBy { it.id }

    private fun at(day: Int, hour: Int) = LocalDateTime(2026, 11, day, hour, 0).toInstant(zone)

    private fun build(vararg items: Transaction) =
        buildTransactionDays(items.toList(), accounts, categories, MoneyFormat.RU, zone, today)

    @Test
    fun `shows the author only for other members' operations`() {
        val items = listOf(
            Transaction("mine", TransactionType.EXPENSE, "card", Money(100, ars), at(8, 9), authorMemberId = "me"),
            Transaction("hers", TransactionType.EXPENSE, "card", Money(100, ars), at(8, 10), authorMemberId = "wife"),
            Transaction("old", TransactionType.EXPENSE, "card", Money(100, ars), at(8, 11)),
        )
        val rows = buildTransactionDays(items, accounts, categories, MoneyFormat.RU, zone, today, authors = mapOf("wife" to "Маша"))
            .single().rows.associate { it.id to it.author }
        assertEquals(mapOf("mine" to null, "hers" to "Маша", "old" to null), rows)
    }

    @Test
    fun `an income in an expense category is marked as a refund`() {
        val rows = build(
            Transaction("r", TransactionType.INCOME, "card", Money(4_500_00, ars), at(8, 9), categoryId = "cafe"),
            Transaction("i", TransactionType.INCOME, "card", Money(100, ars), at(8, 10)),
        ).single().rows.associateBy { it.id }
        assertEquals(true, rows.getValue("r").refund)
        assertEquals(Kind.INCOME, rows.getValue("r").kind)
        assertEquals(false, rows.getValue("i").refund)
    }

    @Test
    fun `groups by local day newest first with relative labels`() {
        val days = build(
            Transaction("a", TransactionType.EXPENSE, "card", Money(100, ars), at(6, 10)),
            Transaction("b", TransactionType.EXPENSE, "card", Money(100, ars), at(8, 9)),
            // 23:30 local on the 7th is already the 8th in UTC: must stay on the 7th.
            Transaction("c", TransactionType.EXPENSE, "card", Money(100, ars), LocalDateTime(2026, 11, 7, 23, 30).toInstant(zone)),
            Transaction("d", TransactionType.EXPENSE, "card", Money(100, ars), at(8, 20)),
        )
        assertEquals(listOf(today, LocalDate(2026, 11, 7), LocalDate(2026, 11, 6)), days.map { it.date })
        assertEquals(listOf(RelativeDay.TODAY, RelativeDay.YESTERDAY, RelativeDay.OTHER), days.map { it.relative })
        assertEquals(listOf("d", "b"), days[0].rows.map { it.id })
    }

    @Test
    fun `formats expense income and cross-currency transfer`() {
        val rows = build(
            Transaction("e", TransactionType.EXPENSE, "card", Money(1_500_00, ars), at(8, 12), categoryId = "cafe", note = "кофе"),
            Transaction("i", TransactionType.INCOME, "card", Money(100_00, ars), at(8, 11), categoryId = "gone"),
            Transaction(
                "t", TransactionType.TRANSFER, "cash", Money(100_00, usd), at(8, 10),
                toAccountId = "card", toAmount = Money(120_000_00, ars),
            ),
        ).single().rows

        val (expense, income, transfer) = rows
        assertEquals("Кафе", expense.title)
        assertEquals("Карта", expense.subtitle)
        assertEquals("кофе", expense.note)
        assertEquals("-1${g}500,00${nbsp}ARS", expense.amount)

        assertEquals(Kind.INCOME, income.kind)
        assertNull(income.title) // deleted category
        assertEquals("+100,00${nbsp}ARS", income.amount)

        assertEquals(Kind.TRANSFER, transfer.kind)
        assertEquals("Наличные → Карта", transfer.subtitle)
        assertEquals("+120${g}000,00${nbsp}ARS", transfer.secondaryAmount)
    }

    @Test
    fun `same-currency transfer has no second amount`() {
        val row = build(
            Transaction(
                "t", TransactionType.TRANSFER, "card", Money(5_00, ars), at(8, 10),
                toAccountId = "x", toAmount = Money(5_00, ars),
            ),
        ).single().rows.single()
        assertNull(row.secondaryAmount)
        assertEquals("Карта → ?", row.subtitle)
    }

    @Test
    fun `row carries category icon and colour`() {
        val rows = build(
            Transaction("e", TransactionType.EXPENSE, "card", Money(100, ars), at(8, 12), categoryId = "cafe"),
            Transaction("i", TransactionType.INCOME, "card", Money(100, ars), at(8, 11), categoryId = "gone"),
        ).single().rows
        assertEquals("cafe", rows[0].icon)
        assertEquals(0xFF795548, rows[0].color)
        assertNull(rows[1].icon)
        assertNull(rows[1].color)
    }

    @Test
    fun `purchase paid in another currency shows the receipt amount`() {
        val row = build(
            Transaction(
                "r", TransactionType.EXPENSE, "cash", Money(14_29, usd), at(8, 12),
                categoryId = "cafe", originalAmount = Money(15_000_00, ars),
            ),
        ).single().rows.single()
        assertEquals(MoneyFormat.RU.format(-Money(14_29, usd)), row.amount)
        assertEquals(MoneyFormat.RU.format(Money(15_000_00, ars)), row.secondaryAmount)
    }

    @Test
    fun `day total nets incomes and expenses per currency without transfers`() {
        val day = build(
            Transaction("e1", TransactionType.EXPENSE, "card", Money(30_000_00, ars), at(8, 9)),
            Transaction("e2", TransactionType.EXPENSE, "card", Money(15_300_00, ars), at(8, 10)),
            Transaction("t", TransactionType.TRANSFER, "card", Money(1_000_00, ars), at(8, 11), toAccountId = "cash", toAmount = Money(1_00, usd)),
        ).single()
        assertEquals("-45${g}300,00${nbsp}ARS", day.total)
        assertEquals(Kind.EXPENSE, day.totalTone)
    }

    @Test
    fun `mixed day has no tone and a day of transfers no total`() {
        val mixed = build(
            Transaction("e", TransactionType.EXPENSE, "card", Money(100_00, ars), at(8, 9)),
            Transaction("i", TransactionType.INCOME, "cash", Money(5_00, usd), at(8, 10)),
        ).single()
        assertNull(mixed.totalTone)
        val transfers = build(
            Transaction("t", TransactionType.TRANSFER, "card", Money(1_00, ars), at(8, 11), toAccountId = "cash", toAmount = Money(1, usd)),
        ).single()
        assertNull(transfers.total)
    }

    @Test
    fun `shows two live tags and counts the rest, skipping deleted ones`() {
        val tags = listOf(Tag("аня", "Аня", 0xFFFF7AB8), Tag("кот", "кот"), Tag("отпуск", "отпуск", 0xFF2EE6C5), Tag("миша", "Миша", 0xFF3D9BFF))
            .associateBy { it.id }
        val items = listOf(
            Transaction("many", TransactionType.EXPENSE, "card", Money(100, ars), at(8, 9), labels = listOf("Аня", "deleted", "кот", "отпуск", "Миша")),
            Transaction("gone", TransactionType.EXPENSE, "card", Money(100, ars), at(8, 10), labels = listOf("deleted")),
        )
        val rows = buildTransactionDays(items, accounts, categories, MoneyFormat.RU, zone, today, tagsById = tags).single().rows.associateBy { it.id }
        val many = rows.getValue("many")
        assertEquals(listOf("Аня", "кот"), many.tags.map { it.name })
        assertEquals(0xFFFF7AB8, many.tags[0].color)
        assertEquals(CategoryPalette.forKey("кот"), many.tags[1].color)
        assertEquals(2, many.moreTags)
        assertEquals(emptyList<TransactionsUiState.RowTag>(), rows.getValue("gone").tags)
        assertEquals(0, rows.getValue("gone").moreTags)
    }
}
