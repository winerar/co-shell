package app.coshell.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import app.coshell.shortcuts.ShortcutLinks

/**
 * A tap on a widget or the tile opens the entry sheet through the shortcut path (#555), so «+» rules apply: PIN first, the update window
 * when entry is closed. A distinct `data` keeps the pending intents apart — extras do not count for equality.
 */
internal object WidgetIntents {
    fun intent(context: Context, key: String, type: String? = null, templateId: String? = null, screen: String? = null): Intent =
        ShortcutLinks.intent(context, type, templateId, screen).setData(Uri.parse("coshell://widget/$key")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun pending(context: Context, key: String, type: String? = null, templateId: String? = null, screen: String? = null): PendingIntent =
        PendingIntent.getActivity(context, key.hashCode(), intent(context, key, type, templateId, screen), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}
