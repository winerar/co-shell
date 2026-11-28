package app.coshell.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** «Быстрая запись» (#795): redrawn on add, resize and every theme or lock change (`WidgetRefresh`). */
class QuickEntryWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = launchAsync { WidgetUpdater.update(context, manager, ids.toList()) }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) =
        launchAsync { WidgetUpdater.update(context, manager, listOf(id)) }
}

/** «Шаблоны» (#795). */
class TemplatesWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = launchAsync { WidgetUpdater.update(context, manager, ids.toList()) }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) =
        launchAsync { WidgetUpdater.update(context, manager, listOf(id)) }
}

private fun BroadcastReceiver.launchAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
        try {
            block()
        } finally {
            pending.finish()
        }
    }
}

/** Draws every placed widget from the current data. */
object WidgetUpdater {
    /** All placed widgets of both kinds; nothing is read when none is placed. */
    suspend fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, QuickEntryWidget::class.java)) + manager.getAppWidgetIds(ComponentName(context, TemplatesWidget::class.java))
        if (ids.isNotEmpty()) update(context, manager, ids.toList())
    }

    internal suspend fun update(context: Context, manager: AppWidgetManager, ids: List<Int>) {
        if (ids.isEmpty()) return
        val data = loadWidgetData(context)
        for (id in ids) {
            val options = manager.getAppWidgetOptions(id)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
            val provider = manager.getAppWidgetInfo(id)?.provider?.className
            val templates = provider == TemplatesWidget::class.java.name
            val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // The launcher picks the layout by the real size: the old height option said «two rows» for one row on some phones (#821).
                responsive(context, data, templates)
            } else if (templates) {
                TemplatesWidgetViews.build(context, data, WidgetLayouts.isGrid(height))
            } else {
                QuickWidgetViews.build(context, data, WidgetLayouts.quickMode(width))
            }
            manager.updateAppWidget(id, views)
        }
    }
}

/** Android 12+: one `RemoteViews` per size range, the launcher shows the largest that fits (#821). */
@RequiresApi(Build.VERSION_CODES.S)
private fun responsive(context: Context, data: WidgetData, templates: Boolean): RemoteViews {
    val oneRow = WidgetLayouts.ONE_ROW_DP.toFloat()
    val sizes = if (templates) {
        mapOf(
            SizeF(WidgetLayouts.TEMPLATES_MIN_WIDTH_DP.toFloat(), oneRow) to TemplatesWidgetViews.build(context, data, grid = false),
            SizeF(WidgetLayouts.TEMPLATES_MIN_WIDTH_DP.toFloat(), WidgetLayouts.GRID_MIN_HEIGHT_DP.toFloat()) to TemplatesWidgetViews.build(context, data, grid = true),
        )
    } else {
        mapOf(
            SizeF(WidgetLayouts.QUICK_MIN_WIDTH_DP.toFloat(), oneRow) to QuickWidgetViews.build(context, data, QuickMode.ICONS),
            SizeF(WidgetLayouts.WORDS_MIN_DP.toFloat(), oneRow) to QuickWidgetViews.build(context, data, QuickMode.WORDS),
        )
    }
    return RemoteViews(sizes)
}
