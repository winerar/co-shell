package app.coshell.backup.auto

import kotlinx.datetime.DayOfWeek

/** How often the automatic copy is made («Каждый день / Раз в неделю / Раз в месяц», board CopyA, #606). */
enum class BackupPeriod(val days: Int) {
    DAILY(1),
    WEEKLY(7),
    MONTHLY(31),
}

/**
 * When and how many automatic copies are made. The defaults are the behaviour before #606: once a week, 8 copies kept.
 * [monthDay] 31 means «the last day» in a shorter month; [hour] is the phone's local hour (0..23).
 */
data class BackupSchedule(
    val period: BackupPeriod = BackupPeriod.WEEKLY,
    val weekday: DayOfWeek = DayOfWeek.SUNDAY,
    val monthDay: Int = 1,
    val hour: Int = DEFAULT_HOUR,
    val keep: Int = AutoBackupPolicy.KEEP,
) {
    companion object {
        const val DEFAULT_HOUR = 3

        /** Choices of «Хранить»; the default ([AutoBackupPolicy.KEEP]) is one of them. */
        val KEEP_OPTIONS = listOf(3, 5, AutoBackupPolicy.KEEP, 12, 30)
    }
}
