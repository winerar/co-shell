package app.coshell.importer.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.backup.DocumentStorage
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.importer.ImportBatch
import app.coshell.core.domain.importer.ImportRepository
import app.coshell.core.domain.importer.wallet.AccountChoice
import app.coshell.core.domain.importer.wallet.CategoryChoice
import app.coshell.core.domain.importer.wallet.UnpairedChoice
import app.coshell.core.domain.importer.wallet.WalletCategoryKey
import app.coshell.core.domain.importer.wallet.WalletCsvParser
import app.coshell.core.domain.importer.wallet.WalletImportUseCase
import app.coshell.core.model.MoneyFormat
import app.coshell.importer.PreImportBackup
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone

/**
 * «Ещё → Импорт из Wallet» (ROADMAP 5c): reads the CSV picked in [WalletImportRoute], keeps the
 * user's choices in a [WalletImportDraft], previews and saves through [WalletImportUseCase]
 * after a safety copy, and undoes whole batches.
 */
@HiltViewModel
class WalletImportViewModel @Inject constructor(
    private val storage: DocumentStorage,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val imports: ImportRepository,
    private val walletImport: WalletImportUseCase,
    private val preImportBackup: PreImportBackup,
    val timeZone: TimeZone,
    val moneyFormat: MoneyFormat,
) : ViewModel() {

    private val _state = MutableStateFlow(WalletImportUiState())
    val state: StateFlow<WalletImportUiState> = _state.asStateFlow()

    /** Where parsing and planning run; tests swap it for the test dispatcher. */
    internal var work: CoroutineContext = Dispatchers.Default

    init {
        viewModelScope.launch { imports.observeBatches().collect { list -> _state.update { it.copy(batches = list) } } }
    }

    fun onFilePicked(uri: String) = runBusy {
        val text = try {
            storage.readText(uri)
        } catch (e: IOException) {
            return@runBusy fail(WalletImportError.READ_FAILED)
        }
        val parsed = withContext(work) { WalletCsvParser.parse(text) }
        if (parsed.records.isEmpty()) return@runBusy fail(WalletImportError.NOT_WALLET)
        val liveAccounts = accounts.observeAccounts().first()
        val liveCategories = categories.observeCategories().first()
        val draft = withContext(work) { WalletImportDraft.start(parsed.records, liveAccounts, liveCategories) }
        _state.update {
            it.copy(
                step = WalletImportStep.SUMMARY, draft = draft, parseErrors = parsed.errors, plan = null,
                existingAccounts = liveAccounts, existingCategories = liveCategories, savedBatchId = null, undone = null,
            )
        }
    }

    /** «Далее»: the next step; on the preview it imports. */
    fun next() {
        val s = _state.value
        if (!s.canGoNext) return
        when (val target = s.steps.getOrNull(s.steps.indexOf(s.step) + 1)) {
            WalletImportStep.PREVIEW -> buildPreview()
            WalletImportStep.DONE -> save()
            null -> Unit
            else -> _state.update { it.copy(step = target, error = null) }
        }
    }

    /** System back / arrow: the previous step. `false` on the first step and after saving — leave the screen. */
    fun back(): Boolean {
        val s = _state.value
        if (s.isBusy) return true
        if (s.step == WalletImportStep.START || s.step == WalletImportStep.DONE) return false
        val previous = s.steps.getOrNull(s.steps.indexOf(s.step) - 1) ?: return false
        _state.update { it.copy(step = previous, error = null, plan = null) }
        return true
    }

    fun chooseAccount(walletName: String, choice: AccountChoice) = editDraft { it.chooseAccount(walletName, choice) }
    fun typeBalance(walletName: String, text: String) = editDraft { it.typeBalance(walletName, text) }
    fun chooseCategory(key: WalletCategoryKey, choice: CategoryChoice) = editDraft { it.chooseCategory(key, choice) }
    fun chooseUnpaired(line: Int, choice: UnpairedChoice) = editDraft { it.chooseUnpaired(line, choice) }

    fun requestUndo(batch: ImportBatch) = _state.update { it.copy(pendingUndo = batch) }
    fun cancelUndo() = _state.update { it.copy(pendingUndo = null) }

    /** Undo the batch asked about ([requestUndo]) — or the one just saved, from the last step. */
    fun confirmUndo() {
        val s = _state.value
        val batchId = s.pendingUndo?.id ?: s.savedBatchId ?: return
        _state.update { it.copy(pendingUndo = null) }
        runBusy {
            val removed = imports.undoBatch(batchId)
            _state.update {
                if (batchId == it.savedBatchId) WalletImportUiState(batches = it.batches, undone = removed) else it.copy(undone = removed)
            }
        }
    }

    private fun buildPreview() = runBusy {
        val draft = _state.value.draft ?: return@runBusy
        val plan = withContext(work) { walletImport.preview(draft.records, draft.choices(timeZone)) }
        _state.update { it.copy(step = WalletImportStep.PREVIEW, plan = plan) }
    }

    private fun save() = runBusy {
        val plan = _state.value.plan ?: return@runBusy
        if (!preImportBackup.save()) return@runBusy fail(WalletImportError.BACKUP_FAILED)
        try {
            walletImport.save(plan)
        } catch (e: RuntimeException) {
            if (e is CancellationException) throw e
            return@runBusy fail(WalletImportError.SAVE_FAILED) // a database error: the transaction rolled back
        }
        _state.update { it.copy(step = WalletImportStep.DONE, savedBatchId = plan.batchId) }
    }

    private fun editDraft(change: (WalletImportDraft) -> WalletImportDraft) =
        _state.update { s -> s.draft?.let { s.copy(draft = change(it)) } ?: s }

    private fun fail(error: WalletImportError) = _state.update { it.copy(error = error) }

    private fun runBusy(action: suspend () -> Unit) {
        if (_state.value.isBusy) return
        _state.update { it.copy(isBusy = true, error = null, undone = null) }
        viewModelScope.launch {
            try {
                action()
            } finally {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }
}
