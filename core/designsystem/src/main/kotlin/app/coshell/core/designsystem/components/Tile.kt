package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A bento tile (design-refactor Step 5): a small [CoShellCard] of a 2-column grid on «Обзор» — an [icon]
 * in [iconColor] and a grey [title] on top, [content] (a large number, mini bars) under it, [footer] pinned
 * to the bottom. At least [Sizes.tileMinHeight] tall; give tiles of one row `Modifier.fillMaxHeight()` inside
 * a `Row(Modifier.height(IntrinsicSize.Min))` so they match. [onClick] — the whole tile is a tap target.
 * [tint] — a status colour (e.g. warning): the border, a faint fill and the title take it.
 */
@Composable
fun CoShellTile(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    iconColor: Color = accentInk(),
    onClick: (() -> Unit)? = null,
    tint: Color? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    CoShellCard(modifier = modifier.heightIn(min = Sizes.tileMinHeight), tint = tint, onClick = onClick) {
        Column(
            Modifier.padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                if (icon != null) Icon(painterResource(icon), contentDescription = null, Modifier.size(Sizes.tileIcon), tint = iconColor)
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            content()
        }
        if (footer != null) {
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth().padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.l), content = footer)
        }
    }
}
