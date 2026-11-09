package app.coshell.transactions.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.designsystem.motion.LocalAddConfirm
import app.coshell.core.designsystem.components.keypad.KeypadKey
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
    val haptics = LocalHapticFeedback.current
    val addConfirm = LocalAddConfirm.current
    var saving by remember { mutableStateOf(false) }
    LaunchedEffect(state.isDone) {
        if (!state.isDone) return@LaunchedEffect
        // A light tick confirms the saved operation (design-questions §6); delete has its snackbar.
        // The «+» answers with a check mark (design-refactor Step 4).
        if (saving) {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            addConfirm.fire()
        }
        onDone()
    }
    TransactionEditScreen(
        state = state,
        actions = TransactionEditActions(
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
            onSave = {
                saving = true
                viewModel.save()
            },
            onDelete = viewModel::delete,
            onStepChange = viewModel::onStepChange,
            onKeypadTarget = viewModel::onKeypadTarget,
            onKey = { key ->
                if (key == KeypadKey.Done) saving = true
                viewModel.onKey(key)
            },
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class TransactionEditActions(
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
    val onStepChange: (TransactionEditUiState.EntryStep) -> Unit = {},
    val onKeypadTarget: (TransactionEditUiState.KeypadTarget) -> Unit = {},
    val onKey: (KeypadKey) -> Unit = {},
)
