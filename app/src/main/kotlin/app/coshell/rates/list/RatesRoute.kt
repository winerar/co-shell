package app.coshell.rates.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.designsystem.components.keypad.KeypadKey
import kotlinx.datetime.LocalDate

/** Connects [RatesScreen] to [RatesViewModel]. */
@Composable
fun RatesRoute(onBack: () -> Unit, viewModel: RatesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RatesScreen(
        state = state,
        actions = RatesActions(
            onBack = onBack,
            onRefresh = viewModel::refreshNow,
            onCurrentClick = viewModel::setManual,
            onAddManual = viewModel::addManual,
            onManualClick = viewModel::editManual,
            onManualDelete = viewModel::askDelete,
            onConfirmDelete = viewModel::confirmDelete,
            onCancelDelete = viewModel::cancelDelete,
            onConverterKey = viewModel::onConverterKey,
            onConverterFrom = viewModel::onConverterFrom,
        ),
        editorActions = ManualRateActions(
            onFromChange = viewModel::onFromChange,
            onToChange = viewModel::onToChange,
            onRateChange = viewModel::onRateChange,
            onDateSelect = viewModel::onDateSelect,
            onDatePicked = viewModel::onDatePicked,
            onSave = viewModel::saveManual,
            onDismiss = viewModel::dismissEditor,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class RatesActions(
    val onBack: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onCurrentClick: (CurrentRateItem) -> Unit = {},
    val onAddManual: () -> Unit = {},
    val onManualClick: (ManualRateItem) -> Unit = {},
    val onManualDelete: (ManualRateItem) -> Unit = {},
    val onConfirmDelete: () -> Unit = {},
    val onCancelDelete: () -> Unit = {},
    /** The converter (#319): a key of its keypad, another source currency. */
    val onConverterKey: (KeypadKey) -> Unit = {},
    val onConverterFrom: (String) -> Unit = {},
)

/** Callbacks of the "manual rate" dialog. */
data class ManualRateActions(
    val onFromChange: (String) -> Unit = {},
    val onToChange: (String) -> Unit = {},
    val onRateChange: (String) -> Unit = {},
    val onDateSelect: (LocalDate) -> Unit = {},
    val onDatePicked: (Long) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
