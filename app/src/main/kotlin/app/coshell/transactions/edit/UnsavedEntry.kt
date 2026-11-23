package app.coshell.transactions.edit

import app.coshell.core.domain.quickentry.EntryDraft
import app.coshell.core.domain.quickentry.EntryDrafts
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import kotlin.time.Instant

/**
 * «Не сохранено» (#323, board `EwDraftA`): what the plate on top of a new entry sheet says about the draft of the last closed one —
 * the amount and the category (or the note) typed, and how long ago ([minutes]).
 */
data class UnsavedUi(val amount: String?, val label: String?, val minutes: Int)

/** The draft of the form as it is now; `null` when there is nothing worth keeping (only an amount or a note count) or the form is not a new operation. */
internal fun draftOf(form: TransactionEditUiState, accounts: Map<String, Account>, now: Instant): EntryDraft? {
    if (!form.isNew || form.isLoading || form.isDone) return null
    val currency = form.accountId?.let { accounts[it]?.currency }
    val amount = currency?.let { parsePositiveAmount(form.amount, it).money }
    if (!EntryDrafts.worthKeeping(amount, form.note)) return null
    return EntryDraft(form.type, form.accountId, amount, form.categoryId, form.note, form.tagIds, now)
}

/** The form with the draft put back: only what still exists (an account or a category deleted since stays as it was). */
internal fun TransactionEditUiState.withDraft(
    draft: EntryDraft,
    accounts: Map<String, Account>,
    categories: List<Category>,
    moneyFormat: MoneyFormat,
): TransactionEditUiState = copy(
    type = draft.type,
    accountId = draft.accountId?.takeIf { it in accounts } ?: accountId,
    amount = draft.amount?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
    categoryId = draft.categoryId?.takeIf { id -> categories.any { it.id == id } },
    note = draft.note,
    tagIds = draft.tagIds,
    amountError = null,
    unsaved = null,
)

internal fun unsavedUi(draft: EntryDraft, now: Instant, categories: List<Category>, moneyFormat: MoneyFormat) = UnsavedUi(
    amount = draft.amount?.let { moneyFormat.format(it) },
    label = categories.firstOrNull { it.id == draft.categoryId }?.name ?: draft.note.takeIf { it.isNotBlank() },
    minutes = ((now - draft.savedAt).inWholeMinutes).toInt().coerceAtLeast(0),
)
