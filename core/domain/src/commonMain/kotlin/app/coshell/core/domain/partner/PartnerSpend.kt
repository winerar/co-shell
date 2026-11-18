package app.coshell.core.domain.partner

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Currencies
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate

/** A transaction another member of the family wrote or changed; [updatedAtMs] — when (server clock of that write). */
data class PartnerChange(val transaction: Transaction, val updatedAtMs: Long, val authorUid: String)

/** What the sync brought from the other members since [sinceMs]; Room behind it. */
interface PartnerChanges {
    suspend fun since(sinceMs: Long, myUid: String): List<PartnerChange>
}

/**
 * «Трата партнёра» (#326, board `Fb326A`): the changes that deserve a notice — expenses of other members (not transfers, not
 * imports), not smaller than [minimum] whole units of [main] at the rate of the day of the expense ([dayOf]). Without a rate the
 * size is unknown and the notice goes out anyway: better one too many than a silent big purchase.
 */
fun spendsToNotify(
    changes: List<PartnerChange>,
    minimum: Int,
    main: CurrencyCode,
    converter: CurrencyConverter,
    dayOf: (Transaction) -> LocalDate,
): List<PartnerChange> {
    val threshold = minimum.toLong() * pow10(Currencies.info(main).fractionDigits)
    return changes.filter { change ->
        val t = change.transaction
        if (t.type != TransactionType.EXPENSE || t.importSource != null) return@filter false
        if (threshold <= 0L) return@filter true
        val size = if (t.amount.currency == main) t.amount else converter.convert(t.amount, main, dayOf(t))?.money
        size == null || size.amountMinor >= threshold
    }.sortedBy { it.updatedAtMs }
}

private fun pow10(digits: Int): Long {
    var result = 1L
    repeat(digits) { result *= 10 }
    return result
}
