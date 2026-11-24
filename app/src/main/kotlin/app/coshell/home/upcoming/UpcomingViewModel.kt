package app.coshell.home.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.credit.CreditCard
import app.coshell.core.domain.debts.DebtBook
import app.coshell.core.domain.debts.DebtRepository
import app.coshell.core.domain.debts.InMemoryDebtRepository
import app.coshell.core.domain.credit.creditPaymentsDue
import app.coshell.core.model.TransactionType
import app.coshell.core.domain.recurring.MissedPaymentsUseCase
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.MoneyFormat
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import app.coshell.core.domain.settings.InMemoryScreenOptions
import app.coshell.core.domain.settings.ScreenOptionsStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn

/**
 * «Скоро · 7 дней» on «Обзоре» (#143). Apart from `HomeViewModel`: a family without regular payments
 * only reads its templates. The day is fixed when the screen subscribes, like the rest of «Обзор».
 */
@HiltViewModel
class UpcomingViewModel @Inject constructor(
    templates: TemplateRepository,
    accounts: AccountRepository,
    transactions: TransactionRepository,
    categories: CategoryRepository,
    moneyFormat: MoneyFormat,
    clock: Clock,
    private val timeZone: TimeZone,
    private val missed: MissedPaymentsUseCase,
    screenOptions: ScreenOptionsStore = InMemoryScreenOptions(),
    debts: DebtRepository = InMemoryDebtRepository(),
) : ViewModel() {

    private val today = clock.todayIn(timeZone)

    val rows: StateFlow<List<UpcomingRow>> = combine(
        templates.observeTemplates(),
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        screenOptions.options.map { it.soonDays }.distinctUntilChanged(),
    ) { list, accountList, categoryList, days ->
        buildUpcomingRows(list, accountList.associateBy { it.id }, categoryList.associateBy { it.id }, moneyFormat, today, days)
    }
        .offMain()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /**
     * Credit card payments of the same days (#686): a card with a debt, its payment day within the window, not paid yet this cycle.
     * Only transfers into the cards since the earliest cycle start are read — a bounded period, not the whole history.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val credit: StateFlow<List<CreditPaymentRow>> = accounts.observeAccountsWithBalance()
        .flatMapLatest { list ->
            val cards = list.filter { it.account.credit != null && !it.account.archived }
            if (cards.isEmpty()) {
                flowOf(emptyList())
            } else {
                val since = cards.minOf { CreditCard.previousDue(it.account.credit!!.payDay, CreditCard.dueDate(it.account.credit!!.payDay, today)) }
                val filter = TransactionFilter(
                    accountIds = cards.map { it.account.id }.toSet(),
                    from = since.atStartOfDayIn(timeZone),
                    type = TransactionType.TRANSFER,
                )
                transactions.observeTransactions(filter).map { buildCreditRows(creditPaymentsDue(list, it, today, timeZone), moneyFormat, today) }
            }
        }
        .offMain()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** Debts to be returned within [DebtBook.REMIND_DAYS] days, today or already late (#552) — in the same card. */
    val debtDue: StateFlow<List<DebtDueRow>> = debts.observeEvents()
        .map { buildDebtRows(DebtBook.due(DebtBook.of(it), today), moneyFormat) }
        .offMain()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** Templates with «Повтор» in all — «Все 24 ›» to «Регулярные платежи» (#403). */
    val total: StateFlow<Int> = templates.observeTemplates()
        .map { list -> list.count { it.schedule != null } }
        .offMain()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    /**
     * «Провести оба» (#406): posts every missed payment of [templateId] by its amount. Nothing posted (the category was
     * archived since) — [onFail] opens the sheet on the oldest one instead.
     */
    fun postAll(templateId: String, onFail: () -> Unit) {
        viewModelScope.launch { if (missed.postAll(templateId, today, timeZone) == 0) onFail() }
    }

    /** «Пропустить»: the missed payments of [templateId] are done without operations. */
    fun skipAll(templateId: String) {
        viewModelScope.launch { missed.skipAll(templateId, today) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
