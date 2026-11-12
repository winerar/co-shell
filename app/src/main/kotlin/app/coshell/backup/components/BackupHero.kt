package app.coshell.backup.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.backup.auto.AutoBackupPolicy
import app.coshell.backup.auto.AutoBackupUiState
import app.coshell.core.designsystem.components.HeroRing
import app.coshell.core.designsystem.components.ProgressHero
import app.coshell.core.designsystem.theme.AmountHeroStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * Top of «Резервная копия» (board Backup): a ring counting days to the next weekly copy, the state under it
 * («Автокопия включена» · «Последняя: 3 ноября · раз в неделю»). Off — a quiet ring; failed — a red cross.
 */
@Composable
internal fun BackupHero(state: AutoBackupUiState, modifier: Modifier = Modifier) {
    val days = state.daysLeft?.takeIf { !state.lastFailed }
    Column(
        modifier.fillMaxWidth().padding(horizontal = Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        ProgressHero(
            ring = days?.let { HeroRing.Progress(AutoBackupPolicy.ringFraction(it)) } ?: HeroRing.Idle,
            state = days,
            contentKey = { it == null },
            modifier = Modifier.padding(bottom = Spacing.m),
        ) { shown ->
            if (shown != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$shown", style = AmountHeroStyle)
                    Text(
                        pluralStringResource(R.plurals.auto_backup_days_left, shown),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Icon(
                    painterResource(if (state.lastFailed) DesignR.drawable.ic_close else DesignR.drawable.ic_settings_backup_restore),
                    contentDescription = null,
                    tint = if (state.lastFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Sizes.heroIcon),
                )
            }
        }
        Text(stringResource(heroTitle(state)), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            heroSubtitle(state),
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.lastFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun heroTitle(state: AutoBackupUiState) = when {
    !state.enabled -> R.string.auto_backup_off_title
    state.lastFailed -> R.string.auto_backup_failed_title
    else -> R.string.auto_backup_on
}

@Composable
private fun heroSubtitle(state: AutoBackupUiState) = when {
    !state.enabled -> stringResource(R.string.auto_backup_off)
    state.lastFailed -> stringResource(R.string.auto_backup_failed)
    state.lastBackup != null -> stringResource(R.string.auto_backup_last, state.lastBackup)
    else -> stringResource(R.string.auto_backup_pending)
}
