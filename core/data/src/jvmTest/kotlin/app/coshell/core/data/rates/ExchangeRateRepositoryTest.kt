package app.coshell.core.data.rates

import app.cash.turbine.test
import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomExchangeRateRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.domain.rates.RatesSnapshot
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class ExchangeRateRepositoryTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val eur = CurrencyCode("EUR")
    private val usdt = CurrencyCode("USDT")
    private val nov9 = LocalDate(2026, 11, 9)
    private val nov10 = LocalDate(2026, 11, 10)

    private val db = createInMemoryCoShellDatabase()
    private val stamper = WriteStamper()
    private val rates = RoomExchangeRateRepository(db.rateDao(), stamper)

    @AfterTest
    fun tearDown() = db.close()

    private fun manual(date: LocalDate, from: CurrencyCode, to: CurrencyCode, rate: String) =
        ExchangeRate(date, from, to, Rate.parse(rate), RateOrigin.MANUAL)

    private fun downloaded(date: LocalDate, to: CurrencyCode, rate: String) =
        ExchangeRate(date, usd, to, Rate.parse(rate), RateOrigin.DOWNLOADED)

    private fun snapshot(date: LocalDate, vararg rates: ExchangeRate) = RatesSnapshot(date, usd, rates.toList(), "test")

    @Test
    fun `manual rate keeps its exact text and direction`() = runTest {
        rates.saveManualRate(manual(nov9, usd, ars, "1450.123456789"))
        assertEquals(listOf(manual(nov9, usd, ars, "1450.123456789")), rates.observeManualRates().first())
    }

    @Test
    fun `saving the opposite direction on the same day replaces the rate`() = runTest {
        rates.saveManualRate(manual(nov9, usd, ars, "1450"))
        rates.saveManualRate(manual(nov9, ars, usd, "0.0007"))
        rates.saveManualRate(manual(nov10, usd, ars, "1460"))
        assertEquals(
            listOf(manual(nov10, usd, ars, "1460"), manual(nov9, ars, usd, "0.0007")),
            rates.observeManualRates().first(),
        )
    }

    @Test
    fun `deleting a manual rate works for either direction and can be undone by saving`() = runTest {
        rates.saveManualRate(manual(nov9, usd, ars, "1450"))
        rates.observeManualRates().test {
            assertEquals(1, awaitItem().size)
            rates.deleteManualRate(nov9, ars, usd)
            assertEquals(emptyList(), awaitItem())
            rates.saveManualRate(manual(nov9, usd, ars, "1455"))
            assertEquals(listOf(manual(nov9, usd, ars, "1455")), awaitItem())
        }
    }

    @Test
    fun `downloaded rates are stored per date and replaced by a newer download of that date`() = runTest {
        assertNull(rates.latestDownloadedDate())
        rates.saveDownloaded(snapshot(nov9, downloaded(nov9, ars, "1440"), downloaded(nov9, amd, "386.1")))
        rates.saveDownloaded(snapshot(nov10, downloaded(nov10, ars, "1445")))
        rates.saveDownloaded(snapshot(nov9, downloaded(nov9, ars, "1441")))

        assertEquals(nov10, rates.latestDownloadedDate())
        assertEquals(
            setOf(downloaded(nov9, ars, "1441"), downloaded(nov9, amd, "386.1"), downloaded(nov10, ars, "1445")),
            rates.observeRates().first().toSet(),
        )
        assertEquals(emptyList(), rates.observeManualRates().first())
    }

    @Test
    fun `observeRates gives manual and downloaded rates together`() = runTest {
        rates.saveDownloaded(snapshot(nov9, downloaded(nov9, ars, "1440")))
        rates.saveManualRate(manual(nov9, usd, ars, "1500"))
        assertEquals(
            setOf(downloaded(nov9, ars, "1440"), manual(nov9, usd, ars, "1500")),
            rates.observeRates().first().toSet(),
        )
    }

    @Test
    fun `only manual rates go through saveManualRate`() = runTest {
        assertFailsWith<IllegalArgumentException> { rates.saveManualRate(downloaded(nov9, ars, "1440")) }
    }

    @Test
    fun `used currencies come from live accounts and every side of operations`() = runTest {
        val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
        val transactions = RoomTransactionRepository(db.transactionDao(), stamper)
        accounts.saveAccount(Account("card", "Bybit", usdt))
        accounts.saveAccount(Account("cash", "Песо", ars))
        accounts.saveAccount(Account("old", "Евро", eur))
        accounts.deleteAccount("old")
        val at = Instant.parse("2026-11-09T12:00:00Z")
        transactions.saveTransaction(
            Transaction(
                "buy", TransactionType.EXPENSE, "card", Money(10_00, usdt), at,
                originalAmount = Money(14_500_00, amd),
            ),
        )
        assertEquals(setOf(usdt, ars, amd), rates.usedCurrencies())
    }

    @Test
    fun `operation days are local calendar days of live operations`() = runTest {
        val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
        val transactions = RoomTransactionRepository(db.transactionDao(), stamper)
        accounts.saveAccount(Account("cash", "Песо", ars))
        fun op(id: String, at: String) =
            Transaction(id, TransactionType.EXPENSE, "cash", Money(1_00, ars), Instant.parse(at))
        transactions.saveTransaction(op("late", "2026-11-10T02:00:00Z")) // Nov 9, 23:00 in Buenos Aires
        transactions.saveTransaction(op("noon", "2026-11-09T15:00:00Z"))
        transactions.saveTransaction(op("gone", "2026-11-01T15:00:00Z"))
        transactions.deleteTransaction("gone")
        assertEquals(setOf(nov9), rates.operationDates(TimeZone.of("America/Argentina/Buenos_Aires")))
        assertEquals(setOf(nov9, nov10), rates.operationDates(TimeZone.UTC))

        rates.saveDownloaded(snapshot(nov9, downloaded(nov9, ars, "1440"), downloaded(nov9, amd, "386")))
        assertEquals(setOf(nov9), rates.downloadedDates())
    }

    @Test
    fun `backup carries manual rates but not downloaded ones`() = runTest {
        rates.saveManualRate(manual(nov9, usd, ars, "1450.5"))
        rates.saveDownloaded(snapshot(nov9, downloaded(nov9, amd, "386.1")))
        val json = JsonBackupRepository(db, stamper).exportJson()

        val target = createInMemoryCoShellDatabase()
        try {
            val result = JsonBackupRepository(target, stamper).importJson(json)
            assertIs<BackupImportResult.Imported>(result)
            assertEquals(1, result.manualRates)
            val restored = RoomExchangeRateRepository(target.rateDao(), stamper)
            assertEquals(listOf(manual(nov9, usd, ars, "1450.5")), restored.observeRates().first())
        } finally {
            target.close()
        }
    }

    @Test
    fun `backup with a broken rate is rejected`() = runTest {
        val json = """{"format":"co-shell-backup","version":1,"exportedAt":"2026-11-09T00:00:00Z",
            "manualRates":[{"date":"2026-11-09","from":"USD","to":"ARS","rate":"-5"}]}"""
        assertIs<BackupImportResult.Corrupted>(JsonBackupRepository(db, stamper).importJson(json))
        assertEquals(emptyList(), rates.observeManualRates().first())
    }
}
