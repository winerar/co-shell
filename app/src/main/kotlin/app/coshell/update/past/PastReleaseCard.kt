package app.coshell.update.past

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.update.ReleaseNote
import app.coshell.ui.format.formatDayMonth

/**
 * One release on «Прошлые версии» (board Fb284A): «0.37 [у вас] · 12 ноября · 3 ›»; open — all points,
 * closed — the first point on one line with «· …» when there are more.
 */
@Composable
internal fun PastReleaseCard(release: ReleaseNote, isInstalled: Boolean, expanded: Boolean, onToggle: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val items = release.items
    CoShellCard(Modifier.fillMaxWidth(), onClick = onToggle) {
        Column(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Text(release.versionName, style = AmountTitleStyle.copy(fontWeight = FontWeight.Bold))
                if (isInstalled) InstalledBadge()
                Spacer(Modifier.weight(1f))
                Text(
                    listOfNotNull(release.date?.let(::formatDayMonth), items.size.takeIf { it > 0 }?.toString()).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                )
                Icon(
                    painterResource(DesignR.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = muted,
                    modifier = Modifier.rotate(if (expanded) 90f else 0f),
                )
            }
            when {
                items.isEmpty() -> Unit
                expanded -> Column(Modifier.padding(top = Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items.forEach {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                            Text("•", style = MaterialTheme.typography.bodyMedium, color = accentInk())
                            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
                else -> Text(
                    items.first() + if (items.size > 1) " · …" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.xxs),
                )
            }
        }
    }
}

@Composable
private fun InstalledBadge() {
    Text(
        stringResource(R.string.update_past_installed),
        style = MaterialTheme.typography.labelSmall,
        color = accentInk(),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = Spacing.s, vertical = Spacing.xxs),
    )
}
