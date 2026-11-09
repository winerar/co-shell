package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.motion.tactileToggleable
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

// List rows of the app (design-system.md §«Компоненты»): medium density — a row with a 40 dp badge is
// 64 dp tall; edge padding Spacing.l; tap = Tactile.Row (depth and light, no tilt). Transparent, so it sits
// on a screen, card or sheet alike; `containerColor` — to cover something under it (swipe-to-delete strip).

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
) {
    val clickable = if (onClick != null) Modifier.tactileClickable(enabled = enabled, onClick = onClick) else Modifier
    RowLayout(modifier.then(clickable), enabled, containerColor, title, supporting, leading, trailing)
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
) = CoShellListRow(
    title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    modifier = modifier,
    supporting = subtitle?.let { { Text(it, maxLines = subtitleMaxLines, overflow = TextOverflow.Ellipsis) } },
    leading = leading,
    trailing = trailing,
    onClick = onClick,
    enabled = enabled,
    containerColor = containerColor,
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
) = RowLayout(
    modifier = modifier.tactileToggleable(value = checked, onValueChange = onCheckedChange),
    enabled = true,
    containerColor = Color.Transparent,
    title = { Text(title) },
    supporting = subtitle?.let { { Text(it) } },
    leading = leading,
    trailing = { CoShellSwitch(checked = checked, onCheckedChange = null) },
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
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .heightIn(min = Sizes.rowMinHeight)
            .padding(horizontal = Spacing.l, vertical = Spacing.m)
            .alpha(if (enabled) 1f else DisabledAlpha),
        horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) CompositionLocalProvider(LocalContentColor provides colors.onSurfaceVariant, content = leading)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
                ProvideTextStyle(typography.bodyLarge, title)
            }
            if (supporting != null) {
                CompositionLocalProvider(LocalContentColor provides colors.onSurfaceVariant) {
                    ProvideTextStyle(typography.bodyMedium, supporting)
                }
            }
        }
        if (trailing != null) CompositionLocalProvider(LocalContentColor provides colors.onSurfaceVariant, content = trailing)
    }
}

/** Material's "disabled" opacity. */
private const val DisabledAlpha = 0.38f
