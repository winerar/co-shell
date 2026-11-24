package app.coshell.backup.auto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.ui.format.formatClock
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.format.formatWeekdayDayMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** «Резервная копия → Автокопия». */
data class AutoBackupUiState(
    val enabled: Boolean = false,
    val folderName: String? = null,
    /** «9 ноября», `null` — no automatic copy yet. */
    val lastBackup: String? = null,
    val lastFailed: Boolean = false,
    /** Days until the next copy (ring «дней до копии»), `null` — off or no copy yet. */
    val daysLeft: Int? = null,
    val schedule: BackupSchedule = BackupSchedule(),
    /** «вс, 29 ноября» and «3:00» of the next copy for «Следующая: …». */
    val nextDay: String = "",
    val nextTime: String = "",
)

@HiltViewModel
class AutoBackupViewModel @Inject constructor(
    private val settings: AutoBackupSettings,
    private val writer: FolderBackupWriter,
    private val control: AutoBackupScheduler,
    private val timeZone: TimeZone,
    private val clock: Clock,
) : ViewModel() {

    val state: StateFlow<AutoBackupUiState> = settings.config.map { it.toUi() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settings.config.value.toUi())

    /** The user picked a folder: keep access, schedule weekly copies and make the first one now. */
    fun onFolderPicked(treeUri: String) {
        settings.config.value.folderUri?.takeIf { it != treeUri }?.let(writer::releaseFolder)
        val name = runCatching { writer.takeFolder(treeUri) }.getOrNull()
        settings.setFolder(treeUri, name)
        control.enable()
    }

    /** A change of «Каждый день / Раз в неделю / Раз в месяц», the day, the hour or «Хранить». */
    fun setSchedule(schedule: BackupSchedule) = settings.setSchedule(schedule)

    fun disable() {
        settings.config.value.folderUri?.let(writer::releaseFolder)
        settings.setFolder(null, null)
        control.disable()
    }

    private fun AutoBackupConfig.toUi(): AutoBackupUiState {
        val lastMoment = lastBackupAtMs?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone) }
        val last = lastMoment?.date
        val now = clock.now().toLocalDateTime(timeZone)
        val next = AutoBackupPolicy.upcoming(schedule, now)
        return AutoBackupUiState(
            enabled = folderUri != null,
            folderName = folderName,
            lastBackup = last?.let(::formatDayMonth),
            lastFailed = lastFailed,
            daysLeft = if (folderUri != null) AutoBackupPolicy.daysUntilNext(schedule, lastMoment, now) else null,
            schedule = schedule,
            nextDay = formatWeekdayDayMonth(next.date),
            nextTime = clockOf(next.hour),
        )
    }
}

/** «3:00» for the whole [hour]; the tile «Время» and its choices use it too. */
fun clockOf(hour: Int): String = formatClock(LocalDateTime(2000, 1, 1, hour, 0))
