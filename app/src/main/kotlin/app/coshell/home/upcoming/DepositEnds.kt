package app.coshell.home.upcoming

import app.coshell.core.domain.deposit.DepositIncome
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate

/** A deposit that closes within the «Скоро» window (#634, step 3; board `HomeDepositSoon`): the expected [income] (before any tax). */
data class DepositEndRow(val accountId: String, val name: String, val color: Long?, val end: LocalDate, val income: String)

fun buildDepositRows(accounts: List<AccountWithBalance>, moneyFormat: MoneyFormat, today: LocalDate, days: Int): List<DepositEndRow> =
    accounts.mapNotNull { item ->
        val terms = item.account.deposit ?: return@mapNotNull null
        if (item.account.archived || !DepositIncome.endsWithin(terms, today, days)) return@mapNotNull null
        val income = DepositIncome.expected(terms, item.balance, today) ?: return@mapNotNull null
        DepositEndRow(item.account.id, item.account.name, item.account.color, terms.endDate, moneyFormat.format(income))
    }.sortedWith(compareBy({ it.end }, { it.name.lowercase() }))
