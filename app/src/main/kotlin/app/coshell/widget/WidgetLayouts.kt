package app.coshell.widget

import app.coshell.templates.TemplateItem

/** Which «Быстрая запись» layout fits the width (#795): two round icons on 2×1, words from 3×1. */
enum class QuickMode { ICONS, WORDS }

/**
 * Pure choices of the widgets, apart from `RemoteViews`, so they are tested without a phone (owner's poll 2026-11-27):
 * the quick widget shrinks from 4×1 to 2×1 (icons only), the templates widget is a strip (one row) or a grid (two rows) of as many templates as fit the width (#821).
 */
object WidgetLayouts {
    /** A cell is ≈ 74 dp: 3×1 is ≈ 220 dp, 2×1 ≈ 146 dp; the two words need about 180 dp. */
    const val WORDS_MIN_DP = 180

    /** One launcher row: Android's `70 × n − 30` for n = 1; a larger minimum made launchers place 2×2 and 4×2 (#821). */
    const val ONE_ROW_DP = 40

    /** The smallest width of both widgets (2×1) and the templates' default (4×1), as in `widget_*_info.xml`. */
    const val QUICK_MIN_WIDTH_DP = 110
    const val TEMPLATES_MIN_WIDTH_DP = 250

    /** Two rows of chips need about 110 dp; one row fits in a single cell. */
    const val GRID_MIN_HEIGHT_DP = 110

    /** Templates drawn at most (5 cells wide), then «+» (strip) or «Все шаблоны ›» (grid). */
    const val CHIPS = 4

    /** Widths of 3, 4 and 5 cells (`70 × n − 30`): one more template each (poll 2026-11-28 «А», #821). */
    private val MORE_CHIPS_AT_DP = intArrayOf(180, 250, 320)

    fun quickMode(widthDp: Int): QuickMode = if (widthDp >= WORDS_MIN_DP) QuickMode.WORDS else QuickMode.ICONS

    fun isGrid(heightDp: Int): Boolean = heightDp >= GRID_MIN_HEIGHT_DP

    /** How many templates fit the width: 1 on 2 cells … 4 on 5 cells; the chips never shrink below a readable name. */
    fun chipCount(widthDp: Int): Int = 1 + MORE_CHIPS_AT_DP.count { widthDp >= it }

    /** Every width where [chipCount] changes, the smallest first: the launcher's size ranges on Android 12+. */
    val chipWidthsDp: List<Int> = listOf(QUICK_MIN_WIDTH_DP) + MORE_CHIPS_AT_DP.toList()

    /** The templates of the widget, in the app's own order, as many as fit [widthDp]. */
    fun chips(items: List<TemplateItem>, widthDp: Int): List<TemplateItem> = items.take(chipCount(widthDp))

    /** The grid's first row: half of the cells (templates and «Все шаблоны ›»), the larger half on top. */
    fun firstRow(cells: Int): Int = (cells + 1) / 2

    /** The second line of a chip: the amount, or [ask] when it is asked at entry. */
    fun amountLine(item: TemplateItem, ask: String): String = item.amount.ifBlank { ask }
}
