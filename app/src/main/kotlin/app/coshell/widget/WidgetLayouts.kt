package app.coshell.widget

import app.coshell.templates.TemplateItem

/** Which «Быстрая запись» layout fits the width (#795): two round icons on 2×1, words from 3×1. */
enum class QuickMode { ICONS, WORDS }

/**
 * Pure choices of the widgets, apart from `RemoteViews`, so they are tested without a phone (owner's poll 2026-11-27):
 * the quick widget shrinks from 4×1 to 2×1 (icons only), the templates widget is a strip of three (4×1) or a grid (4×2).
 */
object WidgetLayouts {
    /** A cell is ≈ 74 dp: 3×1 is ≈ 220 dp, 2×1 ≈ 146 dp; the two words need about 180 dp. */
    const val WORDS_MIN_DP = 180

    /** One launcher row: Android's `70 × n − 30` for n = 1; a larger minimum made launchers place 2×2 and 4×2 (#821). */
    const val ONE_ROW_DP = 40

    /** The smallest widths, as in `widget_*_info.xml`: 2×1 and 4×1. */
    const val QUICK_MIN_WIDTH_DP = 110
    const val TEMPLATES_MIN_WIDTH_DP = 250

    /** Two rows of chips need about 110 dp; one row fits in a single cell. */
    const val GRID_MIN_HEIGHT_DP = 110

    /** Templates drawn: three, then «+» (strip) or «Все шаблоны ›» (grid). */
    const val CHIPS = 3

    fun quickMode(widthDp: Int): QuickMode = if (widthDp >= WORDS_MIN_DP) QuickMode.WORDS else QuickMode.ICONS

    fun isGrid(heightDp: Int): Boolean = heightDp >= GRID_MIN_HEIGHT_DP

    /** The templates of the widget, in the app's own order. */
    fun chips(items: List<TemplateItem>): List<TemplateItem> = items.take(CHIPS)

    /** The second line of a chip: the amount, or [ask] when it is asked at entry. */
    fun amountLine(item: TemplateItem, ask: String): String = item.amount.ifBlank { ask }
}
