package app.coshell.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [AiAddonScreen] to its ViewModel. */
@Composable
fun AiAddonRoute(onBack: () -> Unit, viewModel: AiAddonViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AiAddonScreen(
        state,
        AiAddonActions(
            onBack = onBack,
            onEnable = viewModel::onEnable,
            onConfirmDownload = viewModel::onConfirmDownload,
            onAskDismiss = viewModel::onAskDismiss,
            onCancel = viewModel::onCancel,
            onDeleteModel = viewModel::onDeleteModel,
        ),
    )
}
