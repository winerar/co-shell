package app.coshell.core.domain.credit

import app.coshell.core.model.CreditTerms
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MinPayment
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class CreditCardTest {
    private val amd = CurrencyCode("AMD")
    private val percent = CreditTerms(limitMinor = 500_000_00, payDay = 15, minPayment = MinPayment.Percent(500))
    private val fixed = CreditTerms(limitMinor = 500_000_00, payDay = 15, minPayment = MinPayment.Fixed(9_000_00))
    private fun money(major: Long) = Money(major * 100, amd)

    @Test
    fun `debt is the negative balance and what is left of the limit is available`() {
        val s = CreditCard.status(percent, money(-180_000), LocalDate(2026, 11, 23))
        assertEquals(money(180_000), s.debt)
        assertEquals(money(320_000), s.available)
        assertEquals(money(500_000), s.limit)
    }

    @Test
    fun `a percent minimum is a share of the debt and a fixed one never exceeds it`() {
        assertEquals(money(9_000), CreditCard.status(percent, money(-180_000), LocalDate(2026, 11, 23)).minPayment)
        assertEquals(money(9_000), CreditCard.status(fixed, money(-180_000), LocalDate(2026, 11, 23)).minPayment)
        assertEquals(money(4_000), CreditCard.status(fixed, money(-4_000), LocalDate(2026, 11, 23)).minPayment)
        assertEquals(Money(1, amd), CreditCard.status(percent, Money(-1, amd), LocalDate(2026, 11, 23)).minPayment)
    }

    @Test
    fun `without a debt there is no payment and the whole limit is available`() {
        val none = CreditCard.status(percent, money(0), LocalDate(2026, 11, 23))
        assertNull(none.dueDate)
        assertNull(none.minPayment)
        assertEquals(money(500_000), none.available)
        val ahead = CreditCard.status(percent, money(20_000), LocalDate(2026, 11, 23))
        assertEquals(money(0), ahead.debt)
        assertNull(ahead.minPayment)
    }

    @Test
    fun `debt above the limit leaves nothing available`() {
        assertEquals(money(0), CreditCard.status(percent, money(-520_000), LocalDate(2026, 11, 23)).available)
    }

    @Test
    fun `the due date is the next pay day, today counts, a short month ends on its last day`() {
        assertEquals(LocalDate(2026, 12, 15), CreditCard.dueDate(15, LocalDate(2026, 11, 23)))
        assertEquals(LocalDate(2026, 11, 23), CreditCard.dueDate(23, LocalDate(2026, 11, 23)))
        assertEquals(LocalDate(2026, 11, 30), CreditCard.dueDate(31, LocalDate(2026, 11, 23)))
        assertEquals(LocalDate(2027, 2, 28), CreditCard.dueDate(31, LocalDate(2027, 2, 1)))
        assertEquals(LocalDate(2027, 1, 15), CreditCard.dueDate(15, LocalDate(2026, 12, 20)))
    }

    @Test
    fun `the previous due date opens the billing cycle`() {
        assertEquals(LocalDate(2026, 11, 15), CreditCard.previousDue(15, LocalDate(2026, 12, 15)))
        assertEquals(LocalDate(2027, 2, 28), CreditCard.previousDue(31, LocalDate(2027, 3, 31)))
        assertEquals(LocalDate(2026, 12, 15), CreditCard.previousDue(15, LocalDate(2027, 1, 15)))
    }

    @Test
    fun `terms are validated`() {
        assertFailsWith<IllegalArgumentException> { CreditTerms(0, 15, MinPayment.Percent(500)) }
        assertFailsWith<IllegalArgumentException> { CreditTerms(1, 0, MinPayment.Percent(500)) }
        assertFailsWith<IllegalArgumentException> { CreditTerms(1, 32, MinPayment.Percent(500)) }
        assertFailsWith<IllegalArgumentException> { MinPayment.Percent(0) }
        assertFailsWith<IllegalArgumentException> { MinPayment.Fixed(0) }
    }
}
