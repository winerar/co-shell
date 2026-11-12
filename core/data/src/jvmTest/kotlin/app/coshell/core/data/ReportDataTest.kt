package app.coshell.core.data

import app.cash.turbine.test
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomReportRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportDataTest {
    private val db = createInMemoryCoShellDatabase()
    private val transactions = RoomTransactionRepository(db.transactionDao(), WriteStamper())
    private val tz = TimeZone.of("America/Argentina/Buenos_Aires")
    private val reports = RoomReportRepository(db.reportDao(), tz)
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private var next = 0

    @AfterTest
    fun tearDown() = db.close()

    private suspend fun save(day: Int, hour: Int, category: String?, amount: Money, type: TransactionType = TransactionType.EXPENSE) =
        transactions.saveTransaction(
            Transaction(
                id = "t${next++}", type = type, accountId = "acc", amount = amount,
                date = LocalDateTime(2026, 11, day, hour, 0).toInstant(tz), categoryId = category,
                toAccountId = "other".takeIf { type == TransactionType.TRANSFER },
                toAmount = amount.takeIf { type == TransactionType.TRANSFER },
            ),
        )

    @Test
    fun `operations are summed by local day, category, type and currency`() = runTest {
        save(3, 0, "cafe", Money(100, ars))
        save(3, 23, "cafe", Money(50, ars))
        save(3, 22, "cafe", Money(7, usd))
        save(3, 12, "cafe", Money(30, ars), TransactionType.INCOME)
        save(4, 0, "cafe", Money(1, ars))
        save(3, 12, null, Money(999, ars))
        save(3, 12, "cafe", Money(999, ars), TransactionType.TRANSFER)
        save(1, 0, "cafe", Money(5, ars))
        save(30, 23, "cafe", Money(5, ars))

        val days = reports.observeDays(LocalDate(2026, 11, 2), LocalDate(2026, 11, 5)).first()

        val nov3 = LocalDate(2026, 11, 3)
        assertEquals(
            setOf(
                ReportDay(nov3, "cafe", TransactionType.EXPENSE, Money(150, ars)),
                ReportDay(nov3, "cafe", TransactionType.EXPENSE, Money(7, usd)),
                ReportDay(nov3, "cafe", TransactionType.INCOME, Money(30, ars)),
                ReportDay(LocalDate(2026, 11, 4), "cafe", TransactionType.EXPENSE, Money(1, ars)),
            ),
            days.toSet(),
        )
    }

    @Test
    fun `deleting an operation updates the sums`() = runTest {
        save(3, 12, "cafe", Money(100, ars))
        save(3, 13, "cafe", Money(50, ars))

        reports.observeDays(LocalDate(2026, 11, 1), LocalDate(2026, 12, 1)).test {
            assertEquals(Money(150, ars), awaitItem().single().amount)
            transactions.deleteTransaction("t1")
            assertEquals(Money(100, ars), awaitItem().single().amount)
        }
    }

    @Test
    fun `the whole history is summed by local day, future days too`() = runTest {
        save(1, 0, "cafe", Money(5, ars))
        save(1, 23, "cafe", Money(10, ars))
        save(30, 23, "cafe", Money(7, ars))
        save(3, 12, "cafe", Money(999, ars), TransactionType.TRANSFER)

        val days = reports.observeAllDays().first()

        assertEquals(
            setOf(
                ReportDay(LocalDate(2026, 11, 1), "cafe", TransactionType.EXPENSE, Money(15, ars)),
                ReportDay(LocalDate(2026, 11, 30), "cafe", TransactionType.EXPENSE, Money(7, ars)),
            ),
            days.toSet(),
        )
    }
}
