package app.coshell.transactions.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.search.filterTotal
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.domain.tags.TagRepository
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Live list of transactions, a page at a time ([GrowingWindow]), with search and filter chips ([ListFilter]);
 * grouping and formatting live in [buildTransactionDays].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactions: TransactionRepository,
    accounts: AccountRepository,
    categories: CategoryRepository,
    otherMembers: OtherMembers,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val deleteWithUndo: DeleteTransactionWithUndo,
    rates: ExchangeRateRepository,
    reportCurrencies: ReportCurrencySettings,
    tags: TagRepository,
) : ViewModel() {

    private val _selection = MutableStateFlow(Selection())

    /** Rows chosen by a long press (19В); the screen swaps the bottom bar for «Дублировать · Удалить». */
    val selection: StateFlow<Selection> = _selection.asStateFlow()

    fun onLongPress(id: String) = _selection.update { it.longPress(id) }

    fun onToggle(id: String) = _selection.update { it.toggle(id) }

    fun clearSelection() = _selection.update { Selection() }

    /** Deletes the chosen rows at once; one «Отменить» brings them all back. */
    fun deleteSelected() {
        val ids = _selection.value.ids
        if (ids.isEmpty()) return
        _selection.value = Selection()
        viewModelScope.launch { deleteWithUndo(ids) }
    }

    private val window = GrowingWindow()
    private val filter = MutableStateFlow(ListFilter())

    /** Rows shown while a filter is set: everything is loaded for the total, the list still grows by pages. */
    private val shownLimit = MutableStateFlow(GrowingWindow.PAGE_SIZE)

    /** The screen nears the end of the list: load the next page. */
    fun loadMore() {
        if (filter.value.needsAll) shownLimit.update { it + GrowingWindow.PAGE_SIZE } else window.loadMore()
    }

    fun onFilter(change: (ListFilter) -> ListFilter) = filter.update { old ->
        change(old).also { new -> if (new.roomKey != old.roomKey) shownLimit.value = GrowingWindow.PAGE_SIZE }
    }

    /** «Обзор» → «Внимание»: this month's operations without a category. */
    fun showUncategorizedThisMonth() = onFilter { ListFilter.uncategorizedThisMonth(today()) }

    private fun today() = clock.now().toLocalDateTime(timeZone).date

    /** Rows from Room with what is needed to show and search them; re-queried only when [ListFilter.roomKey] changes. */
    private class Source(
        val page: GrowingWindow.Page<Transaction>,
        val accountList: List<Account>,
        val accountsById: Map<String, Account>,
        val categoryList: List<Category>,
        val categoriesById: Map<String, Category>,
        val authors: Map<String, String>,
        val tags: Tags,
        val search: SearchIndex,
    )

    /** Live tags, by id, and how operations use them — for pills, the «# Тег» sheet and search. */
    private class Tags(val list: List<Tag>, val usage: Map<String, TagUsage>) {
        val byId: Map<String, Tag> = list.associateBy { it.id }
    }

    private val tagInfo = combine(tags.observeTags(), tags.observeUsage(), ::Tags)

    private val authors = otherMembers.names()

    private val source = combine(
        combine(filter.map { it.roomKey }.distinctUntilChanged(), authors) { key, names -> key to names.keys }
            .distinctUntilChanged()
            .flatMapLatest { (key, others) ->
                if (key.needsAll) {
                    transactions.observeTransactions(key.toRepositoryFilter(GrowingWindow.PAGE_SIZE, timeZone, others))
                        .map { GrowingWindow.Page(it, hasMore = false) }
                } else {
                    window.observe { limit -> transactions.observeTransactions(key.toRepositoryFilter(limit, timeZone, others)) }
                }
            },
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        authors,
        tagInfo,
    ) { page, accountList, categoryList, names, tagState ->
        val accountsById = accountList.associateBy { it.id }
        val categoriesById = categoryList.associateBy { it.id }
        Source(
            page, accountList, accountsById, categoryList, categoriesById, names, tagState,
            SearchIndex(accountsById, categoriesById, moneyFormat, tagState.byId),
        )
    }

    /** Rates and the main report currency, for the total (4А); the book is rebuilt only when rates change. */
    private val money = combine(rates.observeRates().map { RateBook.of(it) }, reportCurrencies.currencies) { book, currencies ->
        book to currencies.firstOrNull()
    }

    val state: StateFlow<TransactionsUiState> = combine(source, filter, shownLimit, money) { src, f, limit, (book, main) ->
        val matching = src.page.items.filter { f.matches(it, src.search) }
        val shown = if (f.needsAll) matching.take(limit) else matching
        _selection.update { it.keepOnly(shown.mapTo(HashSet()) { t -> t.id }) }
        val today = today()
        TransactionsUiState(
            hasMore = if (f.needsAll) matching.size > limit else src.page.hasMore,
            filter = f,
            choices = buildFilterChoices(src.accountList, src.categoryList, src.authors, f, src.tags.list, src.tags.usage),
            total = if (f.isActive) book.filterTotal(matching, main, timeZone).toUi(moneyFormat) else null,
            today = today,
            days = buildTransactionDays(
                transactions = shown,
                accountsById = src.accountsById,
                categoriesById = src.categoriesById,
                moneyFormat = moneyFormat,
                timeZone = timeZone,
                today = today,
                authors = src.authors,
                tagsById = src.tags.byId,
            ),
        )
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState(isLoading = true))
}
