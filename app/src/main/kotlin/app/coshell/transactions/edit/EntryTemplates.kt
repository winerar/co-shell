package app.coshell.transactions.edit

import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.recurring.MarkPaymentsHandledUseCase
import app.coshell.core.domain.recurring.PaymentHistory
import app.coshell.core.domain.recurring.PaymentHistoryUseCase
import app.coshell.core.domain.templates.SaveAsTemplateUseCase
import app.coshell.core.domain.templates.SaveTemplateUseCase
import app.coshell.core.domain.templates.TemplateGap
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.domain.templates.TemplateUsage
import app.coshell.core.domain.templates.TransactionDraft
import app.coshell.core.domain.templates.draftFrom
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import javax.inject.Inject
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Templates for the entry sheet (#104): a template → draft against the accounts and categories the
 * sheet already loaded, the usage counter and «Сохранить как шаблон». Keeps [TransactionEditViewModel] thin.
 */
class EntryTemplates @Inject constructor(
    private val templates: TemplateRepository,
    private val usage: TemplateUsage,
    private val saveAs: SaveAsTemplateUseCase,
    transactions: TransactionRepository,
) {
    private val markHandled = MarkPaymentsHandledUseCase(templates)
    private val paymentHistory = PaymentHistoryUseCase(transactions)

    /** The draft and the template's name; `null` — the template is gone (deleted on the other phone). */
    suspend fun draft(
        templateId: String,
        now: Instant,
        accounts: Map<String, Account>,
        categories: List<Category>,
    ): Pair<TransactionDraft, String>? {
        val template = templates.getTemplate(templateId) ?: return null
        val draft = draftFrom(
            template,
            now = now,
            account = accounts[template.accountId],
            target = template.toAccountId?.let { accounts[it] },
            category = template.categoryId?.let { id -> categories.firstOrNull { it.id == id } },
        )
        return draft to template.name
    }

    /** An operation was saved from the template: it moves up in «Часто». */
    suspend fun used(templateId: String) = usage.record(templateId)

    /**
     * «Провести» saved the payment of [day] (#143), or «Пропустить этот раз» was tapped (#404): it and every
     * older one leave «Скоро».
     */
    suspend fun paid(templateId: String, day: LocalDate) {
        markHandled(templateId, day)
    }

    /** «В прошлый раз · в среднем» for the payment of [day] (#404); `null` — nothing paid before. */
    suspend fun history(templateId: String, day: LocalDate, timeZone: TimeZone): PaymentHistory? =
        templates.getTemplate(templateId)?.let { paymentHistory(it, day, timeZone) }

    /** `true` — saved; `false` — the operation's account or category can no longer be used. */
    suspend fun saveAs(transaction: Transaction, name: String, rememberAmount: Boolean): Boolean =
        saveAs.invoke(transaction, name, rememberAmount) == SaveTemplateUseCase.Result.Saved
}

/**
 * The form filled from a template: a new operation dated today. What the template could not fill
 * ([TransactionDraft.gaps]) decides where the sheet opens — on the account, the target account, or
 * the amount with «выберите категорию». A payment in another currency (#415) opens with the receipt filled
 * («Аренда · 650 USD»): «списано» follows by the rate ([withDebitSuggestion]) or waits to be typed.
 */
internal fun TransactionEditUiState.filledFrom(
    draft: TransactionDraft,
    templateName: String,
    moneyFormat: MoneyFormat,
): TransactionEditUiState = copy(
    isNew = true,
    isCopy = false,
    type = draft.type,
    amount = draft.amount?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
    amountSuggested = false,
    suggestedRateDate = null,
    toAmount = "",
    accountId = draft.accountId,
    toAccountId = draft.toAccountId,
    categoryId = draft.categoryId,
    date = today,
    note = draft.note,
    tagIds = draft.labels.filter { id -> tags.options.any { it.id == id } },
    receiptEnabled = draft.receipt != null,
    receiptCurrency = draft.receipt?.currency?.value ?: receiptCurrency,
    receiptAmount = draft.receipt?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
    amountError = null,
    toAmountError = null,
    accountError = false,
    toAccountError = null,
    categoryError = TemplateGap.CATEGORY in draft.gaps,
    step = when {
        TemplateGap.ACCOUNT in draft.gaps -> EntryStep.ACCOUNT
        TemplateGap.TARGET_ACCOUNT in draft.gaps -> EntryStep.TO_ACCOUNT
        else -> EntryStep.DETAILS
    },
    keypadTarget = KeypadTarget.AMOUNT,
    templateName = templateName,
    payment = null,
)
