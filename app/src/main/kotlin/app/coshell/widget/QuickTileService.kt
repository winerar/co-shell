package app.coshell.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import app.coshell.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The «Расход» tile of quick settings (#795, poll 2026-11-27): a tap opens the entry sheet of an expense, with the same rules as «+». Added by the person
 * (it never appears by itself). Closed entry: the tile is dimmed and says «Обновите»; the tap opens the same window.
 */
class QuickTileService : TileService() {
    private var scope: CoroutineScope? = null

    override fun onStartListening() {
        val work = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { scope = it }
        work.launch { render(isInputLocked(applicationContext)) }
    }

    override fun onStopListening() {
        scope?.cancel()
        scope = null
    }

    // The Intent overload is only used below Android 14 (the check is right here), where there is no other.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        val open = {
            val intent = WidgetIntents.intent(this, "tile_expense", type = "EXPENSE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        }
        if (isLocked) unlockAndRun(open) else open()
    }

    private fun render(locked: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (locked) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.label = getString(R.string.tile_label)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_widget_out)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) tile.subtitle = getString(if (locked) R.string.tile_locked else R.string.tile_subtitle)
        tile.updateTile()
    }
}
