package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.smoothResize
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.motion.tactileCombinedClickable
import app.coshell.core.designsystem.motion.tactileToggleable
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

// List rows of the app (design-system.md §«Компоненты»): medium density — a row with a 40 dp badge is
// 64 dp tall; edge padding Spacing.l; tap = Tactile.Row (depth and light, no tilt); `onLongClick` — the selection mode of a list. Transparent, so it sits
// on a screen, card or sheet alike; `containerColor` — to cover something under it (swipe-to-delete strip).
// `compact` — «Компактные списки» (Set1Look): 44 dp at least, Spacing.xs above and below; only the data lists of «Операции» and «Счета» ask for it.
// `dense` — menu density (154А, #155): 52 dp at least, Spacing.s above and below, the grey line in bodySmall — for menus of sections, not data lists.

/** Slot row: [title] in bodyLarge, [supporting] in grey bodyMedium, [leading]/[trailing] in grey. */
@Composable
fun CoShellListRow(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supporting: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
    onLongClick: (() -> Unit)? = null,
    dense: Boolean = false,
    compact: Boolean = false,
) {
    val clickable = when {
        onClick != null && onLongClick != null ->
            Modifier.tactileCombinedClickable(enabled = enabled, onLongClick = onLongClick, onClick = onClick)
        onClick != null -> Modifier.tactileClickable(enabled = enabled, onClick = onClick)
        else -> Modifier
    }
    RowLayout(modifier.then(clickable), enabled, containerColor, title, supporting, leading, trailing, dense, compact)
}

/** Text row: one-line [title], optional [subtitle] (up to [subtitleMaxLines]). */
@Composable
fun CoShellListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleMaxLines: Int = Int.MAX_VALUE,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
    onLongClick: (() -> Unit)? = null,
    dense: Boolean = false,
    compact: Boolean = false,
) = CoShellListRow(
    title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    modifier = modifier,
    supporting = subtitle?.let { { Text(it, maxLines = subtitleMaxLines, overflow = TextOverflow.Ellipsis) } },
    leading = leading,
    trailing = trailing,
    onClick = onClick,
    enabled = enabled,
    containerColor = containerColor,
    onLongClick = onLongClick,
    dense = dense,
    compact = compact,
)

/** Settings row with a switch; the whole row toggles (one target for TalkBack, the switch only shows). */
@Composable
fun CoShellSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) = RowLayout(
    // Disabled (#145: «Отпечаток» while there is no PIN): 38 %, the row does not toggle.
    modifier = if (enabled) modifier.tactileToggleable(value = checked, onValueChange = onCheckedChange) else modifier,
    enabled = enabled,
    containerColor = Color.Transparent,
    title = { Text(title) },
    supporting = subtitle?.let { { Text(it) } },
    leading = leading,
    trailing = { CoShellSwitch(checked = checked, onCheckedChange = null, enabled = enabled) },
)

@Composable
private fun RowLayout(
    modifier: Modifier,
    enabled: Boolean,
    containerColor: Color,
    title: @Composable () -> Unit,
    supporting: (@Composable () -> Unit)?,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    dense: Boolean = false,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val gap = Spacing.l
    val minText = TEXT_MIN_WIDTH * LocalDensity.current.fontScale.coerceAtLeast(1f)
    Layout(
        content = {
            if (leading != null) {
                Box(Modifier.layoutId(Slot.Leading)) {
                    CompositionLocalProvider(LocalContentColor provides colors.onSurfaceVariant, content = leading)
                }
            }
            Column(Modifier.layoutId(Slot.Text), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
                    ProvideTextStyle(typography.bodyLarge, title)
                }
                if (supporting != null) {
                    CompositionLocalProvider(LocalContentColor provides colors.onSurfaceVariant) {
                        ProvideTextStyle(if (dense) typography.bodySmall else typography.bodyMedium, supporting)
                    }
                }
            }
            if (trailing != null) {
                Box(Modifier.layoutId(Slot.Trailing), contentAlignment = Alignment.CenterEnd) {
                    CompositionLocalProvider(LocalContentColor provides colors.onSurfaceVariant, content = trailing)
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .smoothResize() // a subtitle may appear or wrap to two lines (issue #64)
            .background(containerColor)
            .heightIn(min = if (compact) Sizes.rowCompactMinHeight else if (dense) Sizes.rowDenseMinHeight else Sizes.rowMinHeight)
            .padding(horizontal = Spacing.l, vertical = if (compact) Spacing.xs else if (dense) Spacing.s else Spacing.m)
            .alpha(if (enabled) 1f else DisabledAlpha),
    ) { measurables, constraints ->
        val gapPx = gap.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val leadingP = measurables.firstOrNull { it.layoutId == Slot.Leading }?.measure(loose)
        val left = (leadingP?.width?.plus(gapPx)) ?: 0
        val room = (constraints.maxWidth - left).coerceAtLeast(0)
        val trailingM = measurables.firstOrNull { it.layoutId == Slot.Trailing }
        val textM = measurables.first { it.layoutId == Slot.Text }
        // The amount stays beside the text while the text keeps [minText]; otherwise it goes under it (end-aligned),
        // so a huge amount never squeezes the words into columns of letters.
        val trailingNatural = trailingM?.measure(Constraints(maxWidth = room))
        val beside = trailingNatural == null || room - trailingNatural.width - gapPx >= minText.roundToPx()
        val textP = textM.measure(
            Constraints(maxWidth = if (beside && trailingNatural != null) room - trailingNatural.width - gapPx else room),
        )
        val trailingP = trailingNatural
        val contentH = if (beside) maxOf(textP.height, trailingP?.height ?: 0) else textP.height + (trailingP?.height ?: 0) + Spacing.xxs.roundToPx()
        val height = maxOf(contentH, leadingP?.height ?: 0).coerceIn(constraints.minHeight, constraints.maxHeight.coerceAtLeast(constraints.minHeight))
        layout(constraints.maxWidth, height) {
            leadingP?.placeRelative(0, (height - leadingP.height) / 2)
            if (beside) {
                textP.placeRelative(left, (height - textP.height) / 2)
                trailingP?.placeRelative(constraints.maxWidth - trailingP.width, (height - trailingP.height) / 2)
            } else {
                val top = (height - contentH) / 2
                textP.placeRelative(left, top)
                trailingP?.placeRelative(constraints.maxWidth - trailingP.width, top + textP.height + Spacing.xxs.roundToPx())
            }
        }
    }
}

private enum class Slot { Leading, Text, Trailing }

/** The text of a row keeps at least this much (× the font scale) beside the amount; less — the amount goes under it. */
private val TEXT_MIN_WIDTH = 120.dp

/** Material's "disabled" opacity. */
private const val DisabledAlpha = 0.38f
