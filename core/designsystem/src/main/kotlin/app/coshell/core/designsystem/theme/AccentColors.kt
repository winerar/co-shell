package app.coshell.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Accent for text and thin lines: links, section headers, text buttons, focused fields, the selected tab.
 * Dark theme — Neon Mint itself; light theme — the ink: neon on white is unreadable, and the owner
 * dropped the deep green (2026-11-09). Fills use `colorScheme.primary` / `primaryContainer`.
 */
@Composable
@ReadOnlyComposable
fun accentInk(): Color = accentInkFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

internal fun accentInkFor(scheme: ColorScheme, dark: Boolean): Color = if (dark) scheme.primary else scheme.onSurface

/** Colours of a progress indicator: the moving part and the track under it. */
@Immutable
data class ProgressColors(val indicator: Color, val track: Color)

/**
 * Graphite-mint progress (owner's choice 2026-11-09): light theme — graphite on a Neon Mint track
 * (neon on white is too pale), dark theme — Neon Mint on a graphite track.
 */
@Composable
@ReadOnlyComposable
fun progressColors(): ProgressColors = progressColorsFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

/** Progress colours: graphite on Neon Mint in the light theme, Neon Mint on graphite in the dark one. */
internal fun progressColorsFor(scheme: ColorScheme, dark: Boolean) =
    if (dark) {
        ProgressColors(scheme.primary, GraphiteLineSoft)
    } else {
        ProgressColors(Graphite4, scheme.primary)
    }
