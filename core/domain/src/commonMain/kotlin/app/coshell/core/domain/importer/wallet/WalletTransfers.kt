package app.coshell.core.domain.importer.wallet

/** Two rows of one Wallet transfer: money left [from] and arrived to [to]. */
data class WalletTransferPair(val from: WalletRecord, val to: WalletRecord)

data class WalletTransfers(
    val pairs: List<WalletTransferPair>,
    /** Transfer rows without a counterpart: the wizard asks for the other account or a type. */
    val unpaired: List<WalletRecord>,
)

/**
 * Glues Wallet's two-row transfers (ARCHITECTURE §8): rows with `transfer=true`, the same
 * date-time to the second, opposite signs, different accounts and equal `|ref_currency_amount|`.
 * Currencies may differ (USD +1500 ↔ AMD −539 810 is an exchange). Several transfers in the same
 * second are matched in file order; whatever finds no partner is unpaired.
 */
object WalletTransferMatcher {

    fun match(records: List<WalletRecord>): WalletTransfers {
        val pairs = mutableListOf<WalletTransferPair>()
        val unpaired = mutableListOf<WalletRecord>()
        records.filter { it.isTransfer }
            .groupBy { it.dateTime }
            .values
            .forEach { group ->
                val outgoing = group.filter { it.amount.amountMinor < 0 }.toMutableList()
                val incoming = group.filter { it.amount.amountMinor >= 0 }.toMutableList()
                for (out in outgoing.toList()) {
                    val partner = incoming.firstOrNull { inc ->
                        inc.account != out.account && inc.refAmount.abs().sameValueAs(out.refAmount.abs())
                    } ?: continue
                    pairs += WalletTransferPair(from = out, to = partner)
                    outgoing -= out
                    incoming -= partner
                }
                unpaired += outgoing
                unpaired += incoming
            }
        return WalletTransfers(pairs, unpaired.sortedBy { it.line })
    }
}
