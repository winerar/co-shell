package app.coshell.debts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.debts.DebtBook
import app.coshell.core.domain.debts.DebtRepository
import app.coshell.core.model.Debt
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.MoneyFormat
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** «Долги» (#552): two lists — «Мне должны» and «Я должен» — with totals per currency; closed debts are folded away. */
@HiltViewModel
class DebtsViewModel @Inject constructor(
    debts: DebtRepository,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val tab = MutableStateFlow(0)
    private val closedShown = MutableStateFlow(false)

    val state: StateFlow<DebtsUiState> = combine(debts.observeEvents(), tab, closedShown) { events, tabIndex, showClosed ->
        val book = DebtBook.of(events)
        val direction = if (tabIndex == 0) DebtDirection.LENT else DebtDirection.BORROWED
        val today = clock.now().toLocalDateTime(timeZone).date
        val dues = DebtBook.due(book, today).associateBy { it.debt.id }
        fun row(debt: Debt): DebtRowUi {
            val due = dues[debt.id]
            val kind = when {
                debt.dueDate == null -> DebtDue.NONE
                debt.closed -> DebtDue.SET
                due?.stage == DebtBook.Stage.LATE -> DebtDue.LATE
                due?.stage == DebtBook.Stage.TODAY -> DebtDue.TODAY
                due?.stage == DebtBook.Stage.SOON -> DebtDue.SOON
                else -> DebtDue.SET
            }
            val total = debt.total.amountMinor.coerceAtLeast(1)
            return DebtRowUi(
                id = debt.id,
                person = debt.person,
                left = moneyFormat.format(debt.left),
                ofTotal = if (debt.returned.amountMinor > 0) moneyFormat.format(debt.total) else "",
                returnedFraction = (debt.returned.amountMinor.toFloat() / total).coerceIn(0f, 1f),
                due = kind,
                dueDate = debt.dueDate?.let(::formatDayMonth).orEmpty(),
                days = due?.days ?: 0,
                note = debt.note,
                closed = debt.closed,
            )
        }
        DebtsUiState(
            isLoading = false,
            tab = tabIndex,
            totals = DebtBook.totals(book, direction).map(moneyFormat::format),
            active = DebtBook.active(book, direction).map(::row),
            closed = DebtBook.closed(book, direction).map(::row),
            closedShown = showClosed,
            nothingAtAll = book.isEmpty(),
        )
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebtsUiState())

    fun selectTab(index: Int) = tab.update { index }

    fun toggleClosed() = closedShown.update { !it }
}
