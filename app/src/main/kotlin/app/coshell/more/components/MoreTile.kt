package app.coshell.more.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/** A section of «Ещё» as a bento tile: icon in a soft square of its colour, title, a line about it. */
@Composable
internal fun MoreTile(icon: Int, title: String, subtitle: String?, color: Long, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tint = categoryColor(color)
    CoShellCard(modifier = modifier.heightIn(min = Sizes.tileMinHeight), onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Box(
                Modifier.size(Sizes.badge).background(tint.copy(alpha = 0.16f), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), contentDescription = null, tint = tint)
            }
            Spacer(Modifier.heightIn(min = Spacing.s))
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Two lines in every tile, so tiles in a row are of one height.
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
