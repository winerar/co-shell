package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.LocalDate

/** A Wallet account as seen in the file. One name may show up with several currencies. */
data class WalletAccountSummary(val name: String, val currencies: Set<CurrencyCode>, val records: Int)

/**
 * A Wallet category as seen in the file (transfers excluded). [expenses]/[incomes] count rows by
 * the sign of the amount; [envelopeIds] are hints, one name may carry several (ARCHITECTURE §8).
 */
data class WalletCategorySummary(
    val name: String,
    val expenses: Int,
    val incomes: Int,
    val envelopeIds: Set<String>,
)

/** The first wizard screen: what is in the file before anything is mapped. */
data class WalletSummary(
    val records: Int,
    val firstDate: LocalDate?,
    val lastDate: LocalDate?,
    val accounts: List<WalletAccountSummary>,
    val currencies: Set<CurrencyCode>,
    val categories: List<WalletCategorySummary>,
    val pairedTransfers: Int,
    val unpairedTransfers: Int,
    val labels: Set<String>,
) {
    companion object {
        /** Wallet's category name of transfer rows; not a category in co-shell. */
        const val TRANSFER_CATEGORY = "TRANSFER"

        fun of(records: List<WalletRecord>): WalletSummary {
            val transfers = WalletTransferMatcher.match(records)
            val dates = records.map { it.dateTime.date }
            return WalletSummary(
                records = records.size,
                firstDate = dates.minOrNull(),
                lastDate = dates.maxOrNull(),
                accounts = records.groupBy { it.account }.map { (name, rows) ->
                    WalletAccountSummary(name, rows.mapTo(linkedSetOf()) { it.amount.currency }, rows.size)
                }.sortedBy { it.name.lowercase() },
                currencies = records.map { it.amount.currency }.sortedBy { it.value }.toSet(),
                categories = records
                    .filter { !it.isTransfer && it.category != TRANSFER_CATEGORY }
                    .groupBy { it.category }
                    .map { (name, rows) ->
                        WalletCategorySummary(
                            name = name,
                            expenses = rows.count { it.amount.amountMinor < 0 },
                            incomes = rows.count { it.amount.amountMinor >= 0 },
                            envelopeIds = rows.mapNotNull { it.envelopeId }.sorted().toSet(),
                        )
                    }.sortedBy { it.name.lowercase() },
                pairedTransfers = transfers.pairs.size,
                unpairedTransfers = transfers.unpaired.size,
                labels = records.mapNotNull { it.label }.sorted().toSet(),
            )
        }
    }
}
