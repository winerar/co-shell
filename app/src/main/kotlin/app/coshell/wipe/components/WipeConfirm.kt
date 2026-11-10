package app.coshell.wipe.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellLinearProgress
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.DataCounts
import app.coshell.wipe.WipeDataUiState
import app.coshell.core.designsystem.R as DesignR

/** Step 1 of board 18: what goes (for the whole family), the copy first, the word, the red button. */
@Composable
internal fun WipeConfirm(state: WipeDataUiState, onWordChange: (String) -> Unit, onWipe: () -> Unit, modifier: Modifier = Modifier) {
    val danger = MaterialTheme.colorScheme.error
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CoShellCard(tint = danger) {
            Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(stringResource(R.string.wipe_family_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = danger)
                Text(stringResource(R.string.wipe_family_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.counts?.let { Counts(it, Modifier.padding(vertical = Spacing.s)) }
                Text(stringResource(R.string.wipe_kept), style = MaterialTheme.typography.bodySmall)
            }
        }
        CoShellCard {
            Row(Modifier.padding(Spacing.l), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Icon(painterResource(DesignR.drawable.ic_file_save), contentDescription = null, tint = LocalStatusPalette.current.success)
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(stringResource(R.string.wipe_backup_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.wipe_backup_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text(
            stringResource(R.string.wipe_word_prompt, WipeDataUiState.CONFIRM_WORD),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.s),
        )
        CoShellTextField(
            value = state.word,
            onValueChange = onWordChange,
            label = stringResource(R.string.wipe_word_label),
            placeholder = WipeDataUiState.CONFIRM_WORD,
            enabled = state.step == WipeDataUiState.Step.CONFIRM,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.backupFailed) CoShellMessage(stringResource(R.string.wipe_backup_failed), isError = true)
        if (state.step == WipeDataUiState.Step.WIPING) CoShellLinearProgress(Modifier.fillMaxWidth())
        CoShellButton(
            onClick = onWipe,
            enabled = state.canWipe,
            colors = ButtonDefaults.buttonColors(containerColor = danger, contentColor = MaterialTheme.colorScheme.onError),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.wipe_button))
        }
    }
}

@Composable
private fun Counts(counts: DataCounts, modifier: Modifier = Modifier) {
    val items = listOf(
        counts.accounts to R.string.wipe_count_accounts,
        counts.transactions to R.string.wipe_count_transactions,
        counts.categories to R.string.wipe_count_categories,
        counts.manualRates to R.string.wipe_count_rates,
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                row.forEach { (n, label) ->
                    Column(Modifier.weight(1f)) {
                        Text("%,d".format(n).replace(',', ' '), style = AmountTitleStyle)
                        Text(stringResource(label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
