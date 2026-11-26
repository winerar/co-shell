package app.coshell.goals.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.gate.LocalInputLock

/** Connects [GoalEditScreen] to its ViewModel; closes the sheet after a save or delete. [onTopUp] — account id and minor units. */
@Composable
fun GoalEditRoute(onDone: () -> Unit, onTopUp: (String, Long) -> Unit, viewModel: GoalEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    GoalEditScreen(
        state,
        GoalEditActions(
            onName = viewModel::onName,
            onAmount = viewModel::onAmount,
            onAccount = viewModel::onAccount,
            onDeadline = viewModel::onDeadline,
            onYearly = viewModel::onYearly,
            onSave = lock.guard(viewModel::save),
            onDelete = lock.guard(viewModel::delete),
            onTopUp = lock.guard { state.accountId?.let { onTopUp(it, state.topUpMinor) } },
        ),
    )
}
