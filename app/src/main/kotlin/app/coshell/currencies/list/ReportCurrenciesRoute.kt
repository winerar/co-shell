package app.coshell.currencies.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [ReportCurrenciesScreen] to [ReportCurrenciesViewModel]. */
@Composable
fun ReportCurrenciesRoute(onBack: () -> Unit, viewModel: ReportCurrenciesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportCurrenciesScreen(
        state = state,
        actions = ReportCurrenciesActions(
            onBack = onBack,
            onMakeMain = viewModel::makeMain,
            onRemove = viewModel::remove,
            onAdd = viewModel::openAdd,
            onCodeChange = viewModel::onCodeChange,
            onConfirmAdd = viewModel::confirmAdd,
            onDismissAdd = viewModel::dismissAdd,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class ReportCurrenciesActions(
    val onBack: () -> Unit = {},
    val onMakeMain: (String) -> Unit = {},
    val onRemove: (String) -> Unit = {},
    val onAdd: () -> Unit = {},
    val onCodeChange: (String) -> Unit = {},
    val onConfirmAdd: () -> Unit = {},
    val onDismissAdd: () -> Unit = {},
)
