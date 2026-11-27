package app.coshell.widget

import android.widget.RemoteViews
import app.coshell.R

/** Tinting helpers: the drawables are white shapes and glyphs, the theme colour is applied at run time (#795). */
internal fun RemoteViews.tint(viewId: Int, color: Int) = setInt(viewId, "setColorFilter", color)

/** The card: a hairline layer under the fill layer. */
internal fun RemoteViews.paintCard(fill: Int, edge: Int) {
    tint(R.id.w_card_edge, edge)
    tint(R.id.w_card_fill, fill)
}
