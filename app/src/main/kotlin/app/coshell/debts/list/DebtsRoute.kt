package app.coshell.debts.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.gate.LocalInputLock

/** Connects [DebtsScreen] to [DebtsViewModel]; the sheets open through navigation ([onAdd], [onOpen]). */
@Composable
fun DebtsRoute(onBack: (() -> Unit)?, onAdd: () -> Unit, onOpen: (String) -> Unit, viewModel: DebtsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    DebtsScreen(
        state,
        DebtsActions(
            onBack = onBack,
            onTab = viewModel::selectTab,
            onAdd = lock.guard(onAdd),
            onOpen = onOpen,
            onToggleClosed = viewModel::toggleClosed,
        ),
    )
}
