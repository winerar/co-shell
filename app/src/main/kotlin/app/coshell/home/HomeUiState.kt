package app.coshell.home

import app.coshell.core.model.AccountType
import app.coshell.transactions.list.TransactionsUiState
import kotlinx.datetime.LocalDate

/**
 * «Обзор» (design-refactor Step 5): hero card with the total, bento tiles 2×2 (expenses, income,
 * attention, accounts) and the latest operations. All texts are formatted already.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    /** Total of all accounts (except excluded ones) in each report currency, main first, e.g. ["125 300,00 USD", "180 000 000 ARS"]. */
    val totals: List<String> = emptyList(),
    /** Oldest rate behind the totals ("по курсу на 9 ноября"); `null` when nothing was converted. */
    val rateDate: LocalDate? = null,
    /** Currencies left out of the totals for lack of any rate. */
    val missing: List<String> = emptyList(),
    /** Plain sums per account currency — the breakdown under the totals (only when there are several). */
    val byCurrency: List<String> = emptyList(),
    /** This month's expenses; `null` while loading. */
    val spending: MonthSpending? = null,
    /** This month's incomes, same rules as [spending]. */
    val income: MonthTotals? = null,
    val attention: Attention = Attention(),
    val accounts: List<AccountRow> = emptyList(),
    /** The newest operations (up to [RECENT_COUNT]), already formatted like on the Transactions tab. */
    val recent: List<TransactionsUiState.Row> = emptyList(),
) {
    /**
     * Expenses of the current month in each report currency, main first; each expense at the rate of its day.
     * @property month first day of the month ("Траты за ноябрь").
     * @property missing currencies of expenses left out for lack of any rate.
     * @property converted some expense is in another currency than a report one (show the rate hint).
     * @property daily expenses of each day in the main currency, minor units (index 0 — the 1st), for the mini bars.
     * @property today index of today in [daily].
     */
    data class MonthSpending(
        val month: LocalDate,
        val totals: List<String>,
        val missing: List<String>,
        val converted: Boolean = false,
        val daily: List<Long> = emptyList(),
        val today: Int = 0,
    )

    /** Sums of one kind of operations this month in each report currency, main first. */
    data class MonthTotals(val totals: List<String>, val missing: List<String> = emptyList())

    /**
     * What may need a look («Внимание» tile).
     * @property missingRates currencies without any rate (left out of totals).
     * @property staleRatesSince date of the oldest rate behind the totals when it is older than [STALE_RATE_DAYS].
     * @property uncategorized this month's expenses and incomes without a category.
     */
    data class Attention(
        val missingRates: List<String> = emptyList(),
        val staleRatesSince: LocalDate? = null,
        val uncategorized: Int = 0,
    ) {
        val count: Int get() = missingRates.size + (if (staleRatesSince != null) 1 else 0) + (if (uncategorized > 0) 1 else 0)
        val isRatesIssue: Boolean get() = missingRates.isNotEmpty() || staleRatesSince != null
    }

    /** @property excludedFromTotal the account is left out of [totals] ("не в общей сумме"). */
    data class AccountRow(
        val id: String,
        val name: String,
        val balance: String,
        val isNegative: Boolean,
        val excludedFromTotal: Boolean = false,
        val type: AccountType = AccountType.CARD,
        /** The account's colour (stored palette value), `null` — none. */
        val color: Long? = null,
    )

    companion object {
        const val RECENT_COUNT = 5
        /** Rates are downloaded daily; older ones mean the download keeps failing. */
        const val STALE_RATE_DAYS = 3
    }
}
