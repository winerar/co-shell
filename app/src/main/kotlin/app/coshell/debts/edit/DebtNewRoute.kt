package app.coshell.debts.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.gate.LocalInputLock

/** Connects [DebtNewScreen] to its ViewModel; closes the sheet after a save. */
@Composable
fun DebtNewRoute(onDone: () -> Unit, viewModel: DebtNewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    DebtNewScreen(
        state,
        DebtNewActions(
            onDirection = viewModel::onDirection,
            onPerson = viewModel::onPerson,
            onAmount = viewModel::onAmount,
            onAccount = viewModel::onAccount,
            onDue = viewModel::onDue,
            onNote = viewModel::onNote,
            onSave = lock.guard(viewModel::save),
        ),
    )
}
