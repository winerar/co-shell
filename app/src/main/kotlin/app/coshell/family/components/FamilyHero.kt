package app.coshell.family.components

import app.coshell.core.designsystem.components.CoShellDot
import app.coshell.core.designsystem.components.CoShellAvatarStack
import app.coshell.core.designsystem.theme.RateSmallStyle
import app.coshell.core.designsystem.theme.ScreenTitleStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.Member
import app.coshell.core.designsystem.R as DesignR

/** How fresh the family's data is on this phone: the dot of the hero card. */
internal enum class SyncFreshness { SYNCED, PENDING, NEVER }

/**
 * Board `Family`: the family at a glance — members' avatars overlapping, «Общий бюджет · N участника»,
 * a dot with the last sync and how fast changes reach the other phone.
 */
@Composable
internal fun FamilyHero(members: List<Member>, status: String, freshness: SyncFreshness) {
    // A hero: the palette's gradient, as every hero card (owner 2026-11-12, #277).
    CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), accent = true) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                StackedAvatars(members)
                Column {
                    Text(
                        stringResource(R.string.family_budget_title),
                        style = ScreenTitleStyle,
                    )
                    Text(
                        pluralStringResource(R.plurals.family_members_count, members.size, members.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                CoShellDot(freshness.color(), size = Sizes.bullet)
                Text(status, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.family_sync_speed),
                    style = RateSmallStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

/** «Синхронизировать сейчас» — a wide violet outline under the hero card. */
@Composable
internal fun SyncNowButton(enabled: Boolean, onClick: () -> Unit) {
    CoShellOutlinedButton(
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l).height(Sizes.button),
    ) {
        Icon(painterResource(DesignR.drawable.ic_sync), contentDescription = null, modifier = Modifier.size(Sizes.buttonIcon))
        Text(
            stringResource(R.string.family_sync_now),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = Spacing.s),
        )
    }
}

/** Avatars overlapping ([CoShellAvatarStack]); at most [MAX_STACKED]. */
@Composable
private fun StackedAvatars(members: List<Member>) {
    val shown = members.take(MAX_STACKED)
    CoShellAvatarStack(shown.size) { index, shift -> MemberAvatar(shown[index], index, size = Sizes.badgeLarge, modifier = shift) }
}

@Composable
private fun SyncFreshness.color(): Color = when (this) {
    SyncFreshness.SYNCED -> LocalStatusPalette.current.success
    SyncFreshness.PENDING -> LocalStatusPalette.current.warning
    SyncFreshness.NEVER -> MaterialTheme.colorScheme.outline
}

private const val MAX_STACKED = 4
