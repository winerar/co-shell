package app.coshell.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.export.CsvExport
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.tags.TagRepository
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * «Данные и копия → Экспорт в CSV» (#204, board `Fb204Export`): a period (a month, a year or own days), the accounts, the number of
 * operations that fall in, and the file. The file is built by [CsvExport]; where it goes (share) is the screen's business.
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    private val transactions: TransactionRepository,
    accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val tags: TagRepository,
    private val otherMembers: OtherMembers,
    clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val today = clock.todayIn(timeZone)

    private val choice = MutableStateFlow(Choice(period = monthOf(today)))

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ExportUiState> = combine(accounts.observeAccounts(includeArchived = false), choice) { list, c -> list to c }
        .flatMapLatest { (list, c) ->
            val ids = if (c.allAccounts) null else c.accountIds.intersect(list.mapTo(HashSet()) { it.id })
            transactions.observeTransactions(TransactionFilter(accountIds = ids, from = c.period.first.start(), to = c.period.second.start())).map { found ->
                ExportUiState(
                    kind = c.kind,
                    first = c.period.first,
                    last = c.period.second.minus(1, DateTimeUnit.DAY),
                    allAccounts = c.allAccounts,
                    accounts = list.map { AccountPick(it.id, it.name, it.currency.value, c.allAccounts || it.id in c.accountIds) },
                    count = if (ids != null && ids.isEmpty()) 0 else found.size,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ExportUiState())

    fun onMonth(first: LocalDate) = choice.update { it.copy(kind = ExportKind.MONTH, period = monthOf(first)) }

    fun onYear(year: Int) = choice.update { it.copy(kind = ExportKind.YEAR, period = LocalDate(year, 1, 1) to LocalDate(year + 1, 1, 1)) }

    /** Own days: [first] and [last] are both included. */
    fun onCustom(first: LocalDate, last: LocalDate) =
        choice.update { it.copy(kind = ExportKind.CUSTOM, period = minOf(first, last) to maxOf(first, last).plus(1, DateTimeUnit.DAY)) }

    /** A tap on a kind chip: it keeps what it can (the month or year of the shown period) and asks nothing. */
    fun onKind(kind: ExportKind) = choice.update {
        when (kind) {
            ExportKind.MONTH -> it.copy(kind = kind, period = monthOf(it.period.first))
            ExportKind.YEAR -> it.copy(kind = kind, period = LocalDate(it.period.first.year, 1, 1) to LocalDate(it.period.first.year + 1, 1, 1))
            ExportKind.CUSTOM -> it.copy(kind = kind)
        }
    }

    fun onAllAccounts(all: Boolean) = choice.update { it.copy(allAccounts = all, accountIds = if (all) it.accountIds else it.accountIds.ifEmpty { emptySet() }) }

    fun onAccount(id: String, picked: Boolean, every: List<String>) = choice.update {
        val base = if (it.allAccounts) every.toSet() else it.accountIds
        it.copy(allAccounts = false, accountIds = if (picked) base + id else base - id)
    }

    /** The CSV text for what is shown; [labels] come from resources, [dateText] writes a moment as the person reads it. */
    fun build(labels: CsvExport.Labels, dateText: (kotlin.time.Instant) -> String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val c = choice.value
            val list = categories.observeCategories(includeArchived = true).first()
            val byId = list.associateBy { it.id }
            val accountList = state.value.accounts
            val allTags = tags.getTags()
            val members = otherMembers.names().first()
            val names = CsvExport.Names(
                accounts = accountMap(accountList),
                category = { id -> byId[id]?.let { categoryName(it, byId) } },
                tag = { ref -> allTags.firstOrNull { it.id == ref || it.name.equals(ref, ignoreCase = true) }?.name ?: ref },
                member = { uid -> members[uid] },
                date = dateText,
            )
            val ids = if (c.allAccounts) null else c.accountIds
            val found = transactions.observeTransactions(TransactionFilter(accountIds = ids, from = c.period.first.start(), to = c.period.second.start())).first()
            val filter = CsvExport.Filter(c.period.first.start(), c.period.second.start(), ids)
            onReady(CsvExport.build(found, filter, names, labels))
        }
    }

    private fun accountMap(picks: List<AccountPick>): Map<String, Account> =
        picks.associate { it.id to Account(it.id, it.name, app.coshell.core.model.CurrencyCode(it.currency)) }

    private fun categoryName(c: Category, byId: Map<String, Category>): String =
        c.parentId?.let { byId[it]?.name }?.let { "$it · ${c.name}" } ?: c.name

    private fun LocalDate.start() = atStartOfDayIn(timeZone)

    private fun monthOf(day: LocalDate): Pair<LocalDate, LocalDate> {
        val first = LocalDate(day.year, day.month, 1)
        return first to first.plus(1, DateTimeUnit.MONTH)
    }

    private data class Choice(
        val kind: ExportKind = ExportKind.MONTH,
        /** First day and the day after the last. */
        val period: Pair<LocalDate, LocalDate>,
        val allAccounts: Boolean = true,
        val accountIds: Set<String> = emptySet(),
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

enum class ExportKind { MONTH, YEAR, CUSTOM }

data class AccountPick(val id: String, val name: String, val currency: String, val picked: Boolean)

/** [first] .. [last] are the days of the period, both included. */
data class ExportUiState(
    val kind: ExportKind = ExportKind.MONTH,
    val first: LocalDate = LocalDate(2000, 1, 1),
    val last: LocalDate = LocalDate(2000, 1, 31),
    val allAccounts: Boolean = true,
    val accounts: List<AccountPick> = emptyList(),
    val count: Int = 0,
)
