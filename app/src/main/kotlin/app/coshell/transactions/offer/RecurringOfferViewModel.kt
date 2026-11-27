package app.coshell.transactions.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.recurring.RecurringCandidate
import app.coshell.core.domain.recurring.recurringCandidates
import app.coshell.core.domain.settings.SuggestionDismissals
import app.coshell.core.domain.templates.SaveTemplateUseCase
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * «Похоже на ежемесячный платёж · Сделать регулярным» (#789, board `Smart789Coach`): the newest regular payment hidden in the
 * last [RECENT] operations. «Сделать регулярным» saves a template with «Повтор» (reminds, posts nothing by itself) and opens
 * it ([opened]); «Не сейчас» — not offered again on this phone.
 */
@HiltViewModel
class RecurringOfferViewModel @Inject constructor(
    transactions: TransactionRepository,
    categories: CategoryRepository,
    private val templates: TemplateRepository,
    private val saveTemplate: SaveTemplateUseCase,
    private val dismissals: SuggestionDismissals,
    private val ids: IdGenerator,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private var current: Pair<RecurringCandidate, String>? = null

    val offer: StateFlow<RecurringOffer?> = combine(
        transactions.observeTransactions(TransactionFilter(limit = RECENT)),
        templates.observeTemplates(),
        categories.observeCategories(includeArchived = true),
        dismissals.dismissed,
    ) { list, templateList, categoryList, dismissed ->
        val found = recurringCandidates(list, templateList, dismissed, timeZone, clock.todayIn(timeZone)).firstOrNull()
        val name = found?.let { c -> c.latest.payee?.takeIf { it.isNotBlank() } ?: c.latest.note.takeIf { it.isNotBlank() } ?: categoryList.firstOrNull { it.id == c.latest.categoryId }?.name }
        current = found?.let { it to name.orEmpty() }
        if (found == null || name == null) null
        else RecurringOffer(found.key, name.trim(), moneyFormat.copy(trimZeroFraction = true).format(found.amount), weekly = found.recurrence is Recurrence.Weekly, day = found.last.day)
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val _opened = Channel<String>(Channel.BUFFERED)

    /** The id of the template just made, to open in its editor. */
    val opened: Flow<String> = _opened.receiveAsFlow()

    fun accept() {
        val (candidate, name) = current ?: return
        viewModelScope.launch {
            val template = TransactionTemplate(
                id = ids.newId(),
                name = name.trim().ifEmpty { return@launch },
                type = candidate.latest.type,
                accountId = candidate.latest.accountId,
                currency = candidate.latest.amount.currency,
                amount = candidate.amount,
                categoryId = candidate.latest.categoryId,
                note = candidate.latest.note,
                sortOrder = (templates.maxSortOrder() ?: -1) + 1,
                schedule = RecurringSchedule(candidate.recurrence, start = candidate.next),
            )
            if (saveTemplate(template) is SaveTemplateUseCase.Result.Saved) _opened.send(template.id)
        }
    }

    fun later() {
        val key = current?.first?.key ?: return
        viewModelScope.launch { dismissals.dismiss(key) }
    }

    private companion object {
        /** As the entry sheet: enough history for a monthly habit, never the whole table. */
        const val RECENT = 500
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** What the card says: [name] — the payee, note or category; [day] — the day of month of a monthly payment. */
data class RecurringOffer(val key: String, val name: String, val amount: String, val weekly: Boolean, val day: Int)
