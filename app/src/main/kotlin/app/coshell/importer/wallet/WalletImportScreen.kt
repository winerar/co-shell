package app.coshell.importer.wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellLinearProgress
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.importer.wallet.WalletCategoryKey
import app.coshell.core.domain.importer.wallet.WalletRecord
import app.coshell.core.model.MoneyFormat
import app.coshell.importer.wallet.components.CategoryPickDialog
import app.coshell.importer.wallet.components.UnpairedAccountDialog
import app.coshell.importer.wallet.components.accountsStep
import app.coshell.importer.wallet.components.categoriesStep
import app.coshell.importer.wallet.components.doneStep
import app.coshell.importer.wallet.components.previewStep
import app.coshell.importer.wallet.components.startStep
import app.coshell.importer.wallet.components.summaryStep
import app.coshell.importer.wallet.components.transfersStep
import app.coshell.ui.form.ConfirmDialog
import app.coshell.ui.top.ScreenTopBar
import kotlinx.datetime.TimeZone

/** «Импорт из Wallet»: the current step as a list, «Далее»/«Импортировать» at the bottom, pick dialogs. */
@Composable
fun WalletImportScreen(state: WalletImportUiState, actions: WalletImportActions, moneyFormat: MoneyFormat, timeZone: TimeZone) {
    var pickCategory by remember { mutableStateOf<WalletCategoryKey?>(null) }
    var pickTransfer by remember { mutableStateOf<WalletRecord?>(null) }
    Scaffold(
        topBar = {
            ScreenTopBar(
                stringResource(R.string.import_title),
                onBack = actions.onBack,
                subtitle = "${state.step.title()} · " +
                    stringResource(R.string.import_step_of, state.steps.indexOf(state.step) + 1, state.steps.size),
            )
        },
        bottomBar = { BottomButton(state, actions) },
    ) { padding ->
        val draft = state.draft
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = Spacing.xl),
        ) {
            if (state.isBusy) item(key = "busy") { CoShellLinearProgress(Modifier.fillMaxWidth().padding(horizontal = Spacing.l)) }
            state.error?.let { error ->
                item(key = "error") { CoShellMessage(error.text(), isError = true, Modifier.padding(Spacing.l)) }
            }
            state.undone?.let { n ->
                item(key = "undone") { CoShellMessage(stringResource(R.string.import_undone, n), isError = false, Modifier.padding(Spacing.l)) }
            }
            when (state.step) {
                WalletImportStep.START -> startStep(state.batches, timeZone, actions.onPickFile, actions.onRequestUndo)
                WalletImportStep.SUMMARY -> draft?.let { summaryStep(it.summary, state.parseErrors) }
                WalletImportStep.ACCOUNTS -> draft?.let { accountsStep(it, state.existingAccounts, actions.onChooseAccount, actions.onTypeBalance) }
                WalletImportStep.CATEGORIES -> draft?.let { categoriesStep(it, state.existingCategories) { key -> pickCategory = key } }
                WalletImportStep.TRANSFERS -> draft?.let {
                    transfersStep(it, state.existingAccounts, moneyFormat, actions.onChooseUnpaired) { record -> pickTransfer = record }
                }
                WalletImportStep.PREVIEW -> state.plan?.let { previewStep(it, moneyFormat) }
                WalletImportStep.DONE -> doneStep(state.plan?.newCount ?: 0, actions.onUndoSaved)
            }
        }
    }

    pickCategory?.let { key ->
        CategoryPickDialog(
            key = key,
            selected = state.draft?.categoryChoices?.get(key),
            existing = state.existingCategories,
            onPick = { actions.onChooseCategory(key, it); pickCategory = null },
            onDismiss = { pickCategory = null },
        )
    }
    pickTransfer?.let { record ->
        UnpairedAccountDialog(
            selected = state.draft?.unpairedChoices?.get(record.line),
            existing = state.existingAccounts,
            onPick = { actions.onChooseUnpaired(record.line, it); pickTransfer = null },
            onDismiss = { pickTransfer = null },
        )
    }
    if (state.pendingUndo != null) {
        ConfirmDialog(
            title = R.string.import_undo_confirm_title,
            text = R.string.import_undo_confirm_text,
            confirmLabel = R.string.import_undo,
            onConfirm = actions.onConfirmUndo,
            onDismiss = actions.onCancelUndo,
            destructive = true,
        )
    }
}

/** «Далее» on the middle steps, «Импортировать» on the preview, «Готово» (leave) after saving. */
@Composable
private fun BottomButton(state: WalletImportUiState, actions: WalletImportActions) {
    if (state.step == WalletImportStep.START) return
    val done = state.step == WalletImportStep.DONE
    CoShellButton(
        onClick = if (done) actions.onClose else actions.onNext,
        enabled = done || state.canGoNext,
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(Spacing.l),
    ) {
        Text(
            stringResource(
                when (state.step) {
                    WalletImportStep.PREVIEW -> R.string.import_do
                    WalletImportStep.DONE -> R.string.import_done_close
                    else -> R.string.import_next
                },
            ),
        )
    }
}
