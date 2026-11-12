package app.coshell.currencies.list

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BalancePreviewTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val nov9 = LocalDate(2026, 11, 9)
    private val rates = listOf(ExchangeRate(LocalDate(2026, 11, 5), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))
    private val fmt = MoneyFormat.RU

    private val accounts = listOf(
        AccountWithBalance(Account("a", "Карта", ars), Money(100_000_00, ars)),
        AccountWithBalance(Account("b", "Bybit", usd), Money(5_00, usd)),
        AccountWithBalance(Account("c", "Вклад", usd, excludedFromTotal = true), Money(1_000_00, usd)),
    )

    @Test
    fun `total in each report currency, main first, without excluded accounts`() {
        assertEquals(
            listOf(fmt.format(Money(105_00, usd)), fmt.format(Money(105_000_00, ars))),
            balancePreview(accounts, rates, listOf(usd, ars), nov9, fmt),
        )
    }

    @Test
    fun `follows the order of report currencies`() {
        assertEquals(fmt.format(Money(105_000_00, ars)), balancePreview(accounts, rates, listOf(ars, usd), nov9, fmt).first())
    }

    @Test
    fun `no accounts - nothing to preview`() {
        assertEquals(emptyList<String>(), balancePreview(emptyList(), rates, listOf(usd), nov9, fmt))
    }
}
