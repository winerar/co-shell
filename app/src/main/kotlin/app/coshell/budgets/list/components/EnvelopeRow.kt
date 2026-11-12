package app.coshell.budgets.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import app.coshell.R
import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.BulletTone
import app.coshell.core.designsystem.components.CoShellBullet
import app.coshell.core.designsystem.components.CoShellExpandIcon
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.bulletColor
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One envelope (`BudA` row): badge, name (and «папка · 3 категории»), «310 000 / 400 000 ARS», the bullet with
 * today's mark and the caption in the colour of the state. [inner] — a limit inside a folder (`BudMany`): smaller,
 * indented, the caption only when something is wrong. A folder gets a fold button when it has inner limits.
 */
@Composable
internal fun EnvelopeRow(
    envelope: Envelope,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    inner: Boolean = false,
    expanded: Boolean? = null,
    onToggle: () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .tactileClickable(onClick = onClick)
            .padding(start = if (inner) Spacing.xxl + Spacing.s else Spacing.l, end = Spacing.l, top = Spacing.m, bottom = Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        CategoryBadge(envelope.icon, envelope.color, size = if (inner) Sizes.badgeSmall else Sizes.badge)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    envelope.name,
                    style = if (inner) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                SpentOfLimit(envelope.spent, envelope.limit)
                if (expanded != null) {
                    CoShellIconButton(onClick = onToggle) {
                        CoShellExpandIcon(expanded, stringResource(if (expanded) R.string.budgets_fold else R.string.budgets_unfold))
                    }
                }
            }
            // Under the name, the whole width: next to the amounts «папка · 3 категории» would be cut.
            subtitle(envelope)?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            CoShellBullet(
                envelope.fraction,
                envelope.tone,
                Modifier.padding(top = Spacing.xs),
                mark = envelope.pace,
                height = if (inner) Sizes.bulletSmall else Sizes.bullet,
            )
            if (!inner || envelope.tone == BulletTone.WARNING || envelope.tone == BulletTone.DANGER) {
                Text(
                    captionText(envelope.caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = when (envelope.tone) {
                        BulletTone.WARNING, BulletTone.DANGER -> bulletColor(envelope.tone)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

/** «310 000 / 400 000 ARS»: spent in the main colour, the limit grey. */
@Composable
private fun SpentOfLimit(spent: String, limit: String) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        buildAnnotatedString {
            append(spent)
            withStyle(SpanStyle(color = muted)) { append(" / $limit") }
        },
        style = RateTextStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
        maxLines = 1,
        modifier = Modifier.padding(start = Spacing.s),
    )
}

@Composable
private fun subtitle(envelope: Envelope): String? {
    val parts = listOfNotNull(
        envelope.folderSize?.let { pluralStringResource(R.plurals.budgets_folder, it, it) },
        envelope.carried?.let { stringResource(R.string.budgets_carried, it) },
    )
    return parts.joinToString(" · ").ifEmpty { null }
}

@Composable
internal fun captionText(caption: Caption): String = when (caption) {
    is Caption.Left -> caption.perDay?.let { stringResource(R.string.budgets_caption_left_per_day, caption.left, it) }
        ?: stringResource(R.string.budgets_caption_left, caption.left)
    is Caption.Ahead -> if (caption.days > 0) {
        pluralStringResource(R.plurals.budgets_caption_ahead, caption.days, caption.days, caption.left)
    } else {
        stringResource(R.string.budgets_caption_ahead_last, caption.left)
    }
    is Caption.Over -> stringResource(R.string.budgets_caption_over, caption.over)
    Caption.Idle -> stringResource(R.string.budgets_caption_idle)
    Caption.Paid -> stringResource(R.string.budgets_caption_paid)
}
