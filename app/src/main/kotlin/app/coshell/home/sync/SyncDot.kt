package app.coshell.home.sync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Sizes

/** What the quiet sync mark beside the «Обзор» title says (#193, boards `Idea6A`, `Idea6A2`). */
sealed interface SyncDotState {
    /** Everything is sent. */
    data object Synced : SyncDotState

    /** [count] records wait to be sent. */
    data class Pending(val count: Int) : SyncDotState
}

/** The mark is for people in a family only: alone, there is nothing to sync and nothing to say. */
object SyncDotRules {
    fun of(inFamily: Boolean, pending: Int): SyncDotState? = when {
        !inFamily -> null
        pending > 0 -> SyncDotState.Pending(pending)
        else -> SyncDotState.Synced
    }
}

/** «● Синхронно» / «● Ждут: 3» as a status label; a tap opens «Семья». Touch target 48 dp, the label itself stays small. */
@Composable
fun SyncDot(state: SyncDotState, onClick: () -> Unit) {
    Box(
        Modifier.height(Sizes.button).widthIn(min = Sizes.button).tactileClickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            SyncDotState.Synced -> CoShellStatusChip(stringResource(R.string.sync_dot_ok), StatusTone.Done)
            is SyncDotState.Pending -> CoShellStatusChip(stringResource(R.string.sync_dot_pending, state.count), StatusTone.Work)
        }
    }
}
