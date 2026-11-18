package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The height the bottom capsule (and the system navigation bar under it) takes at the bottom of the screen, as
 * the app shell measures it; 0 where there is no shell (previews, tests). Screens do not read it directly: they
 * take [underBars], and floating buttons are lifted by it ([CoShellAddFab]).
 */
val LocalBottomBarInset = compositionLocalOf { 0.dp }

/**
 * Padding for a screen's scrolling body that goes *under* the floating capsules (board `Operations` demo, 2026-11-18):
 * the list is as tall as the screen and its content starts below the top capsule and ends above the bottom one —
 * at rest — but passes under them while scrolling, so there is no plate or cut-off band behind the capsules.
 * Use as `contentPadding` of a list, or `.verticalScroll(..).padding(underBars(padding))` for a plain column;
 * never `.padding(padding)` on the scrolling container itself (it clips the content at the capsules).
 * [padding] is the Scaffold's; [extra] is the screen's own content padding added on top; [bottomBar] false —
 * the screen has no capsule below (its own button bar ends the list).
 */
@Composable
fun underBars(padding: PaddingValues, extra: PaddingValues = PaddingValues(), bottomBar: Boolean = true): PaddingValues {
    val direction = LocalLayoutDirection.current
    val inset: Dp = if (bottomBar) LocalBottomBarInset.current else 0.dp
    return PaddingValues(
        start = padding.calculateStartPadding(direction) + extra.calculateStartPadding(direction),
        top = padding.calculateTopPadding() + extra.calculateTopPadding(),
        end = padding.calculateEndPadding(direction) + extra.calculateEndPadding(direction),
        bottom = padding.calculateBottomPadding() + inset + extra.calculateBottomPadding(),
    )
}
