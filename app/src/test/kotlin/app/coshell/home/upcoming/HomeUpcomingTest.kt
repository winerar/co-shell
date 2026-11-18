package app.coshell.home.upcoming

import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Скоро · 7 дней» on «Обзоре» (#143, board `Rec143HomeA`); today — Sunday 15 November 2026. */
class HomeUpcomingTest {
    private val today = LocalDate(2026, 11, 15)
    private val ars = CurrencyCode("ARS")
    private val accounts = mapOf("galicia" to Account("galicia", "Galicia", ars))
    private val categories = mapOf("bills" to Category("bills", "Коммуналка", CategoryType.EXPENSE, icon = "bolt", color = 3L))

    private fun template(
        id: String,
        recurrence: Recurrence?,
        amount: Long? = null,
        mode: PostMode = PostMode.REMIND,
        start: LocalDate = LocalDate(2026, 11, 1),
    ) = TransactionTemplate(
        id, id, TransactionType.EXPENSE, "galicia", ars, amount?.let { Money(it, ars) }, "bills",
        schedule = recurrence?.let { RecurringSchedule(it, start, mode) },
    )

    private fun rows(vararg templates: TransactionTemplate) =
        buildUpcomingRows(templates.toList(), accounts, categories, MoneyFormat.RU, today)

    @Test
    fun `missed payments come first and count every overdue day`() {
        val list = rows(
            template("light", Recurrence.Monthly(15)),
            template("cleaning", Recurrence.Weekly(DayOfWeek.FRIDAY), amount = 30_000_00),
            template("spanish", Recurrence.Weekly(DayOfWeek.TUESDAY), amount = 40_000_00, start = LocalDate(2026, 11, 16)),
        )

        assertEquals(listOf("cleaning", "light", "spanish"), list.map { it.item.id })
        val cleaning = list[0]
        assertEquals(PaymentState.MISSED, cleaning.state)
        assertEquals(LocalDate(2026, 11, 6), cleaning.date)
        assertEquals(2, cleaning.missed)
        assertEquals(PaymentState.TODAY, list[1].state)
        assertEquals("", list[1].item.amount)
        assertEquals(PaymentState.SOON, list[2].state)
        assertEquals(LocalDate(2026, 11, 17), list[2].date)
    }

    @Test
    fun `the row carries the category badge and the formatted amount`() {
        val row = rows(template("spanish", Recurrence.Monthly(17), amount = 40_000_00)).single()

        assertEquals("Коммуналка", row.item.categoryName)
        assertEquals("bolt", row.item.categoryIcon)
        assertEquals(MoneyFormat.RU.format(Money(40_000_00, ars)), row.item.amount)
    }

    @Test
    fun `templates without a repeat or due later than a week are not listed`() {
        val list = rows(
            template("coffee", null, amount = 3_000_00),
            template("rent", Recurrence.Monthly(22)),
            template("water", Recurrence.Monthly(21)),
        )

        assertEquals(listOf("water"), list.map { it.item.id })
    }

    @Test
    fun `a payment that posts itself shows «само» until it is overdue`() {
        val netflix = template("netflix", Recurrence.Monthly(18), amount = 7_990_00, mode = PostMode.AUTO)
        assertTrue(rows(netflix).single().auto)

        // Could not post itself on the 10th (account archived, category gone): «Провести» by hand.
        val internet = template("internet", Recurrence.Monthly(10), amount = 18_900_00, mode = PostMode.AUTO)
        val missed = rows(internet).single()
        assertEquals(PaymentState.MISSED, missed.state)
        assertFalse(missed.auto)
    }

    @Test
    fun `two or more missed with an amount get post all and skip`() {
        val list = rows(
            template("cleaning", Recurrence.Weekly(DayOfWeek.FRIDAY), amount = 30_000_00),
            template("lessons", Recurrence.Weekly(DayOfWeek.FRIDAY)),
            template("gas", Recurrence.Monthly(10), amount = 9_000_00),
        ).associateBy { it.item.id }

        assertTrue(list.getValue("cleaning").bulk)
        assertFalse(list.getValue("lessons").bulk) // no amount: «Провести» opens the sheet
        assertFalse(list.getValue("gas").bulk) // one missed: plain «Провести»
    }

    @Test
    fun `a payment in another currency is never «само» nor posted all at once`() {
        val usd = Money(650_00, CurrencyCode("USD"))
        val auto = template("rent", Recurrence.Monthly(18), amount = 1, mode = PostMode.AUTO).copy(amount = usd)
        assertFalse(rows(auto).single().auto)

        val weekly = template("tutor", Recurrence.Weekly(DayOfWeek.FRIDAY), amount = 1).copy(amount = usd)
        assertFalse(rows(weekly).single().bulk)
    }

    @Test
    fun `a payment whose account is archived is paused and not listed`() {
        val archived = accounts + ("cash" to Account("cash", "Наличные", ars, archived = true))
        val rent = template("rent", Recurrence.Monthly(15)).copy(accountId = "cash")
        val list = buildUpcomingRows(listOf(rent, template("light", Recurrence.Monthly(15))), archived, categories, MoneyFormat.RU, today)

        assertEquals(listOf("light"), list.map { it.item.id })
    }

    @Test
    fun `the horizon of the settings widens or narrows what is soon`() {
        // Monthly on the 25th: ten days ahead — not in a week, in a fortnight, and not in three days.
        val rent = template("rent", Recurrence.Monthly(25))
        fun ids(days: Int) = buildUpcomingRows(listOf(rent), accounts, categories, MoneyFormat.RU, today, days).map { it.item.id }
        assertEquals(emptyList<String>(), ids(7))
        assertEquals(listOf("rent"), ids(14))
        assertEquals(emptyList<String>(), ids(3))
    }
}
