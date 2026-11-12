package app.coshell.testing

import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/** Day sums set by the test; ranges are cut like Room's (from inclusive, to exclusive). */
class FakeReportRepository(days: List<ReportDay> = emptyList()) : ReportRepository {
    val days = MutableStateFlow(days)

    override fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<List<ReportDay>> =
        days.map { all -> all.filter { it.day >= from && it.day < toExclusive } }

    override fun observeAllDays(): Flow<List<ReportDay>> = days
}
