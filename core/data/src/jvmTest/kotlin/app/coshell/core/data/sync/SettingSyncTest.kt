package app.coshell.core.data.sync

import app.coshell.core.data.remote.ApiException
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class SettingSyncTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val server = FakeRemoteStore()
    private val a = Phone("member-a", server, startMs = 1_000_000)
    private val b = Phone("member-b", server, startMs = 1_000_000)

    @AfterTest
    fun tearDown() { a.close(); b.close() }

    @Test
    fun `settings chosen on one phone reach the other`() = runTest {
        a.settings.setCurrencies(listOf(ars, usd))
        a.settings.setMonthStartDay(10)

        assertEquals(2, a.engine.sync().pushed)
        assertEquals(
            setOf("setting_report_currencies", "setting_month_start_day"),
            server.records.map { it.record.key }.toSet(),
        )
        b.engine.sync()

        assertEquals(listOf(ars, usd), b.settings.currencies.first())
        assertEquals(10, b.settings.monthStartDay.first())
        assertEquals(0, b.db.syncDao().observePendingCount().first(), "rows from the server are not sent back")
    }

    @Test
    fun `later change wins on both phones`() = runTest {
        a.settings.setMonthStartDay(10)
        a.engine.sync(); b.engine.sync()

        b.tick(); b.settings.setMonthStartDay(25)
        b.engine.sync(); a.engine.sync()

        assertEquals(25, a.settings.monthStartDay.first())
        assertEquals(25, b.settings.monthStartDay.first())
    }

    @Test
    fun `legacy device list gives way to the family choice`() = runTest {
        a.settings.setCurrencies(listOf(amd, usd))
        a.engine.sync()

        b.settings.importLegacyReportCurrencies(listOf(ars))
        b.engine.sync()

        assertEquals(listOf(amd, usd), b.settings.currencies.first())
        a.engine.sync()
        assertEquals(listOf(amd, usd), a.settings.currencies.first())
    }

    @Test
    fun `old rules without the setting kind do not hold back the budget data`() = runTest {
        server.rejectedKinds = setOf(SyncKind.SETTING)
        a.settings.setMonthStartDay(10)
        a.transactions.saveTransaction(
            Transaction(
                id = "t1", type = TransactionType.EXPENSE, accountId = "card", amount = Money(1_000_00, ars),
                date = Instant.fromEpochMilliseconds(1_000_000), categoryId = "cafe",
            ),
        )

        val error = assertFailsWith<ApiException> { a.engine.sync() }
        assertEquals(true, error.isPermissionDenied)
        assertEquals(listOf("transaction_t1"), server.records.map { it.record.key })

        server.rejectedKinds = emptySet()
        a.engine.sync()
        b.engine.sync()
        assertEquals(10, b.settings.monthStartDay.first())
    }
}
