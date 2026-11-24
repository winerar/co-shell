package app.coshell.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Accent for text and thin lines: links, section headers, text buttons, focused fields, the selected tab.
 * Dark theme — the palette's neon itself; light theme — its deep shade (`primaryFixedDim` = [PaletteSpec.inkLight], owner's poll 2026-11-23,
 * #493 Б: «меньше чёрного»; inside the light hero card — the card's own ink). Links take the second colour via [secondaryInk].
 * Fills use `colorScheme.primary` / `primaryContainer`.
 */
@Composable
@ReadOnlyComposable
fun accentInk(): Color = accentInkFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

internal fun accentInkFor(scheme: ColorScheme, dark: Boolean): Color = if (dark) scheme.primary else scheme.primaryFixedDim

/**
 * The second colour (#442 Б, owner's poll 2026-11-23: «основной — сейчас и действие, второй — сравнение и навигация»), both themes:
 * links and text buttons, section headers ([app.coshell.core.designsystem.components.CoShellSectionLabel]), menu icons, outlined
 * secondary buttons, the past period on charts and the mark on a bar. Readable on white and on graphite (`ColorContrastTest`).
 */
@Composable
@ReadOnlyComposable
fun secondaryInk(): Color = MaterialTheme.colorScheme.secondary

/** Fill of the top and bottom capsules (#493 Б): a light tone of the primary in the light neon themes, the accent block otherwise. */
@Composable
@ReadOnlyComposable
fun panelColor(): Color = LocalPanelColor.current

internal val LocalPanelColor = staticCompositionLocalOf { themeColorsFor(CoShellPalette.Lime, dark = false).panel }

/** Colours of a progress indicator: the moving part and the track under it. */
@Immutable
data class ProgressColors(val indicator: Color, val track: Color)

/**
 * Progress in the palette's own neon (owner's poll 2026-11-18, «лайм с обводкой»): the moving part is `primary` in both
 * themes, the track is the quiet accent wash (`surfaceContainerHighest`). On white the neon melts into the page, so the
 * light theme draws a thin deep edge round it ([app.coshell.core.designsystem.components.fillEdge]); the dark theme needs none.
 */
@Composable
@ReadOnlyComposable
fun progressColors(): ProgressColors = progressColorsFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

internal fun progressColorsFor(scheme: ColorScheme, @Suppress("UNUSED_PARAMETER") dark: Boolean) =
    ProgressColors(scheme.primary, scheme.surfaceContainerHighest)

/** Colours of anything chosen: a segment, chip, tab, day, check mark. */
@Immutable
data class SelectionColors(val fill: Color, val content: Color)

/**
 * Chosen state (owner's choice 2026-11-16, «чернильная пилюля», #385): dark theme — the neon fill with its dark ink;
 * light theme — the neon itself with its dark ink (#442: no extra deep shade; the fill melts into the track, so it gets a thin edge — `fillEdge`).
 */
@Composable
@ReadOnlyComposable
fun selectionColors(): SelectionColors = selectionColorsFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

internal fun selectionColorsFor(scheme: ColorScheme, dark: Boolean): SelectionColors =
    if (dark) SelectionColors(scheme.primary, scheme.onPrimary) else SelectionColors(scheme.primary, scheme.onPrimary)

/**
 * Outline of a chosen or filled thing (owner 2026-11-18, #505): always a shade of the theme's main colour — never black
 * or white. Dark theme: a light shade of it; light theme: its deep ink.
 */
@Composable
@ReadOnlyComposable
fun themeEdge(): Color =
    if (LocalDarkTheme.current) lerp(MaterialTheme.colorScheme.primary, Color.White, EDGE_LIGHTEN)
    else MaterialTheme.colorScheme.primaryFixedDim // the palette's deep ink (#493 Б)

private const val EDGE_LIGHTEN = 0.35f
