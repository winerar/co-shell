package app.coshell.importer.wallet

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.domain.importer.ImportBatch
import app.coshell.core.domain.importer.wallet.AccountChoice
import app.coshell.core.domain.importer.wallet.CategoryChoice
import app.coshell.core.domain.importer.wallet.UnpairedChoice
import app.coshell.core.domain.importer.wallet.WalletCategoryKey
import app.coshell.core.model.MoneyFormat

/** Connects [WalletImportScreen] to its ViewModel, the system «open file» dialog and the back gesture. */
@Composable
fun WalletImportRoute(onBack: () -> Unit, viewModel: WalletImportViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val openFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri -> viewModel.onFilePicked(uri.toString()) }
    }
    val back = { if (!viewModel.back()) onBack() }
    BackHandler(onBack = back)
    WalletImportScreen(
        state = state,
        moneyFormat = viewModel.moneyFormat,
        timeZone = viewModel.timeZone,
        actions = WalletImportActions(
            onBack = back,
            onClose = onBack,
            onPickFile = { openFile.launch(CSV_TYPES) },
            onNext = viewModel::next,
            onChooseAccount = viewModel::chooseAccount,
            onTypeBalance = viewModel::typeBalance,
            onChooseCategory = viewModel::chooseCategory,
            onChooseUnpaired = viewModel::chooseUnpaired,
            onRequestUndo = viewModel::requestUndo,
            onConfirmUndo = viewModel::confirmUndo,
            onCancelUndo = viewModel::cancelUndo,
            onUndoSaved = viewModel::confirmUndo,
        ),
    )
}

/** Wallet's export is `text/csv`, but file managers label CSVs in many ways. */
private val CSV_TYPES = arrayOf("text/csv", "text/comma-separated-values", "text/plain", "application/octet-stream", "*/*")

/** Everything the screen can ask for; defaults make previews short. */
data class WalletImportActions(
    val onBack: () -> Unit = {},
    val onClose: () -> Unit = {},
    val onPickFile: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onChooseAccount: (String, AccountChoice) -> Unit = { _, _ -> },
    val onTypeBalance: (String, String) -> Unit = { _, _ -> },
    val onChooseCategory: (WalletCategoryKey, CategoryChoice) -> Unit = { _, _ -> },
    val onChooseUnpaired: (Int, UnpairedChoice) -> Unit = { _, _ -> },
    val onRequestUndo: (ImportBatch) -> Unit = {},
    val onConfirmUndo: () -> Unit = {},
    val onCancelUndo: () -> Unit = {},
    val onUndoSaved: () -> Unit = {},
)
