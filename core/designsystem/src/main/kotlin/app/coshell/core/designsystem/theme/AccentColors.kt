package app.coshell.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Accent for text and thin lines: links, section headers, text buttons, focused fields, the selected tab.
 * Dark theme — the palette's neon itself; light theme — its deep shade (`primaryFixedDim`, ≥ 5.4:1 on white, #442; the owner
 * allowed coloured headings and links). Fills use `colorScheme.primary` / `primaryContainer`.
 */
@Composable
@ReadOnlyComposable
fun accentInk(): Color = accentInkFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

internal fun accentInkFor(scheme: ColorScheme, dark: Boolean): Color = scheme.primaryFixedDim

/** Ink of links and text buttons (#442): the accent (`secondary`), readable on white; headings and the selected tab take [accentInk]. */
@Composable
@ReadOnlyComposable
fun secondaryInk(): Color = MaterialTheme.colorScheme.secondary

/** Colours of a progress indicator: the moving part and the track under it. */
@Immutable
data class ProgressColors(val indicator: Color, val track: Color)

/**
 * Graphite-neon progress (owner's choice 2026-11-09): light theme — graphite on a track of the primary neon
 * (neon on white is too pale), dark theme — the neon on a graphite track.
 */
@Composable
@ReadOnlyComposable
fun progressColors(): ProgressColors = progressColorsFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

/** Progress colours: graphite on the neon in the light theme, the neon on graphite in the dark one. */
internal fun progressColorsFor(scheme: ColorScheme, dark: Boolean) =
    if (dark) {
        ProgressColors(scheme.primary, scheme.surfaceContainerHighest)
    } else {
        ProgressColors(scheme.inverseSurface, scheme.primary)
    }

/** Colours of anything chosen: a segment, chip, tab, day, check mark. */
@Immutable
data class SelectionColors(val fill: Color, val content: Color)

/**
 * Chosen state (owner's choice 2026-11-16, «чернильная пилюля», #385): dark theme — the neon fill with its dark ink;
 * light theme — the primary's deep shade (`primaryFixedDim`) with a white label (#442: a palette colour instead of black). Neon on white is ~1:1 to the track.
 */
@Composable
@ReadOnlyComposable
fun selectionColors(): SelectionColors = selectionColorsFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

internal fun selectionColorsFor(scheme: ColorScheme, dark: Boolean): SelectionColors =
    if (dark) SelectionColors(scheme.primary, scheme.onPrimary) else SelectionColors(scheme.primaryFixedDim, scheme.surfaceContainerLowest)
