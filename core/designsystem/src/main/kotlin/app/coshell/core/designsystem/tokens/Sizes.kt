package app.coshell.core.designsystem.tokens

import androidx.compose.ui.unit.dp

/** Fixed sizes of repeating elements (medium list density, design-questions §4). Screens use these, not `N.dp`. */
object Sizes {
    /** List row: at least the touch-friendly 56 dp; with a [badge] and Spacing.m above and below it is 64. */
    val rowMinHeight = 56.dp
    /** Category badge, colour swatch — the 40 dp circle of a 64 dp list row. */
    val badge = 40.dp
    /** Subcategory badge inside an open folder. */
    val badgeSmall = 32.dp
    /** Ring around the selected swatch or icon. */
    val selectionRing = 2.dp
    /** Space under the last list item so a floating button doesn't cover it: FAB 56 + margins 16 + 16. */
    val fabClearance = 88.dp
    /** Big status ring in the middle of a single-purpose screen (update, later import/sync). */
    val heroRing = 168.dp
    /** Icon inside [heroRing]. */
    val heroIcon = 56.dp
    /** Stroke of [heroRing]. */
    val heroStroke = 6.dp
    /** Bento tile on «Обзор» (2 in a row): tall enough for a label, a large amount and a line under it. */
    val tileMinHeight = 128.dp
    /** Small icon in a tile's header. */
    val tileIcon = 20.dp
    /** Height of the mini bars in a tile (expenses per day). */
    val miniBarsHeight = 32.dp
    /** Lowest visible bar: a day with little spending still shows; a day with none is a dot of this height. */
    val miniBarMin = 2.dp
    /** Key of the amount keypad in the entry sheet: big enough to hit with a thumb without looking. */
    val keypadKey = 56.dp
    /** Account tile in the account picker grid (badge, name, balance). */
    val accountTile = 96.dp
    /** Thin outline of a card or a floating pill. */
    val hairline = 1.dp
    /** Floating notice pill over the bottom bar (CoShellNoticePill): a touch-friendly 48 dp. */
    val pillHeight = 48.dp
}
