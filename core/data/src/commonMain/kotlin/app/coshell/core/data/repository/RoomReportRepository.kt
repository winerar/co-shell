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

/** [ReportRepository] on [ReportDao]: the range is cut at midnight of [timeZone], days use its offset at the start. */
class RoomReportRepository(private val reports: ReportDao, private val timeZone: TimeZone) : ReportRepository {

    override fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<List<ReportDay>> {
        val start = from.atStartOfDayIn(timeZone)
        val offsetMs = timeZone.offsetAt(start).totalSeconds * 1000L
        return reports.observeDays(start.toEpochMilliseconds(), toExclusive.atStartOfDayIn(timeZone).toEpochMilliseconds(), offsetMs)
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
}
