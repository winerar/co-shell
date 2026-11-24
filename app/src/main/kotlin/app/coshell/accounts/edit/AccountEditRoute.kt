package app.coshell.accounts.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.domain.hints.HintId
import app.coshell.core.model.AccountType
import app.coshell.core.model.DepositPayout
import app.coshell.gate.LocalInputLock
import app.coshell.hints.LocalHintHost

/** Connects [AccountEditScreen] to its ViewModel. */
@Composable
fun AccountEditRoute(onDone: () -> Unit, viewModel: AccountEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    val hints = LocalHintHost.current
    LaunchedEffect(state.isDone) {
        if (!state.isDone) return@LaunchedEffect
        // Offered before leaving: the effect would be cancelled once the sheet is gone.
        if (state.foreignCurrencyAdded) hints?.offer(mapOf(HintId.CURRENCY_ACCOUNT to R.string.hint_currency_account))
        onDone()
    }
    AccountEditScreen(
        state = state,
        actions = AccountEditActions(
            onNameChange = viewModel::onNameChange,
            onCurrencyChange = viewModel::onCurrencyChange,
            onTypeChange = viewModel::onTypeChange,
            onPrepaidSelect = viewModel::onPrepaidSelect,
            onCreditSelect = viewModel::onCreditSelect,
            onCreditLimitChange = viewModel::onCreditLimitChange,
            onCreditPayDayChange = viewModel::onCreditPayDayChange,
            onCreditStatementDayChange = viewModel::onCreditStatementDayChange,
            onDepositRateChange = viewModel::onDepositRateChange,
            onDepositEndChange = viewModel::onDepositEndChange,
            onDepositStartChange = viewModel::onDepositStartChange,
            onDepositPayoutChange = viewModel::onDepositPayoutChange,
            onCreditGraceDaysChange = viewModel::onCreditGraceDaysChange,
            onCreditMinSumChange = viewModel::onCreditMinSumChange,
            onCreditMinChange = viewModel::onCreditMinChange,
            onInitialBalanceChange = viewModel::onInitialBalanceChange,
            onActualBalanceChange = viewModel::onActualBalanceChange,
            onSaveBalance = lock.guard(viewModel::saveBalance),
            onDismissBalanceSaved = viewModel::dismissBalanceSaved,
            onIncludedInTotalChange = viewModel::onIncludedInTotalChange,
            onColorChange = viewModel::onColorChange,
            onCardDigitsChange = viewModel::onCardDigitsChange,
            onSharedChange = viewModel::onSharedChange,
            onLocalOnlyChange = viewModel::onLocalOnlyChange,
            onSave = lock.guard(viewModel::save),
            onToggleArchived = lock.guard(viewModel::toggleArchived),
            onDelete = lock.guard(viewModel::delete),
            onDismissDeleteBlocked = viewModel::dismissDeleteBlocked,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class AccountEditActions(
    val onNameChange: (String) -> Unit = {},
    val onCurrencyChange: (String) -> Unit = {},
    val onTypeChange: (AccountType) -> Unit = {},
    val onPrepaidSelect: () -> Unit = {},
    val onCreditSelect: () -> Unit = {},
    val onCreditLimitChange: (String) -> Unit = {},
    val onCreditPayDayChange: (String) -> Unit = {},
    val onCreditStatementDayChange: (String) -> Unit = {},
    val onDepositRateChange: (String) -> Unit = {},
    val onDepositEndChange: (String) -> Unit = {},
    val onDepositStartChange: (String) -> Unit = {},
    val onDepositPayoutChange: (DepositPayout) -> Unit = {},
    val onCreditGraceDaysChange: (String) -> Unit = {},
    val onCreditMinSumChange: (Boolean) -> Unit = {},
    val onCreditMinChange: (String) -> Unit = {},
    val onInitialBalanceChange: (String) -> Unit = {},
    val onActualBalanceChange: (String) -> Unit = {},
    val onSaveBalance: () -> Unit = {},
    val onDismissBalanceSaved: () -> Unit = {},
    val onIncludedInTotalChange: (Boolean) -> Unit = {},
    val onColorChange: (Long?) -> Unit = {},
    val onCardDigitsChange: (String) -> Unit = {},
    val onSharedChange: (Boolean) -> Unit = {},
    val onLocalOnlyChange: (Boolean) -> Unit = {},
    val onSave: () -> Unit = {},
    val onToggleArchived: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDismissDeleteBlocked: () -> Unit = {},
)
