package app.coshell.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Exchange rates (schema v2). Dates are ISO `YYYY-MM-DD` text (sorts correctly), rates are
// `Rate.toPlainString()` text — never REAL (docs/agents/money-and-currency.md).

/**
 * A rate typed by a family member. Synced table: sync columns and soft delete like the others.
 * [id] is derived from date and the unordered pair ([manualRateId]), so the same pair and day
 * is one record on every phone, whichever direction was typed.
 */
@Entity(tableName = "manual_rates", indices = [Index("date")])
data class ManualRateEntity(
    @PrimaryKey val id: String,
    val date: String,
    @ColumnInfo(name = "from_currency") val fromCurrency: String,
    @ColumnInfo(name = "to_currency") val toCurrency: String,
    val rate: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

/** `2026-11-09:ARS:USD` — currencies in alphabetical order. */
fun manualRateId(date: String, a: String, b: String): String =
    if (a < b) "$date:$a:$b" else "$date:$b:$a"

/**
 * A downloaded rate `1 from = rate to` (cache, not synced, not in backups — can be downloaded again).
 * One row per published date and pair; a newer download of the same date replaces it.
 */
@Entity(tableName = "downloaded_rates", primaryKeys = ["date", "from_currency", "to_currency"])
data class DownloadedRateEntity(
    val date: String,
    @ColumnInfo(name = "from_currency") val fromCurrency: String,
    @ColumnInfo(name = "to_currency") val toCurrency: String,
    val rate: String,
    /** Source name from `RatesSnapshot.source`, e.g. `currency-api`. */
    val source: String,
    @ColumnInfo(name = "downloaded_at") val downloadedAt: Long,
)
