package app.coshell.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** One day of one category, type and currency — a row of [ReportDao.observeDays]. */
data class ReportDayRow(
    /** Days since 1970-01-01 in the zone given by `offsetMs`. */
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    @ColumnInfo(name = "category_id") val categoryId: String,
    val type: String,
    val currency: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
)

/** Reports (#187): SQLite adds the operations up by day, so a year of reports reads day sums, not operations. */
@Dao
interface ReportDao {
    /**
     * Expenses and incomes with a category in [fromMs] ..< [toMs], summed by day, category, type and currency.
     * A day is counted with one UTC offset ([offsetMs]) for the whole range — exact for zones without summer time.
     */
    @Query(
        """SELECT (date + :offsetMs) / 86400000 AS epoch_day, category_id, type, currency, SUM(amount_minor) AS amount_minor
           FROM transactions
           WHERE deleted = 0 AND type IN ('EXPENSE', 'INCOME') AND category_id IS NOT NULL
             AND date >= :fromMs AND date < :toMs
           GROUP BY epoch_day, category_id, type, currency""",
    )
    fun observeDays(fromMs: Long, toMs: Long, offsetMs: Long): Flow<List<ReportDayRow>>
}
