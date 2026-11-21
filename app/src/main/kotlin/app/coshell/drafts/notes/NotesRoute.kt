package app.coshell.drafts.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.gate.LocalInputLock

/** Connects [NotesScreen] to [NotesViewModel]; [onDone] — the note was processed, show «Черновики». */
@Composable
fun NotesRoute(onBack: () -> Unit, onDone: () -> Unit, viewModel: NotesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    LaunchedEffect(viewModel) { viewModel.done.collect { onDone() } }
    NotesScreen(
        state = state,
        actions = NotesActions(onBack = onBack, onTextChange = viewModel::onTextChange, onProcess = lock.guard { viewModel.process() }),
    )
}
