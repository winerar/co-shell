package app.coshell.importer.wallet

import app.coshell.core.domain.importer.ImportBatch
import app.coshell.core.domain.importer.wallet.WalletImportPlan
import app.coshell.core.domain.importer.wallet.WalletParseError
import app.coshell.core.model.Account
import app.coshell.core.model.Category

/** Wizard steps in order (ROADMAP 5c). [TRANSFERS] is skipped when every transfer has a pair. */
enum class WalletImportStep { START, SUMMARY, ACCOUNTS, CATEGORIES, TRANSFERS, PREVIEW, DONE }

/** A failure shown as a message card on the current step. */
enum class WalletImportError {
    /** The file could not be read at all. */
    READ_FAILED,

    /** Read, but it is not a Wallet export (or has no rows). */
    NOT_WALLET,

    /** The safety copy before the import failed — nothing was written. */
    BACKUP_FAILED,
    SAVE_FAILED,
}

/**
 * @property draft the file and the choices; `null` until a file is read.
 * @property parseErrors rows that could not be read (they are left out of the import).
 * @property plan the preview, built on entering [WalletImportStep.PREVIEW].
 * @property batches earlier imports with live operations — each can be undone.
 * @property undone operations removed by the last «Отменить импорт», for the message.
 */
data class WalletImportUiState(
    val step: WalletImportStep = WalletImportStep.START,
    val isBusy: Boolean = false,
    val error: WalletImportError? = null,
    val draft: WalletImportDraft? = null,
    val parseErrors: List<WalletParseError> = emptyList(),
    val existingAccounts: List<Account> = emptyList(),
    val existingCategories: List<Category> = emptyList(),
    val plan: WalletImportPlan? = null,
    val savedBatchId: String? = null,
    val batches: List<ImportBatch> = emptyList(),
    val pendingUndo: ImportBatch? = null,
    val undone: Int? = null,
) {
    /** Steps the user walks through for this file: no transfer step without unpaired transfers. */
    val steps: List<WalletImportStep>
        get() = WalletImportStep.entries.filter { it != WalletImportStep.TRANSFERS || draft?.unpaired?.isNotEmpty() == true }

    /** «Далее» is allowed: a file is read and the current step has nothing invalid. */
    val canGoNext: Boolean
        get() = !isBusy && draft != null && when (step) {
            WalletImportStep.ACCOUNTS -> !draft.hasInvalidBalance
            WalletImportStep.PREVIEW -> plan != null && (plan.newCount > 0 || plan.write.accounts.isNotEmpty())
            WalletImportStep.DONE -> false
            else -> true
        }
}
