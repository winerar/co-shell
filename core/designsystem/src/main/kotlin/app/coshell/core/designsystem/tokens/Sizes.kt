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
}
