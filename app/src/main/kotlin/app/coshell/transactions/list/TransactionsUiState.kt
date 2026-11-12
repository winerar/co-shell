package app.coshell.transactions.list

import kotlinx.datetime.LocalDate

/** The Transactions tab: transactions grouped by local day, newest first. */
data class TransactionsUiState(
    val isLoading: Boolean = false,
    val days: List<Day> = emptyList(),
    /** Older transactions exist beyond the loaded page. */
    val hasMore: Boolean = false,
    val filter: ListFilter = ListFilter(),
    /** What the filter sheets offer: accounts, categories, family members. */
    val choices: FilterChoices = FilterChoices(),
    /** Count and net of everything the filter found (4А); null while no filter is set. */
    val total: Total? = null,
    /** For the ready-made periods of the «Период» sheet. */
    val today: LocalDate? = null,
) {
    /**
     * «23 операции · −184 300,00 ARS», [detail] under it: «≈ 182,40 USD» or the nets of each currency, one per line.
     * [amount] is null when nothing nets (only transfers); [tone] — [Kind.EXPENSE] for a minus, [Kind.INCOME] for a plus;
     * [noRate] — codes of currencies left out of «≈» for lack of any rate ("AMD, RUB").
     */
    data class Total(
        val count: Int,
        val amount: String? = null,
        val detail: String? = null,
        val tone: Kind? = null,
        val noRate: String? = null,
    )

    data class Day(
        val date: LocalDate,
        val relative: RelativeDay,
        val rows: List<Row>,
        /** Net of the day's incomes and expenses per currency ("−45 300,00 ARS · +1 200,00 USD"); transfers don't count. */
        val total: String? = null,
        /** [Kind.EXPENSE] — the day only spent, [Kind.INCOME] — only earned, `null` — mixed. */
        val totalTone: Kind? = null,
    )

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
