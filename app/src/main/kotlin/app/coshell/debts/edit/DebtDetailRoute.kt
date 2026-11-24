package app.coshell.debts.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.gate.LocalInputLock

/** Connects [DebtDetailScreen] to its ViewModel; the sheet closes when the debt is deleted (here or on the other phone). */
@Composable
fun DebtDetailRoute(onDone: () -> Unit, viewModel: DebtDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    LaunchedEffect(state.loaded, state.found) { if (state.loaded && !state.found) onDone() }
    DebtDetailScreen(
        state,
        DebtDetailActions(
            onReturnAccount = viewModel::onReturnAccount,
            onReturnAmount = viewModel::onReturnAmount,
            onSaveReturn = lock.guard(viewModel::saveReturn),
            onResetReturn = viewModel::resetReturn,
            onClose = lock.guard(viewModel::close),
            onDelete = lock.guard { viewModel.delete(onDone) },
        ),
    )
}
