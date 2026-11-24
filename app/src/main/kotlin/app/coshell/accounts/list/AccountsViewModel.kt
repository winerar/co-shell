package app.coshell.accounts.list

import app.coshell.core.model.Account
import app.coshell.core.domain.order.ReorderUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.balanceTotals
import app.coshell.core.domain.settings.InMemoryScreenOptions
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.domain.settings.ScreenOptionsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.domain.credit.CreditCard
import app.coshell.core.model.CreditTerms
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** How the user looks at the list: grouping, folded groups, search — kept while the app runs. */
internal data class AccountsView(
    val showArchived: Boolean = false,
    val grouping: AccountGrouping = AccountGrouping.TYPE,
    val collapsed: Set<String> = emptySet(),
    val query: String = "",
    val searchOpen: Boolean = false,
)

/** Main report currency, rates and today — for «≈ … USD» next to balances and group totals. */
/** Built once per rates change, not on every search keystroke or folded group (#258). */
internal class Conversion(rates: List<ExchangeRate>, val main: CurrencyCode, val today: LocalDate) {
    val book = RateBook.of(rates)
}

@HiltViewModel
class AccountsViewModel @Inject constructor(
    accounts: AccountRepository,
    private val moneyFormat: MoneyFormat,
    otherMembers: OtherMembers = OtherMembers { flowOf(emptyMap()) },
    rates: ExchangeRateRepository? = null,
    reportCurrencies: ReportCurrencySettings? = null,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    screenOptions: ScreenOptionsStore = InMemoryScreenOptions(),
    private val reorder: ReorderUseCase? = null,
) : ViewModel() {

    private val view = MutableStateFlow(AccountsView())

    /** Active accounts as last shown, for «Порядок» (#338). */
    private var shownActive: List<Account> = emptyList()

    /** The order just dragged, shown until the saved one comes back from the database (no jump back on drop). */
    private val localOrder = MutableStateFlow<List<String>?>(null)

    init {
        // «Настройки → Экраны → Счета»: how the list opens (grouping, archive); the user's taps override it afterwards.
        viewModelScope.launch {
            val o = screenOptions.options.first()
            view.update { it.copy(grouping = if (o.accountsByCurrency) AccountGrouping.CURRENCY else it.grouping, showArchived = o.accountsShowArchived || it.showArchived) }
        }
    }

    private val conversion = combine(
        rates?.observeRates() ?: flowOf(emptyList()),
        reportCurrencies?.currencies ?: flowOf(ReportCurrencySettings.DEFAULT),
    ) { list, currencies -> Conversion(list, currencies.firstOrNull() ?: ReportCurrencySettings.DEFAULT.first(), clock.todayIn(timeZone)) }

    private val ordered = combine(accounts.observeAccountsWithBalance(includeArchived = true), localOrder) { list, local ->
        val repoOrder = list.filter { !it.account.archived }.map { it.account.id }
        if (local != null && local == repoOrder) localOrder.value = null
        val sorted = if (local == null || local == repoOrder) list else list.sortedBy { item -> local.indexOf(item.account.id).let { if (it < 0) Int.MAX_VALUE else it } }
        shownActive = sorted.filter { !it.account.archived }.map { it.account }
        sorted
    }

    val state: StateFlow<AccountsUiState> =
        combine(ordered, view, otherMembers.names(), conversion) { list, v, others, c ->
            buildState(list, v.showArchived, others, v, c)
        }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState(isLoading = true))

    fun toggleArchived() = view.update { it.copy(showArchived = !it.showArchived) }

    fun onGrouping(grouping: AccountGrouping) = view.update { it.copy(grouping = grouping) }

    fun onToggleGroup(key: String) = view.update {
        it.copy(collapsed = if (key in it.collapsed) it.collapsed - key else it.collapsed + key)
    }

    fun onQuery(query: String) = view.update { it.copy(query = query) }

    fun onToggleSearch() = view.update { it.copy(searchOpen = !it.searchOpen, query = "") }

    /** «Порядок» (#338): the active account at [from] goes to [to]; shown at once, saved for the family. */
    fun moveAccount(from: Int, to: Int) {
        val shown = shownActive
        if (from !in shown.indices || to !in shown.indices || from == to) return
        localOrder.value = shown.map { it.id }.toMutableList().apply { add(to, removeAt(from)) }
        viewModelScope.launch { reorder?.moveAccount(shown, from, to) }
    }

    internal fun buildState(
        accounts: List<AccountWithBalance>,
        showArchived: Boolean,
        others: Map<String, String> = emptyMap(),
        view: AccountsView = AccountsView(showArchived = showArchived),
        conversion: Conversion? = null,
    ): AccountsUiState {
        val convert = { sums: List<Money> ->
            if (conversion == null || sums.isEmpty()) {
                null
            } else {
                conversion.book.balanceTotals(sums, listOf(conversion.main), conversion.today).first()
                    .takeIf { it.missing.isEmpty() }
                    ?.let { "≈" + MoneyFormat.NBSP + moneyFormat.format(it.sum) } // «≈» never wraps away from the amount (#431)
            }
        }
        val today = conversion?.today ?: clock.todayIn(timeZone)
        val toRow = { item: AccountWithBalance -> toRow(item, others, conversion?.main, convert, today) }
        val (archived, active) = accounts.partition { it.account.archived }
        val activeRows = active.map(toRow)
        val shown = if (view.query.isBlank()) activeRows else activeRows.filter { it.name.contains(view.query.trim(), ignoreCase = true) }
        val grouping = if (view.grouping == AccountGrouping.OWNER && others.isEmpty()) AccountGrouping.TYPE else view.grouping
        val inTotal = { rows: List<AccountsUiState.Row> -> rows.filter { !it.excludedFromTotal }.mapNotNull { it.money } }
        return AccountsUiState(
            active = activeRows,
            archived = archived.map(toRow),
            showArchived = showArchived,
            total = convert(inTotal(activeRows)),
            grouping = grouping,
            canGroupByOwner = others.isNotEmpty(),
            groups = buildAccountGroups(shown, grouping, view.collapsed) { rows -> convert(inTotal(rows)) },
            query = view.query,
            searchOpen = view.searchOpen,
        )
    }

    private fun toRow(
        item: AccountWithBalance,
        others: Map<String, String>,
        main: CurrencyCode?,
        convert: (List<Money>) -> String?,
        today: LocalDate,
    ) = AccountsUiState.Row(
        id = item.account.id,
        name = item.account.name,
        type = item.account.type,
        balance = moneyFormat.format(item.balance),
        isNegative = item.balance.isNegative,
        excludedFromTotal = item.account.excludedFromTotal,
        color = item.account.color,
        // Labels matter only with other members around.
        personalOf = if (item.account.isShared || others.isEmpty()) null else others[item.account.ownerMemberId].orEmpty(),
        converted = if (item.account.currency == main) null else convert(listOf(item.balance)),
        currency = item.account.currency.value,
        prepaid = item.account.prepaid,
        credit = item.account.credit?.let { creditRow(it, item.balance, today) },
        money = item.balance,
    )

    /** «Доступно … из …» and the payment day of a credit card (#686): from its conditions and the balance. */
    private fun creditRow(terms: CreditTerms, balance: Money, today: LocalDate): AccountsUiState.Row.Credit {
        val status = CreditCard.status(terms, balance, today)
        return AccountsUiState.Row.Credit(
            available = moneyFormat.format(status.available),
            limit = moneyFormat.format(status.limit),
            used = (status.debt.amountMinor.toFloat() / status.limit.amountMinor).coerceIn(0f, 1f),
            due = status.dueDate,
            graceUntil = status.graceUntil,
        )
    }
}
