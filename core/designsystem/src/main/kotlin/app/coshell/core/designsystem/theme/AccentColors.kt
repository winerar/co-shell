package app.coshell.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Accent for text and thin lines: links, section headers, text buttons, focused fields, the selected tab.
 * Dark theme — the palette's neon itself; light theme — the ink (neon on white is unreadable, and the owner refused an extra deep shade of the
 * primary, #442: links take the palette's accent via [secondaryInk]). Fills use `colorScheme.primary` / `primaryContainer`.
 */
@Composable
@ReadOnlyComposable
fun accentInk(): Color = accentInkFor(MaterialTheme.colorScheme, LocalDarkTheme.current)

internal fun accentInkFor(scheme: ColorScheme, dark: Boolean): Color = if (dark) scheme.primary else scheme.onSurface

/** Ink of links and text buttons (#442): the accent (`secondary`), readable on white; headings and the selected tab take [accentInk]. */
@Composable
@ReadOnlyComposable
fun secondaryInk(): Color = MaterialTheme.colorScheme.secondary

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
 * or white. Dark theme: a light shade of it; light theme: a deep one.
 */
@Composable
@ReadOnlyComposable
fun themeEdge(): Color =
    if (LocalDarkTheme.current) lerp(MaterialTheme.colorScheme.primary, Color.White, EDGE_LIGHTEN)
    else lerp(MaterialTheme.colorScheme.primary, Color.Black, EDGE_DEEPEN)

private const val EDGE_LIGHTEN = 0.35f
private const val EDGE_DEEPEN = 0.5f
