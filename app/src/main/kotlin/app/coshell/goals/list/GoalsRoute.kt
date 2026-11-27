package app.coshell.goals.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.gate.LocalInputLock

/** Connects [GoalsScreen] to [GoalsViewModel]; the goal sheet opens through navigation ([onAdd], [onOpen]). */
@Composable
fun GoalsRoute(onBack: (() -> Unit)?, onAdd: () -> Unit, onOpen: (String) -> Unit, viewModel: GoalsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    GoalsScreen(state, GoalsActions(onBack = onBack, onAdd = lock.guard(onAdd), onOpen = onOpen, onToggleReached = viewModel::toggleReached))
}
