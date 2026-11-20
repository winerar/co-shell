package app.coshell.transactions.edit

import app.coshell.core.domain.quickentry.ReceiptPrefills
import app.coshell.core.domain.receipts.AfipQrFormat
import app.coshell.core.domain.receipts.Receipt
import app.coshell.core.domain.receipts.ReceiptNote
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import kotlinx.datetime.LocalDate

/**
 * The sheet was filled from a read receipt (#526 QR, a shared PDF or link): [seller] is the CUIT (QR) or the shop
 * ([document] — a receipt file or text, «Из чека · Teleshka») for the quiet source line, [key] marks the saved operation,
 * [duplicate] is the live operation of the same receipt (the «Этот чек уже записан» window).
 */
data class ScanUi(val seller: String, val key: String, val duplicate: ScanDuplicate? = null, val document: Boolean = false)

/** What the window shows about the operation already made from the receipt; the amount is formatted, the date is not. */
data class ScanDuplicate(val date: LocalDate, val amount: String, val category: String?)

/**
 * A new expense from the receipt: the amount — or, on an account in another currency, the purchase amount (the debited
 * sum then follows by the rate, as for any receipt) — and the day of the receipt. Line items go to an empty note
 * ([ReceiptNote]); category and tags stay as they are.
 */
internal fun TransactionEditUiState.withReceipt(receipt: Receipt, moneyFormat: MoneyFormat): TransactionEditUiState {
    val prefill = ReceiptPrefills.of(receipt, account?.currency?.let(::CurrencyCode))
    return copy(
        isNew = true,
        type = TransactionType.EXPENSE,
        refund = false,
        amount = prefill.amount?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
        amountSuggested = false,
        suggestedRateDate = null,
        amountError = null,
        receiptEnabled = prefill.purchase != null,
        receiptCurrency = prefill.purchase?.currency?.value ?: receiptCurrency,
        receiptAmount = prefill.purchase?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
        receiptAmountError = null,
        receiptCurrencyError = null,
        keypadTarget = if (prefill.purchase != null) KeypadTarget.RECEIPT else KeypadTarget.AMOUNT,
        date = prefill.date,
        note = note.ifBlank { ReceiptNote.of(receipt, moneyFormat::format).orEmpty() },
        scan = ScanUi(
            seller = receipt.merchant,
            key = prefill.dedupeKey,
            duplicate = scan?.duplicate,
            document = receipt.format != AfipQrFormat.id,
        ),
    )
}
