package app.coshell.core.data.repository

import app.coshell.core.data.db.ReportDao
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
                    )
                }
            }
}
