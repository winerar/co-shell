package app.coshell.ai.cloud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [CloudAiScreen] to its ViewModel. */
@Composable
fun CloudAiRoute(onBack: () -> Unit, viewModel: CloudAiViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CloudAiScreen(state, CloudAiActions(onBack, viewModel::onToggle, viewModel::onConfirm, viewModel::onDismiss))
}
