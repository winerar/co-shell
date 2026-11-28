package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One detail of a form in a single line (#818, poll «А», board `CompChoice` → «Плитки формы»): a pill 40 dp with an
 * icon and the value in words («Сегодня», «Кофейня «Зерно»»). An empty detail — [text] `null` — is the icon alone,
 * a circle 40 dp: the icon speaks for itself, [label] names it for TalkBack (and is read before the value).
 * Several sit in one row instead of the 2×2 [CoShellDetailTile]s, so the entry sheet fits without scrolling.
 */
@Composable
fun CoShellDetailChip(
    icon: Painter,
    text: String?,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = PillShape
    Row(
        modifier
            .heightIn(min = ChipSize)
            .widthIn(min = ChipSize)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
            .tactileClickable(Tactile.Control, shape, role = Role.Button, onClick = onClick)
            .padding(start = if (text == null) 0.dp else Spacing.m, end = if (text == null) 0.dp else Spacing.m + Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s, if (text == null) Alignment.CenterHorizontally else Alignment.Start),
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(IconSize))
        if (text != null) {
            Text(text, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private val ChipSize = Sizes.detailPill
private val IconSize = 18.dp
