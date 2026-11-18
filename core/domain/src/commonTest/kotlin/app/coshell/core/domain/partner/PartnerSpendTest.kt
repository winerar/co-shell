package app.coshell.core.domain.partner

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class PartnerSpendTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val day = LocalDate(2026, 11, 13)
    private val converter = CurrencyConverter(RateBook.of(listOf(ExchangeRate(day, usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))))

    private fun change(
        id: String,
        minor: Long,
        currency: CurrencyCode = ars,
        type: TransactionType = TransactionType.EXPENSE,
        at: Long = 1,
        imported: Boolean = false,
    ) = PartnerChange(
        Transaction(
            id, type, "card", Money(minor, currency), Instant.fromEpochSeconds(0),
            toAccountId = if (type == TransactionType.TRANSFER) "cash" else null,
            toAmount = if (type == TransactionType.TRANSFER) Money(minor, currency) else null,
            importSource = if (imported) "wallet" else null,
            importExternalId = if (imported) id else null,
        ),
        at,
        "anya",
    )

    private fun notify(minimum: Int, vararg changes: PartnerChange) =
        spendsToNotify(changes.toList(), minimum, usd, converter) { day }.map { it.transaction.id }

    @Test
    fun `only expenses count, not transfers, incomes or imports`() {
        assertEquals(
            listOf("spend"),
            notify(0, change("spend", 100_00), change("move", 100_00, type = TransactionType.TRANSFER), change("pay", 100_00, type = TransactionType.INCOME), change("old", 100_00, imported = true)),
        )
    }

    @Test
    fun `the threshold is in the main currency at the rate of the day`() {
        // 50 USD = 50 000 ARS at 1000.
        assertEquals(listOf("big"), notify(50, change("small", 49_000_00), change("big", 50_000_00)))
        assertEquals(listOf("usd"), notify(50, change("usd", 60_00, usd), change("usd-small", 40_00, usd)))
    }

    @Test
    fun `a zero threshold tells about every expense, an unknown rate is told too, oldest first`() {
        val eur = CurrencyCode("EUR")
        assertEquals(listOf("b", "a"), notify(0, change("a", 1_00, at = 5), change("b", 1_00, at = 3)))
        assertEquals(listOf("eur"), notify(50, change("eur", 1_00, eur)))
    }
}
