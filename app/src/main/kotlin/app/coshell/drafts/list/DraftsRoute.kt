package app.coshell.drafts.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.gate.LocalInputLock

/** Connects [DraftsScreen] to [DraftsViewModel]. */
@Composable
fun DraftsRoute(onBack: () -> Unit, onEdit: (String) -> Unit, onNotes: () -> Unit, viewModel: DraftsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    DraftsScreen(
        state = state,
        actions = DraftsActions(
            onBack = onBack,
            onEdit = lock.guard1(onEdit),
            onToggle = viewModel::onToggle,
            onPost = lock.guard { viewModel.post() },
            onNotes = onNotes,
        ),
    )
}
