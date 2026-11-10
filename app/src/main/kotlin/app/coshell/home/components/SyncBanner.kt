package app.coshell.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.data.sync.SyncIssueKind
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.sync.SyncBannerState

/** What the banner's button does; which one shows depends on the problem. */
data class SyncBannerActions(
    val onRetry: () -> Unit = {},
    val onSignIn: () -> Unit = {},
    val onFeedback: (prefill: String) -> Unit = {},
    val onDismiss: () -> Unit = {},
)

/**
 * Sync keeps failing (board 17А, feedback #102): a card washed with the warning colour over the
 * total — why, what happens to the edits, when it last worked, and one button to fix it.
 */
@Composable
fun SyncBanner(state: SyncBannerState, actions: SyncBannerActions, modifier: Modifier = Modifier) {
    val warning = LocalStatusPalette.current.warning
    val prefill = stringResource(R.string.sync_banner_feedback_prefill, state.code.orEmpty())
    val (title, text) = texts(state)
    val pending = if (state.pending > 0 && state.kind != SyncIssueKind.QUOTA) {
        pluralStringResource(R.plurals.sync_banner_pending, state.pending, state.pending)
    } else {
        null
    }
    CoShellCard(modifier = modifier.fillMaxWidth(), tint = warning) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Icon(painterResource(DesignR.drawable.ic_sync), contentDescription = null, tint = warning, modifier = Modifier.size(Sizes.tileIcon))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = warning)
                    Text(listOfNotNull(pending, text).joinToString(" "), style = MaterialTheme.typography.bodyMedium)
                    state.lastSync?.let {
                        Text(
                            stringResource(R.string.sync_banner_last, it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                CoShellButton(
                    onClick = when (state.kind) {
                        SyncIssueKind.OFFLINE, SyncIssueKind.SERVER -> actions.onRetry
                        SyncIssueKind.SIGNED_OUT -> actions.onSignIn
                        SyncIssueKind.NO_ACCESS -> { { actions.onFeedback(prefill) } }
                        SyncIssueKind.QUOTA -> actions.onDismiss
                    },
                    enabled = !state.isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = warning, contentColor = MaterialTheme.colorScheme.surface),
                ) {
                    Text(stringResource(buttonLabel(state.kind)))
                }
            }
        }
    }
}

@Composable
private fun texts(state: SyncBannerState): Pair<String, String> = when (state.kind) {
    SyncIssueKind.OFFLINE -> stringResource(R.string.sync_banner_offline_title) to stringResource(R.string.sync_banner_offline_text)
    SyncIssueKind.SIGNED_OUT -> stringResource(R.string.sync_banner_signed_out_title) to stringResource(R.string.sync_banner_signed_out_text)
    SyncIssueKind.NO_ACCESS ->
        stringResource(R.string.sync_banner_no_access_title) to stringResource(R.string.sync_banner_no_access_text, state.code.orEmpty())
    SyncIssueKind.SERVER -> stringResource(R.string.sync_banner_server_title) to stringResource(R.string.sync_banner_server_text)
    SyncIssueKind.QUOTA ->
        stringResource(R.string.sync_banner_quota_title) to stringResource(R.string.sync_banner_quota_text, state.resumeAt.orEmpty())
}

private fun buttonLabel(kind: SyncIssueKind): Int = when (kind) {
    SyncIssueKind.OFFLINE, SyncIssueKind.SERVER -> R.string.sync_banner_retry
    SyncIssueKind.SIGNED_OUT -> R.string.sync_banner_sign_in
    SyncIssueKind.NO_ACCESS -> R.string.sync_banner_feedback
    SyncIssueKind.QUOTA -> R.string.sync_banner_ok
}
