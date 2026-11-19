package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * Where the numbers of a scanned receipt go in the entry sheet (#526).
 *
 * - [amount] — the debited sum, set only when the account is in the receipt's currency.
 * - [purchase] — «Сумма покупки», set when the account is in another currency (USDT card paying pesos);
 *   the debited sum is then suggested from the rate by the existing receipt flow, not guessed here.
 * - [dedupeKey] — [ReceiptQr.key], to warn «already recorded».
 */
data class ReceiptPrefill(
    val amount: Money?,
    val purchase: Money?,
    val date: LocalDate,
    val dedupeKey: String,
)

object ReceiptPrefills {
    /** [accountCurrency] is `null` while no account is chosen yet: the receipt then fills the plain amount. */
    fun of(qr: ReceiptQr, accountCurrency: CurrencyCode?): ReceiptPrefill {
        val sameCurrency = accountCurrency == null || accountCurrency == qr.money.currency
        return ReceiptPrefill(
            amount = qr.money.takeIf { sameCurrency },
            purchase = qr.money.takeUnless { sameCurrency },
            date = qr.date,
            dedupeKey = qr.key,
        )
    }
}
