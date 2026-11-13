package app.coshell.core.domain.quickentry

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class TipsTest {
    private val ars = CurrencyCode("ARS")
    private val usdt = CurrencyCode("USDT")
    private val usd = CurrencyCode("USD")
    private val date = Instant.parse("2026-11-13T21:40:00Z")

    private val cashArs = Account(id = "cash-ars", name = "Наличные ARS", currency = ars)
    private val cashUsd = Account(id = "cash-usd", name = "Наличные USD", currency = usd)

    /** USDT card paying a peso receipt — the owner's usual case. */
    private val dinner = Transaction(
        id = "t1", type = TransactionType.EXPENSE, accountId = "usdt-card", amount = Money.ofMajor(13, 10, usdt), date = date,
        categoryId = "cafe", payee = "Café Martínez", labels = listOf("tag-anya"), originalAmount = Money.ofMajor(13_300, currency = ars),
        authorMemberId = "m1",
    )

    private val noRate: (Money, CurrencyCode) -> Money? = { _, _ -> null }

    @Test
    fun `percent is taken from the receipt amount`() {
        assertEquals(Money.ofMajor(13_300, currency = ars), Tips.base(dinner))
        assertEquals(Money.ofMajor(1_330, currency = ars), Tips.percentOf(Tips.base(dinner), 10))
    }

    @Test
    fun `percent of a small dollar bill keeps cents and rounds half to even`() {
        assertEquals(Money.ofMajor(1, 96, usd), Tips.percentOf(Money.ofMajor(13, 10, usd), 15))
    }

    @Test
    fun `percent outside 1 to 100 is a bug`() {
        assertFailsWith<IllegalArgumentException> { Tips.percentOf(Money.ofMajor(10, currency = usd), 0) }
    }

    @Test
    fun `tip from a cash account in the same currency copies category date tags and payee`() {
        val tip = Tips.transaction(dinner, Money.ofMajor(1_330, currency = ars), cashArs, "t2", "Чаевые", noRate)
        val t = assertIs<Tips.Result.Ok>(tip).transaction
        assertEquals("cash-ars", t.accountId)
        assertEquals(Money.ofMajor(1_330, currency = ars), t.amount)
        assertNull(t.originalAmount)
        assertEquals(date, t.date)
        assertEquals("cafe", t.categoryId)
        assertEquals(listOf("tag-anya"), t.labels)
        assertEquals("Café Martínez", t.payee)
        assertEquals("Чаевые", t.note)
        assertEquals("m1", t.authorMemberId)
    }

    @Test
    fun `tip in another currency is converted and keeps the original`() {
        val toUsd: (Money, CurrencyCode) -> Money? = { m, to -> Money(m.amountMinor / 1_000, to) }
        val tip = Tips.transaction(dinner, Money.ofMajor(1_330, currency = ars), cashUsd, "t2", "Чаевые", toUsd)
        val t = assertIs<Tips.Result.Ok>(tip).transaction
        assertEquals(Money.ofMajor(1, 33, usd), t.amount)
        assertEquals(Money.ofMajor(1_330, currency = ars), t.originalAmount)
    }

    @Test
    fun `no rate for the tip account currency is reported`() {
        val tip = Tips.transaction(dinner, Money.ofMajor(1_330, currency = ars), cashUsd, "t2", "Чаевые", noRate)
        assertEquals(Tips.Result.Invalid(Tips.Reason.NO_RATE), tip)
    }

    @Test
    fun `zero tip and income main are refused`() {
        assertEquals(Tips.Result.Invalid(Tips.Reason.TIP_NOT_POSITIVE), Tips.transaction(dinner, Money.zero(ars), cashArs, "t2", "", noRate))
        val salary = dinner.copy(type = TransactionType.INCOME, originalAmount = null)
        assertEquals(
            Tips.Result.Invalid(Tips.Reason.MAIN_NOT_EXPENSE),
            Tips.transaction(salary, Money.ofMajor(1, currency = ars), cashArs, "t2", "", noRate),
        )
    }
}
