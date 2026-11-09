package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.importer.wallet.WalletSamples.HEADER
import app.coshell.core.domain.importer.wallet.WalletSamples.row
import kotlin.test.Test
import kotlin.test.assertEquals

class WalletTransferMatcherTest {

    private fun match(vararg lines: String) =
        WalletTransferMatcher.match(WalletCsvParser.parse((listOf(HEADER) + lines).joinToString("\n")).records)

    private fun transfer(account: String, currency: String, amount: String, ref: String, date: String = "2026-10-31 12:55:37") =
        row(
            "account" to account, "category" to "TRANSFER", "currency" to currency, "amount" to amount,
            "ref_currency_amount" to ref, "date" to date, "transfer" to "true", "envelope_id" to "20001",
        )

    @Test
    fun exchangeBetweenCurrenciesIsOnePair() {
        val result = match(
            transfer("Счёт AMD", "AMD", "-380000.00", "-380000.00"),
            transfer("Карта USD", "USD", "1000.00", "380000.00"),
        )
        val pair = result.pairs.single()
        assertEquals("Счёт AMD", pair.from.account)
        assertEquals("Карта USD", pair.to.account)
        assertEquals(emptyList(), result.unpaired)
    }

    @Test
    fun sampleHasTwoPairsAndOneOrphan() {
        val result = WalletTransferMatcher.match(WalletCsvParser.parse(WalletSamples.SAMPLE).records)
        assertEquals(2, result.pairs.size)
        assertEquals(listOf(12), result.unpaired.map { it.line })
    }

    @Test
    fun differentSecondOrDifferentRefAmountIsNotAPair() {
        val result = match(
            transfer("A", "USD", "-10.00", "-3800.00", date = "2026-10-01 10:00:00"),
            transfer("B", "USD", "10.00", "3800.00", date = "2026-10-01 10:00:01"),
            transfer("A", "USD", "-10.00", "-3800.00", date = "2026-10-02 10:00:00"),
            transfer("B", "USD", "10.00", "3799.00", date = "2026-10-02 10:00:00"),
        )
        assertEquals(emptyList(), result.pairs)
        assertEquals(listOf(2, 3, 4, 5), result.unpaired.map { it.line })
    }

    @Test
    fun sameAccountOnBothSidesIsNotAPair() {
        val result = match(transfer("A", "USD", "-10.00", "-3800"), transfer("A", "USD", "10.00", "3800"))
        assertEquals(0, result.pairs.size)
        assertEquals(2, result.unpaired.size)
    }

    @Test
    fun twoTransfersInTheSameSecondPairByRefAmount() {
        val result = match(
            transfer("A", "USD", "-10.00", "-3800.00"),
            transfer("B", "USD", "-20.00", "-7600.00"),
            transfer("C", "USD", "20.00", "7600.0"),
            transfer("D", "USD", "10.00", "3800"),
        )
        assertEquals(listOf("A" to "D", "B" to "C"), result.pairs.map { it.from.account to it.to.account })
    }

    @Test
    fun ordinaryRowsAreIgnored() {
        val result = match(row(), row("amount" to "5.00"))
        assertEquals(emptyList(), result.pairs)
        assertEquals(emptyList(), result.unpaired)
    }
}
