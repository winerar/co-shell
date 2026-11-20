package app.coshell.reports.capital

import app.coshell.accounts.list.AccountGrouping
import app.coshell.accounts.list.AccountsUiState
import app.coshell.accounts.list.buildAccountGroups
import app.coshell.budgets.list.number
import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.AccountWorth
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.accountsAt
import app.coshell.core.domain.reports.netWorth
import app.coshell.core.model.ACCOUNT_ORDER
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.ReportsUiState.CapitalPoint
import app.coshell.reports.signed
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.math.absoluteValue

/** What the user changed on the screen: the month, the search, the order and which groups they folded or opened. */
data class CapitalView(
    val month: YearMonth,
    val query: String = "",
    val order: CapitalOrder = CapitalOrder.GROUPS,
    /** Group keys whose state the user flipped: the first group starts open, the others folded (board). */
    val flipped: Set<String> = emptySet(),
)

/** Everything [buildCapitalMonth] needs; [periods] — the months of the line, oldest first. */
data class CapitalInput(
    val accounts: List<Account>,
    val days: List<BalanceDay>,
    val book: RateBook,
    val currency: CurrencyCode,
    val periods: List<BudgetPeriod>,
    val today: LocalDate,
    /** The other family members by id: personal accounts are labelled only with them around. */
    val others: Map<String, String>,
)

/**
 * Pure: «Капитал · октябрь» (#373). [format] — numbers without codes (the line, the head), [money] — amounts with codes
 * (account rows, group totals). A month outside the line falls back to its last point.
 */
internal fun buildCapitalMonth(input: CapitalInput, view: CapitalView, format: MoneyFormat, money: MoneyFormat): CapitalMonthUiState {
    val currency = input.currency.value
    val worth = netWorth(input.accounts, input.days, input.periods, input.book, input.currency, input.today)
        ?: return CapitalMonthUiState(currency = currency, month = view.month, query = view.query, order = view.order)
    val points = worth.points
    val selected = points.indexOfFirst { it.period.month == view.month }.takeIf { it >= 0 } ?: points.lastIndex
    val point = points[selected]
    val before = points.getOrNull(selected - 1)
    val accounts = accountsAt(input.accounts.sortedWith(ACCOUNT_ORDER), input.days, input.book, input.currency, point.asOf, before?.asOf)
    val needle = view.query.trim()
    val found = if (needle.isEmpty()) accounts else accounts.filter { it.account.name.contains(needle, ignoreCase = true) }
    val toRow = { a: AccountWorth -> row(a, input, money, withChange = view.order == CapitalOrder.CHANGE) }
    val change = before?.let { point.amount - it.amount }
    return CapitalMonthUiState(
        currency = currency,
        points = points.map { CapitalPoint(it.period.month, format.number(it.amount), it.amount.amountMinor) },
        selected = selected,
        month = point.period.month,
        asOf = point.asOf,
        value = format.number(point.amount),
        change = change?.let { format.signed(it) },
        falling = change?.isNegative == true,
        count = accounts.size,
        query = view.query,
        order = view.order,
        groups = if (view.order != CapitalOrder.GROUPS) emptyList() else groups(found, view, toRow, money, input.currency),
        rows = when (view.order) {
            CapitalOrder.GROUPS -> emptyList()
            CapitalOrder.AMOUNT -> found.sortedByDescending { it.converted?.amountMinor ?: Long.MIN_VALUE }.map(toRow)
            CapitalOrder.CHANGE -> found.sortedByDescending { it.change?.amountMinor?.absoluteValue ?: -1L }.map(toRow)
        },
        missing = accounts.filter { it.converted == null }.map { it.account.currency.value }.distinct().sorted(),
    )
}

/** By account type (as «Счета»): the first group open, the rest folded; all open while searching. */
private fun groups(
    found: List<AccountWorth>,
    view: CapitalView,
    toRow: (AccountWorth) -> AccountsUiState.Row,
    money: MoneyFormat,
    currency: CurrencyCode,
): List<AccountsUiState.Group> {
    val byId = found.associateBy { it.account.id }
    val total = { rows: List<AccountsUiState.Row> ->
        money.format(rows.mapNotNull { byId[it.id]?.converted }.fold(Money.zero(currency), Money::plus))
    }
    return buildAccountGroups(found.map(toRow), AccountGrouping.TYPE, emptySet(), total).mapIndexed { i, group ->
        val open = view.query.isNotBlank() || ((i == 0) != (group.key in view.flipped))
        group.copy(expanded = open)
    }
}

private fun row(a: AccountWorth, input: CapitalInput, money: MoneyFormat, withChange: Boolean): AccountsUiState.Row {
    val account = a.account
    val approx = a.converted?.takeIf { account.currency != input.currency }?.let { "≈" + MoneyFormat.NBSP + money.format(it) } // «≈» never wraps away from the amount (#431)
    // «По изменению»: the move replaces «≈» under the balance; an account that did not move keeps «≈».
    val change = a.change?.takeIf { withChange && it.amountMinor != 0L }?.let { if (it.isPositive) "+" + money.format(it) else money.format(it) }
    return AccountsUiState.Row(
        id = account.id,
        name = account.name,
        type = account.type,
        balance = money.format(a.balance),
        isNegative = a.balance.isNegative,
        color = account.color,
        personalOf = if (account.isShared || input.others.isEmpty()) null else input.others[account.ownerMemberId].orEmpty(),
        converted = change ?: approx,
        currency = account.currency.value,
        money = a.balance,
    )
}
