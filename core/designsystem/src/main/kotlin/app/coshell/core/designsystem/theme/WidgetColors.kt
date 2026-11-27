package app.coshell.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import app.coshell.core.designsystem.category.inkOn

/**
 * Colours of the home-screen widgets and the quick-settings tile (#795, owner's poll 2026-11-27): the same theme as the app, as plain
 * ARGB ints for `RemoteViews` (no Compose there). [card]/[cardEdge] — the widget card and its hairline, [main]/[mainInk]/[mainEdge] — the neon
 * «Расход» button, its ink and its thin ring, [quiet]/[quietInk] — the calm «Доход» button, [text]/[muted] — text on the card. Never black or white
 * as a fill; button ink is the more readable of dark and white (`inkOn`).
 */
@Immutable
data class WidgetColors(
    val card: Int,
    val cardEdge: Int,
    val main: Int,
    val mainInk: Int,
    val mainEdge: Int,
    val quiet: Int,
    val quietInk: Int,
    val text: Int,
    val muted: Int,
)

/** [palette] in light or [dark] mode, as the app's own screens show it. */
fun widgetColorsFor(palette: CoShellPalette, dark: Boolean): WidgetColors {
    val s = themeColorsFor(palette, dark).scheme
    val edge = if (dark) lerp(s.primary, Color.White, 0.35f) else s.primaryFixedDim
    return WidgetColors(
        card = s.surface.toArgb(),
        cardEdge = s.outlineVariant.toArgb(),
        main = s.primary.toArgb(),
        mainInk = inkOn(s.primary).toArgb(),
        mainEdge = edge.toArgb(),
        quiet = s.secondaryContainer.toArgb(),
        quietInk = inkOn(s.secondaryContainer).toArgb(),
        text = s.onSurface.toArgb(),
        muted = s.onSurfaceVariant.toArgb(),
    )
}
