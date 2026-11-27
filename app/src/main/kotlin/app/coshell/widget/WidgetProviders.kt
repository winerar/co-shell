package app.coshell.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
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
            val views = if (provider == TemplatesWidget::class.java.name) {
                TemplatesWidgetViews.build(context, data, WidgetLayouts.isGrid(height))
            } else {
                QuickWidgetViews.build(context, data, WidgetLayouts.quickMode(width))
            }
            manager.updateAppWidget(id, views)
        }
    }
}
