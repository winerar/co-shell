package app.coshell.widget

import android.content.Context
import android.content.res.Configuration
import app.coshell.access.AccessCenter
import app.coshell.access.AccessState
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.sync.VersionGate
import app.coshell.core.designsystem.theme.WidgetColors
import app.coshell.core.designsystem.theme.widgetColorsFor
import app.coshell.core.domain.settings.ThemeSettings
import app.coshell.templates.TemplateCatalog
import app.coshell.templates.TemplateItem
import app.coshell.theme.toDesign
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

/** What a widget or the tile draws (#795): the app's theme, whether entry is closed, the templates. */
data class WidgetData(val colors: WidgetColors, val locked: Boolean, val templates: List<TemplateItem>)

/** The graph pieces a widget needs: a receiver cannot take `@Inject` of a singleton directly. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun themeSettings(): ThemeSettings
    fun syncDao(): SyncDao
    fun access(): AccessCenter
    fun templates(): TemplateCatalog
}

internal fun entryPoint(context: Context): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)

/** Entry is closed: the other phone runs a newer data version (#334), or the invitation code was revoked (#666) — as `InputLockViewModel`. */
internal suspend fun isInputLocked(context: Context): Boolean {
    val ep = entryPoint(context)
    val versionLocked = VersionGate.isLocked(ep.syncDao().observeState(VersionGate.KEY).first())
    return versionLocked || ep.access().state.value is AccessState.Revoked
}

internal suspend fun loadWidgetData(context: Context): WidgetData {
    val ep = entryPoint(context)
    val settings = ep.themeSettings()
    val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val dark = settings.mode.first().isDark(systemDark)
    val colors = widgetColorsFor(settings.palette.first().toDesign(), dark)
    return WidgetData(colors, isInputLocked(context), ep.templates().observe().first().items)
}
