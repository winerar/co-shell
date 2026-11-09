package app.coshell.transactions.list

import kotlinx.datetime.LocalDate

/** The Transactions tab: transactions grouped by local day, newest first. */
data class TransactionsUiState(
    val isLoading: Boolean = false,
    val days: List<Day> = emptyList(),
) {
    data class Day(val date: LocalDate, val relative: RelativeDay, val rows: List<Row>)

    enum class RelativeDay { TODAY, YESTERDAY, OTHER }

    /** One transaction, already formatted. The screen picks texts for [title] == null by [kind]. */
    data class Row(
        val id: String,
        val kind: Kind,
        /** Category name; null for transfers and for transactions without a (live) category. */
        val title: String?,
        /** Account name, or "Карта → Наличные" for transfers. */
        val subtitle: String,
        val note: String,
        /** With a sign for expenses ("-") and incomes ("+"), without one for transfers. */
        val amount: String,
        /** Arriving amount of a transfer between currencies ("+120 000,00 ARS") or the receipt amount of a purchase paid in another currency. */
        val secondaryAmount: String? = null,
        /** Category icon key and colour; both null for transfers and missing categories. */
        val icon: String? = null,
        val color: Long? = null,
        /** Who added it — only for the other family member's operations. */
        val author: String? = null,
    )

    enum class Kind { EXPENSE, INCOME, TRANSFER }
}
