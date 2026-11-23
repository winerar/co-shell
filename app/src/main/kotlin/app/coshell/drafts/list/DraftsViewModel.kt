package app.coshell.drafts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.R
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.drafts.DraftCheck
import app.coshell.core.domain.drafts.DraftRepository
import app.coshell.core.domain.drafts.PostDraftsUseCase
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.ui.offMain
import app.coshell.undo.UndoCenter
import app.coshell.undo.UndoOffer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * «Черновики» (#586): the drafts oldest first, ready ones ticked until the person unticks them; «Записать» posts the ticked ready ones
 * and offers «Отменить» (the operations go away, the drafts come back); «Удалить» (#628) removes every ticked one, also with «Отменить».
 */
@HiltViewModel
class DraftsViewModel @Inject constructor(
    private val drafts: DraftRepository,
    accounts: AccountRepository,
    categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val postDrafts: PostDraftsUseCase,
    private val undo: UndoCenter,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {
    /** Rows whose tick the person flipped: a ready row starts ticked, an unready one unticked (#628: those can be ticked to delete). */
    private val flipped = MutableStateFlow<Set<String>>(emptySet())

    val state: StateFlow<DraftsUiState> = combine(
        drafts.observeDrafts(),
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        flipped,
    ) { list, accountList, categoryList, flip ->
        val accountMap = accountList.associateBy { it.id }
        val categoryMap = categoryList.associateBy { it.id }
        DraftsUiState(
            isLoading = false,
            today = day(clock.now()),
            ticksTouched = list.any { it.id in flip },
            rows = list.map { draft ->
                val missing = DraftCheck.missing(draft, accountMap.filterValues { !it.archived }, categoryMap)
                DraftRowUi(
                    id = draft.id,
                    title = draft.title,
                    accountName = draft.accountId?.let { accountMap[it]?.name },
                    date = day(Instant.fromEpochMilliseconds(draft.dateMillis)),
                    amount = draft.money?.let { "−" + moneyFormat.format(it) },
                    missing = missing,
                    categoryName = draft.categoryId?.let { categoryMap[it]?.name },
                    source = draft.categorySource,
                    checked = (missing == null) != (draft.id in flip),
                )
            },
        )
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DraftsUiState())

    private fun day(moment: Instant) = moment.toLocalDateTime(timeZone).date

    /** The tick of a row. */
    fun onToggle(id: String) = flipped.update { if (id in it) it - id else it + id }

    /** «Удалить · N» (#628): removes every ticked draft and offers «Отменить» (they come back as they were). */
    fun delete() {
        if (state.value.deleteCount == 0) return
        val ids = state.value.rows.filter { it.checked }.map { it.id }
        viewModelScope.launch {
            val before = drafts.observeDrafts().first().filter { it.id in ids }
            drafts.deleteDrafts(ids)
            flipped.update { it - ids.toSet() }
            undo.offer(UndoOffer(R.string.drafts_deleted, listOf(before.size)) { drafts.saveDrafts(before) })
        }
    }

    fun post() {
        val ids = state.value.rows.filter { it.checked && it.ready }.map { it.id }
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val before = drafts.observeDrafts().first().filter { it.id in ids }
            val result = postDrafts(ids)
            if (result.transactionIds.isEmpty()) return@launch
            undo.offer(
                UndoOffer(R.string.drafts_posted, listOf(result.transactionIds.size)) {
                    result.transactionIds.forEach { transactions.deleteTransaction(it) }
                    drafts.saveDrafts(before)
                },
            )
        }
    }
}
