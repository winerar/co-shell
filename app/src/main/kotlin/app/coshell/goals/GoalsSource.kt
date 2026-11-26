package app.coshell.goals

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.goals.GoalBook
import app.coshell.core.domain.goals.GoalLine
import app.coshell.core.domain.goals.GoalRepository
import app.coshell.core.domain.rates.BalanceTotal
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.balanceTotals
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Goals with their progress and the totals in the main report currency (#768); shared by «Цели» and the card on «Обзоре». */
data class GoalsSnapshot(
    val lines: List<GoalLine>,
    val accounts: Map<String, Account>,
    val today: LocalDate,
    /** What all goals hold, by today's rates; currencies without a rate are in [BalanceTotal.missing]. */
    val saved: BalanceTotal,
    /** What to put aside this month for all goals with a deadline. */
    val perMonth: BalanceTotal,
)

/** Builds [GoalsSnapshot]: goals over the balances of their accounts (poll «А»), totals by [RateBook.balanceTotals]. */
class GoalsSource @Inject constructor(
    private val goals: GoalRepository,
    private val accounts: AccountRepository,
    private val rates: ExchangeRateRepository,
    private val currencies: ReportCurrencySettings,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    fun observe(): Flow<GoalsSnapshot> = combine(
        goals.observe(),
        accounts.observeAccountsWithBalance(includeArchived = true),
        rates.observeRates(),
        currencies.currencies,
    ) { list, balances, rateList, report ->
        val today = clock.todayIn(timeZone)
        val book = RateBook.of(rateList)
        val main = report.firstOrNull() ?: ReportCurrencySettings.DEFAULT.first()
        val lines = GoalBook.of(list, balances.associate { it.account.id to it.balance }, today) { money, to ->
            if (money.currency == to) money else book.find(money.currency, to, today)?.rate?.convert(money, to)
        }
        GoalsSnapshot(
            lines = lines,
            accounts = balances.associate { it.account.id to it.account },
            today = today,
            saved = total(book, lines.map { it.progress.saved }, main, today),
            perMonth = total(book, lines.mapNotNull { it.progress.perMonth }, main, today),
        )
    }

    private fun total(book: RateBook, amounts: List<app.coshell.core.model.Money>, main: CurrencyCode, today: LocalDate): BalanceTotal =
        book.balanceTotals(amounts, listOf(main), today).first()
}
