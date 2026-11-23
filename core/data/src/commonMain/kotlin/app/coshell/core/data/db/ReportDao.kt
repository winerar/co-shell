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
    /** Who added the operations (#189 «Кто сколько потратил»); `null` — unknown (imported, older records). */
    @ColumnInfo(name = "author_member_id") val authorId: String? = null,
)

/** One day of one account, category and type — a row of [ReportDao.observeAccountDays] (#242). */
data class ReportAccountDayRow(
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    val type: String,
    val currency: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
)

/** How one account's balance moved in one day — a row of [ReportDao.observeBalanceDays]; a day may repeat. */
data class BalanceDayRow(
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "delta_minor") val deltaMinor: Long,
)

/** Reports (#187): SQLite adds the operations up by day, so a year of reports reads day sums, not operations. */
@Dao
interface ReportDao {
    /**
     * Expenses and incomes with a category in [fromMs] ..< [toMs], summed by day, category, type, currency and author.
     * A day is counted with one UTC offset ([offsetMs]) for the whole range — exact for zones without summer time.
     */
    @Query(
        """SELECT (date + :offsetMs) / 86400000 AS epoch_day, category_id, type, currency, SUM(amount_minor) AS amount_minor,
                  author_member_id
           FROM transactions
           WHERE deleted = 0 AND type IN ('EXPENSE', 'INCOME') AND category_id IS NOT NULL
             AND date >= :fromMs AND date < :toMs
           GROUP BY epoch_day, category_id, type, currency, author_member_id""",
    )
    fun observeDays(fromMs: Long, toMs: Long, offsetMs: Long): Flow<List<ReportDayRow>>

    /** «Чем платили» (#242): as [observeDays], but by the account the money left instead of by author. */
    @Query(
        """SELECT (date + :offsetMs) / 86400000 AS epoch_day, account_id, category_id, type, currency,
                  SUM(amount_minor) AS amount_minor
           FROM transactions
           WHERE deleted = 0 AND type IN ('EXPENSE', 'INCOME') AND category_id IS NOT NULL
             AND date >= :fromMs AND date < :toMs
           GROUP BY epoch_day, account_id, category_id, type, currency""",
    )
    fun observeAccountDays(fromMs: Long, toMs: Long, offsetMs: Long): Flow<List<ReportAccountDayRow>>

    /**
     * «Капитал» (#189): the balance change of each account by day over the whole history — expenses and outgoing
     * transfers subtract `amount`, incomes add it, incoming transfers add `to_amount` (as `observeBalanceDeltas`).
     */
    @Query(
        """SELECT (date + :offsetMs) / 86400000 AS epoch_day, account_id,
                  SUM(CASE type WHEN 'INCOME' THEN amount_minor ELSE -amount_minor END) AS delta_minor
           FROM transactions WHERE deleted = 0 GROUP BY epoch_day, account_id
           UNION ALL
           SELECT (date + :offsetMs) / 86400000 AS epoch_day, to_account_id AS account_id, SUM(to_amount_minor) AS delta_minor
           FROM transactions WHERE deleted = 0 AND type = 'TRANSFER' GROUP BY epoch_day, to_account_id
           UNION ALL
           SELECT (date + :offsetMs) / 86400000 AS epoch_day, account_id,
                  SUM(CASE WHEN (direction = 'LENT') = (kind = 'ISSUE') THEN -amount_minor ELSE amount_minor END) AS delta_minor
           FROM debt_events WHERE deleted = 0 AND kind IN ('ISSUE', 'RETURN') AND account_id IS NOT NULL GROUP BY epoch_day, account_id""",
    )
    fun observeBalanceDays(offsetMs: Long): Flow<List<BalanceDayRow>>
}
