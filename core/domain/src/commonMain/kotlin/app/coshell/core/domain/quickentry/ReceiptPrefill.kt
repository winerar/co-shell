package app.coshell.core.domain.quickentry

import app.coshell.core.domain.receipts.Receipt
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * Where the numbers of a read receipt (QR, link, PDF — any [Receipt]) go in the entry sheet (#526).
 *
 * - [amount] — the debited sum, set only when the account is in the receipt's currency.
 * - [purchase] — «Сумма покупки», set when the account is in another currency (USDT card paying pesos);
 *   the debited sum is then suggested from the rate by the existing receipt flow, not guessed here.
 * - [dedupeKey] — [Receipt.key], to warn «already recorded».
 */
data class ReceiptPrefill(
    val amount: Money?,
    val purchase: Money?,
    val date: LocalDate,
    val dedupeKey: String,
)

object ReceiptPrefills {
    /** [accountCurrency] is `null` while no account is chosen yet: the receipt then fills the plain amount. */
    fun of(receipt: Receipt, accountCurrency: CurrencyCode?): ReceiptPrefill {
        val sameCurrency = accountCurrency == null || accountCurrency == receipt.total.currency
        return ReceiptPrefill(
            amount = receipt.total.takeIf { sameCurrency },
            purchase = receipt.total.takeUnless { sameCurrency },
            date = receipt.date,
            dedupeKey = receipt.key,
        )
    }
}
