package app.coshell.transactions.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate

/** Connects [TransactionEditScreen] to its ViewModel. */
@Composable
fun TransactionEditRoute(
    onDone: () -> Unit,
    onAddAccount: () -> Unit,
    viewModel: TransactionEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    TransactionEditScreen(
        state = state,
        actions = TransactionEditActions(
            onBack = onDone,
            onAddAccount = onAddAccount,
            onTypeChange = viewModel::onTypeChange,
            onAmountChange = viewModel::onAmountChange,
            onToAmountChange = viewModel::onToAmountChange,
            onAccountSelect = viewModel::onAccountSelect,
            onToAccountSelect = viewModel::onToAccountSelect,
            onCategorySelect = viewModel::onCategorySelect,
            onDateSelect = viewModel::onDateSelect,
            onDatePicked = viewModel::onDatePicked,
            onNoteChange = viewModel::onNoteChange,
            onReceiptToggle = viewModel::onReceiptToggle,
            onReceiptCurrencyChange = viewModel::onReceiptCurrencyChange,
            onReceiptAmountChange = viewModel::onReceiptAmountChange,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class TransactionEditActions(
    val onBack: () -> Unit = {},
    val onAddAccount: () -> Unit = {},
    val onTypeChange: (TransactionType) -> Unit = {},
    val onAmountChange: (String) -> Unit = {},
    val onToAmountChange: (String) -> Unit = {},
    val onAccountSelect: (String) -> Unit = {},
    val onToAccountSelect: (String) -> Unit = {},
    val onCategorySelect: (String) -> Unit = {},
    val onDateSelect: (LocalDate) -> Unit = {},
    val onDatePicked: (Long) -> Unit = {},
    val onNoteChange: (String) -> Unit = {},
    val onReceiptToggle: (Boolean) -> Unit = {},
    val onReceiptCurrencyChange: (String) -> Unit = {},
    val onReceiptAmountChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: () -> Unit = {},
)
