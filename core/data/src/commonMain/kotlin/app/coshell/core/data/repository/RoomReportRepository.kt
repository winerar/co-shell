package app.coshell.core.data.repository

import app.coshell.core.data.db.ReportDao
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.ForeignPurchase
import app.coshell.core.domain.reports.ReportAccountDay
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportRepository
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * [ReportRepository] on [ReportDao]: the range is cut at midnight of [timeZone], days use its offset at the start
 * (the whole history — the offset of now).
 */
class RoomReportRepository(
    private val reports: ReportDao,
    private val timeZone: TimeZone,
    private val clock: Clock = Clock.System,
) : ReportRepository {

    override fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<List<ReportDay>> {
        val start = from.atStartOfDayIn(timeZone)
        return days(start.toEpochMilliseconds(), toExclusive.atStartOfDayIn(timeZone).toEpochMilliseconds(), offsetMs(start))
    }

    override fun observeAllDays(): Flow<List<ReportDay>> = days(Long.MIN_VALUE, Long.MAX_VALUE, offsetMs(clock.now()))

    override fun observeAccountDays(from: LocalDate, toExclusive: LocalDate): Flow<List<ReportAccountDay>> {
        val start = from.atStartOfDayIn(timeZone)
        val to = toExclusive.atStartOfDayIn(timeZone)
        return reports.observeAccountDays(start.toEpochMilliseconds(), to.toEpochMilliseconds(), offsetMs(start)).map { rows ->
            rows.map {
                ReportAccountDay(
                    LocalDate.fromEpochDays(it.epochDay), it.accountId, it.categoryId,
                    TransactionType.valueOf(it.type), Money(it.amountMinor, CurrencyCode(it.currency)),
                )
            }
        }
    }

    override fun observeForeignPurchases(from: LocalDate, toExclusive: LocalDate): Flow<List<ForeignPurchase>> =
        reports.observeForeignPurchases(from.atStartOfDayIn(timeZone).toEpochMilliseconds(), toExclusive.atStartOfDayIn(timeZone).toEpochMilliseconds())
            .map { rows ->
                rows.map {
                    ForeignPurchase(
                        id = it.id,
                        day = Instant.fromEpochMilliseconds(it.date).toLocalDateTime(timeZone).date,
                        accountId = it.accountId,
                        type = TransactionType.valueOf(it.type),
                        paid = Money(it.amountMinor, CurrencyCode(it.currency)),
                        price = Money(it.originalAmountMinor, CurrencyCode(it.originalCurrency)),
                    )
                }
            }

    override fun observeBalanceDays(): Flow<List<BalanceDay>> =
        reports.observeBalanceDays(offsetMs(clock.now())).map { rows ->
            rows.map { BalanceDay(LocalDate.fromEpochDays(it.epochDay), it.accountId, it.deltaMinor) }
        }

    private fun offsetMs(at: Instant): Long = timeZone.offsetAt(at).totalSeconds * 1000L

    private fun days(fromMs: Long, toMs: Long, offsetMs: Long): Flow<List<ReportDay>> =
        reports.observeDays(fromMs, toMs, offsetMs)
            .map { rows ->
                rows.map {
                    ReportDay(
                        day = LocalDate.fromEpochDays(it.epochDay),
                        categoryId = it.categoryId,
                        type = TransactionType.valueOf(it.type),
                        amount = Money(it.amountMinor, CurrencyCode(it.currency)),
                        authorId = it.authorId,
                    )
                }
            }
}
