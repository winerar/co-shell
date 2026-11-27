package app.coshell.widget

import android.content.Context
import app.coshell.access.AccessCenter
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.sync.VersionGate
import app.coshell.core.domain.settings.ThemeSettings
import app.coshell.templates.TemplateCatalog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Keeps the widgets in step with the app (#795): a changed theme, palette, closed entry or template list redraws them. A widget is drawn by the
 * system, not by Compose, so it only changes when the app tells it to. While the app is not running a phone's own dark-mode switch waits for the next
 * periodic update (every 30 minutes) — a limit of `RemoteViews`.
 */
class WidgetRefresh @Inject constructor(
    @ApplicationContext private val context: Context,
    private val theme: ThemeSettings,
    private val syncDao: SyncDao,
    private val access: AccessCenter,
    private val templates: TemplateCatalog,
) {
    suspend fun run() {
        val locked = combine(syncDao.observeState(VersionGate.KEY).map(VersionGate::isLocked), access.state) { version, state ->
            version || state is app.coshell.access.AccessState.Revoked
        }
        combine(theme.mode, theme.palette, locked, templates.observe().map { it.items }) { mode, palette, lock, items -> listOf(mode, palette, lock, items) }
            .distinctUntilChanged()
            .collect { WidgetUpdater.updateAll(context) }
    }
}
