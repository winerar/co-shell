package app.coshell.backup.auto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Where and when the automatic copy was made. [folderUri] `null` — automatic copies are off. */
data class AutoBackupConfig(
    val folderUri: String? = null,
    val folderName: String? = null,
    val lastBackupAtMs: Long? = null,
    val lastFailed: Boolean = false,
    val schedule: BackupSchedule = BackupSchedule(),
)

/** Settings of the automatic copy (phone-only, like other app settings), observable by the backup screen. */
@Singleton
class AutoBackupSettings @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("auto_backup", Context.MODE_PRIVATE)
    private val _config = MutableStateFlow(read())
    val config: StateFlow<AutoBackupConfig> = _config.asStateFlow()

    fun setFolder(uri: String?, name: String?) = write { putString(FOLDER, uri); putString(NAME, name); putBoolean(FAILED, false) }

    fun setSchedule(schedule: BackupSchedule) = write {
        putString(PERIOD, schedule.period.name)
        putString(WEEKDAY, schedule.weekday.name)
        putInt(MONTH_DAY, schedule.monthDay)
        putInt(HOUR, schedule.hour)
        putInt(KEEP_COPIES, schedule.keep)
    }

    fun recordResult(atMs: Long, failed: Boolean) = write {
        if (!failed) putLong(LAST, atMs)
        putBoolean(FAILED, failed)
    }

    private fun write(block: android.content.SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
        _config.value = read()
    }

    private fun read(): AutoBackupConfig {
        val last = prefs.getLong(LAST, 0L).takeIf { it > 0 }
        return AutoBackupConfig(
            folderUri = prefs.getString(FOLDER, null),
            folderName = prefs.getString(NAME, null),
            lastBackupAtMs = last,
            lastFailed = prefs.getBoolean(FAILED, false),
            schedule = readSchedule(last),
        )
    }

    /**
     * Nothing stored (a phone from before #606, or a fresh one) = the old weekly copy: the weekday and hour of the last
     * copy, so the next one still comes a week after it; no copy yet — the defaults of [BackupSchedule].
     */
    private fun readSchedule(lastMs: Long?): BackupSchedule {
        val base = BackupSchedule()
        val lastMoment = lastMs?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) }
        return BackupSchedule(
            period = prefs.getString(PERIOD, null)?.let { name -> BackupPeriod.entries.find { it.name == name } } ?: base.period,
            weekday = prefs.getString(WEEKDAY, null)?.let { name -> DayOfWeek.entries.find { it.name == name } }
                ?: lastMoment?.dayOfWeek ?: base.weekday,
            monthDay = prefs.getInt(MONTH_DAY, base.monthDay).coerceIn(1, MAX_MONTH_DAY),
            hour = if (prefs.contains(HOUR)) prefs.getInt(HOUR, base.hour).coerceIn(0, 23) else lastMoment?.hour ?: base.hour,
            keep = prefs.getInt(KEEP_COPIES, base.keep).coerceAtLeast(1),
        )
    }

    private companion object {
        const val FOLDER = "folder"
        const val NAME = "folder_name"
        const val LAST = "last"
        const val FAILED = "failed"
        const val PERIOD = "period"
        const val WEEKDAY = "weekday"
        const val MONTH_DAY = "month_day"
        const val HOUR = "hour"
        const val KEEP_COPIES = "keep"
        const val MAX_MONTH_DAY = 31
    }
}
