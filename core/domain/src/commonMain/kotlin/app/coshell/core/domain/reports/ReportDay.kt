package app.coshell.core.domain.reports

import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * The sum of one day's operations of one category, type and currency — what reports are counted from
 * (#187). Room adds them up (`ReportDao`), so a year of reports never loads the operations themselves.
 * [type] is [TransactionType.EXPENSE] or [TransactionType.INCOME]: transfers are not in reports.
 */
data class ReportDay(
    val day: LocalDate,
    val categoryId: String,
    val type: TransactionType,
    val amount: Money,
)

interface ReportRepository {
    /** Live day sums of [from] ..< [toExclusive] (days in the phone's time zone). */
    fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<List<ReportDay>>

    /** Live day sums of the whole history, future-dated operations too — budget rollover walks back through every month (#262). */
    fun observeAllDays(): Flow<List<ReportDay>>
}
