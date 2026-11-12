package app.coshell.budgets.detail.components

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
import app.coshell.R
import app.coshell.budgets.detail.BudgetDetailUiState.Head
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.charts.CoShellBullet
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.charts.bulletColor
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The top of `BudDetail`: «90 000 ARS» left (or overspent) and «осталось · 19 дней», the bullet with today's mark,
 * «Потрачено 310 000 из 400 000 ARS · к сегодня должно быть ≈ 147 000» in the colour of the state.
 */
@Composable
internal fun EnvelopeHead(head: Head, modifier: Modifier = Modifier) {
    val envelope = head.envelope
    val alarm = envelope.tone == BulletTone.WARNING || envelope.tone == BulletTone.DANGER
    CoShellCard(modifier.fillMaxWidth(), accent = true) {
        Column(Modifier.padding(Spacing.l)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                AmountText(
                    head.left,
                    AmountTitleStyle,
                    color = if (head.over) bulletColor(BulletTone.DANGER) else LocalHeroPalette.current.amount,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stateLine(head),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = Spacing.s),
                )
            }
            CoShellBullet(envelope.fraction, envelope.tone, Modifier.padding(top = Spacing.m), mark = envelope.pace, height = Sizes.bulletLarge)
            Text(
                spentLine(head),
                style = MaterialTheme.typography.bodySmall,
                color = if (alarm) bulletColor(envelope.tone) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.s),
            )
            val extra = listOfNotNull(
                envelope.carried?.let { stringResource(R.string.budgets_carried, it) },
                envelope.missing.takeIf { it.isNotEmpty() }?.let { stringResource(R.string.budgets_missing, it.joinToString(", ")) },
            )
            if (extra.isNotEmpty()) {
                Text(
                    extra.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}

/** «осталось · 19 дней» / «перерасход»; a month that is not the current one has no days. */
@Composable
private fun stateLine(head: Head): String {
    val state = stringResource(if (head.over) R.string.budget_detail_over else R.string.budget_detail_left)
    return head.daysLeft?.let { "$state · " + pluralStringResource(R.plurals.budget_detail_days, it, it) } ?: state
}

@Composable
private fun spentLine(head: Head): String {
    val spent = stringResource(R.string.budget_detail_spent_of, head.envelope.spent, head.envelope.limit)
    return head.paceToday?.let { spent + " · " + stringResource(R.string.budget_detail_pace, it) } ?: spent
}
