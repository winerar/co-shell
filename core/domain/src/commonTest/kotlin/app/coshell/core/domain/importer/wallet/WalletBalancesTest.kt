package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.time.Instant

class WalletBalancesTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val card = Account("card", "Карта", usd, initialBalanceMinor = 1_000)
    private val cash = Account("cash", "Наличные", ars)
    private val other = Account("other", "Вклад", usd)
    private val day = Instant.fromEpochSeconds(1_700_000_000)

    private fun usd(minor: Long) = Money(minor, usd)
    private fun ars(minor: Long) = Money(minor, ars)

    @Test
    fun `balance after import adds what the new operations move`() {
        val accounts = listOf(
            AccountWithBalance(card, usd(5_000)),
            AccountWithBalance(cash, ars(0)),
            AccountWithBalance(other, usd(700)),
        )
        val imported = listOf(
            Transaction("1", TransactionType.EXPENSE, "card", usd(1_200), day),
            Transaction("2", TransactionType.INCOME, "card", usd(300), day),
            Transaction("3", TransactionType.TRANSFER, "card", usd(1_000), day, toAccountId = "cash", toAmount = ars(150_000)),
        )

        val after = WalletBalances.afterImport(accounts, imported)

        assertEquals(mapOf("card" to usd(3_100), "cash" to ars(150_000), "other" to usd(700)), after)
    }

    @Test
    fun `difference goes into the initial balance`() {
        // Wallet shows 50.00 USD, co-shell will have 31.00 → +19.00 to the start.
        val fixed = WalletBalances.reconcile(card, balanceAfterImport = usd(3_100), walletBalance = usd(5_000))

        assertEquals(1_000L + 1_900L, fixed.initialBalanceMinor)
        assertEquals(card.copy(initialBalanceMinor = 2_900), fixed)
    }

    @Test
    fun `negative difference lowers the start, below zero if needed`() {
        val fixed = WalletBalances.reconcile(cash, balanceAfterImport = ars(150_000), walletBalance = ars(-20_000))

        assertEquals(-170_000L, fixed.initialBalanceMinor)
    }

    @Test
    fun `equal balances leave the account as is`() {
        assertSame(card, WalletBalances.reconcile(card, usd(3_100), usd(3_100)))
    }

    @Test
    fun `balances in another currency are rejected`() {
        assertFailsWith<IllegalArgumentException> { WalletBalances.reconcile(card, usd(3_100), ars(3_100)) }
    }
}
