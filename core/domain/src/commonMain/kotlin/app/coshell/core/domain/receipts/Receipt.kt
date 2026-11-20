package app.coshell.core.domain.receipts

import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * A purchase read from any source (#526 QR, shared links, PDF and image receipts): the same shape whatever the
 * format, so the entry sheet fills one way. Formats live in [ReceiptFormats].
 *
 * - [merchant] — what the quiet «Из чека» line names: a shop («Teleshka») or a tax id (`30-68584975-1`).
 * - [key] — one receipt, one key (saved as `transactions.receipt_key`, warns «already recorded»). Must stay stable
 *   for a format: changing it makes old operations look new.
 * - [items] — line items when the format has them (a QR has none); not used for the amount, [total] is.
 */
data class Receipt(
    val total: Money,
    val date: LocalDate,
    val merchant: String,
    val key: String,
    val format: String,
    val items: List<ReceiptItem> = emptyList(),
)

/** One line of a receipt: name as printed, quantity as printed («1», «0,5»), the line's sum. */
data class ReceiptItem(val name: String, val quantity: String, val amount: Money)
