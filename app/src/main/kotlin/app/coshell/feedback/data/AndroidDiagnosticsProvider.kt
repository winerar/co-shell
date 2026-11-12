package app.coshell.feedback.data

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.integrity.IntegrityCheck
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.domain.feedback.DiagnosticsProvider
import app.coshell.core.domain.settings.ThemeSettings
import app.coshell.crash.CrashLog
import app.coshell.update.log.UpdateJournal
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Technical lines for a feedback message (#216): install permission and source, firmware, free space,
 * language, theme, sync state, data integrity (#219), the last crash (#218) and the update journal. Nothing about amounts, accounts or people.
 */
class AndroidDiagnosticsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val journal: UpdateJournal,
    private val syncDao: SyncDao,
    private val theme: ThemeSettings,
    private val crashes: CrashLog,
    private val integrity: IntegrityCheck,
) : DiagnosticsProvider {

    override suspend fun diagnostics(): List<String> = withContext(Dispatchers.IO) {
        buildList {
            add("install permission: ${context.packageManager.canRequestPackageInstalls()}")
            add("installer: ${installer()}")
            firmware()?.let { add("firmware: $it") }
            add("free space: ${context.filesDir.usableSpace / MEGABYTE} MB")
            add("language: ${Locale.getDefault().toLanguageTag()}")
            add("theme: ${theme.mode.first()}, system dark: ${systemDark()}")
            add("sync: last ${lastSync()}, pending ${syncDao.observePendingCount().first()}")
            add("integrity: ${integrityLine()}")
            crashes.pending()?.let { addAll(it.lines()) }
            add("manual install preferred: ${journal.preferManualInstall}")
            val lines = journal.lines()
            add("update journal (${lines.size}):")
            addAll(lines)
        }
    }

    private fun installer(): String = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val info = context.packageManager.getInstallSourceInfo(context.packageName)
            "${info.installingPackageName} (initiated by ${info.initiatingPackageName})"
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName).toString()
        }
    }.getOrElse { "unknown (${it::class.simpleName})" }

    /** MIUI / HyperOS version and «MIUI optimization», when the phone tells them. */
    private fun firmware(): String? {
        val parts = listOfNotNull(
            systemProperty("ro.mi.os.version.name")?.let { "HyperOS $it" },
            systemProperty("ro.miui.ui.version.name")?.let { "MIUI $it" },
            systemProperty("persist.sys.miui_optimization")?.let { "optimization $it" },
        )
        return parts.joinToString(", ").ifEmpty { null }
    }

    private suspend fun integrityLine(): String = runCatching { integrity.run() }.fold(
        onSuccess = { problems -> if (problems.isEmpty()) "ok" else problems.joinToString("; ") { it.describe() } },
        onFailure = { "check failed (${it::class.simpleName})" },
    )

    private suspend fun lastSync(): String =
        syncDao.state(SyncEngine.LAST_SYNC)?.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it).toString() } ?: "never"

    private fun systemDark(): Boolean =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private companion object {
        const val MEGABYTE = 1024 * 1024

        /** `android.os.SystemProperties` is hidden; reflection may be refused — then nothing is reported. */
        fun systemProperty(name: String): String? = runCatching {
            Class.forName("android.os.SystemProperties").getMethod("get", String::class.java).invoke(null, name) as String
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
