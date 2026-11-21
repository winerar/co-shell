package app.coshell.drafts.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellEmptyState
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.LocalBottomBarInset
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** What the screen can ask for; defaults keep previews short. */
data class DraftsActions(
    val onBack: () -> Unit = {},
    /** A row: the entry sheet filled from the draft. */
    val onEdit: (String) -> Unit = {},
    val onToggle: (String) -> Unit = {},
    val onPost: () -> Unit = {},
    /** The empty screen's «Быстрые заметки». */
    val onNotes: () -> Unit = {},
)

/**
 * «Черновики» (#586, boards `Notes586Drafts`, `Notes586Unready`, `Notes586Long`, `Notes586Empty2`): the list with the ticks,
 * «Записать · N» for the ticked ones at the bottom; empty — a phrase and a way to the notes.
 */
@Composable
fun DraftsScreen(state: DraftsUiState, actions: DraftsActions) {
    Scaffold(
        topBar = {
            val title = if (state.rows.isEmpty()) stringResource(R.string.drafts_title) else stringResource(R.string.drafts_title_count, state.rows.size)
            ScreenTopBar(title, onBack = actions.onBack)
        },
        bottomBar = {
            if (state.rows.isNotEmpty()) {
                val label = if (state.postCount > 0) stringResource(R.string.drafts_post, state.postCount) else stringResource(R.string.drafts_post_none)
                CoShellButton(
                    onClick = actions.onPost,
                    enabled = state.postCount > 0,
                    modifier = Modifier.fillMaxWidth().padding(bottom = LocalBottomBarInset.current).padding(horizontal = Spacing.l, vertical = Spacing.m),
                ) { Text(label) }
            }
        },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        if (state.isEmpty) {
            CoShellEmptyState(
                iconRes = DesignR.drawable.ic_draft,
                text = stringResource(R.string.drafts_empty),
                modifier = Modifier.padding(underBars(padding)),
                actionLabel = stringResource(R.string.drafts_empty_action),
                onAction = actions.onNotes,
            )
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = underBars(padding, PaddingValues(top = Spacing.s, bottom = Spacing.l))) {
            item(key = "summary", contentType = "hint") {
                CoShellHint(
                    stringResource(R.string.drafts_summary, state.rows.size, state.readyCount),
                    Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.xs),
                )
            }
            item(key = "list", contentType = "card") {
                CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l)) {
                    state.rows.forEachIndexed { index, row ->
                        androidx.compose.runtime.key(row.id) {
                            if (index > 0) CoShellDivider()
                            DraftRow(row, state.today, onClick = { actions.onEdit(row.id) }, onToggle = { actions.onToggle(row.id) })
                        }
                    }
                }
            }
            item(key = "hint", contentType = "hint") {
                CoShellHint(
                    stringResource(if (state.readyCount == state.rows.size) R.string.drafts_hint_ready else R.string.drafts_hint_unready),
                    Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.s),
                )
            }
        }
    }
}
