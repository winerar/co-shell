package app.coshell.debts.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.R
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.debts.CloseDebtUseCase
import app.coshell.core.domain.debts.DebtBook
import app.coshell.core.domain.debts.DebtProblem
import app.coshell.core.domain.debts.DebtRepository
import app.coshell.core.domain.debts.DebtResult
import app.coshell.core.domain.debts.ReturnDebtUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.MoneyFormat
import app.coshell.navigation.DebtDetailDestination
import app.coshell.transactions.edit.parsePositiveAmount
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.format.formatDayMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** A debt's card (#552): what is left, the returns, «Вернули часть», «Закрыть долг» and «Удалить»; every write is an event. */
@HiltViewModel
class DebtDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val debts: DebtRepository,
    accountsRepo: AccountRepository,
    private val returnDebt: ReturnDebtUseCase,
    private val closeDebt: CloseDebtUseCase,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val debtId: String = checkNotNull(savedStateHandle[DebtDetailDestination.ARG_DEBT_ID])
    private val edit = MutableStateFlow(Edit())

    private data class Edit(val accountId: String? = null, val amount: String = "", val error: Int? = null, val done: Boolean = false)

    private var accounts: List<Account> = emptyList()

    val state: StateFlow<DebtDetailUiState> = combine(debts.observeEvents(), accountsRepo.observeAccounts(), edit) { events, accountList, e ->
        accounts = accountList.filter { !it.archived }
        val debt = DebtBook.of(events).firstOrNull { it.id == debtId } ?: return@combine DebtDetailUiState(found = false, loaded = true)
        val usable = accounts.filter { it.currency == debt.total.currency }
        val chosen = usable.firstOrNull { it.id == e.accountId } ?: usable.firstOrNull { it.id == debt.accountId } ?: usable.firstOrNull()
        DebtDetailUiState(
            loaded = true,
            lent = debt.direction == DebtDirection.LENT,
            person = debt.person,
            left = moneyFormat.format(debt.left),
            total = moneyFormat.format(debt.total),
            returned = moneyFormat.format(debt.returned),
            fraction = (debt.returned.amountMinor.toFloat() / debt.total.amountMinor.coerceAtLeast(1)).coerceIn(0f, 1f),
            dueText = debt.dueDate?.let(::formatDayMonth),
            note = debt.note,
            closed = debt.closed,
            returns = debt.returns.map {
                DebtReturnUi(it.id, formatDayMonth(it.date.toLocalDateTime(timeZone).date) + " · " + moneyFormat.format(it.amount))
            },
            currency = debt.total.currency.value,
            accounts = usable.map { ChoiceOption(it.id, it.name, accountName = it.name) },
            returnAccountId = chosen?.id,
            returnAccountName = chosen?.name,
            returnAmount = e.amount,
            returnError = e.error,
            returnDone = e.done,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebtDetailUiState())

    fun onReturnAccount(id: String) = edit.update { it.copy(accountId = id) }
    fun onReturnAmount(text: String) = edit.update { it.copy(amount = text, error = null) }
    fun resetReturn() = edit.update { Edit() }

    /** «Записать» in the «Вернули часть» sheet. */
    fun saveReturn() {
        val s = state.value
        val account = accounts.firstOrNull { it.id == s.returnAccountId }
        if (account == null) return edit.update { it.copy(error = R.string.debts_error_account) }
        val money = parsePositiveAmount(s.returnAmount, account.currency).money
            ?: return edit.update { it.copy(error = R.string.debts_error_amount) }
        viewModelScope.launch {
            when (val r = returnDebt(debtId, money, account.id, clock.now())) {
                is DebtResult.Saved -> edit.update { Edit(done = true) }
                is DebtResult.Invalid -> edit.update {
                    it.copy(
                        error = when (r.problem) {
                            DebtProblem.TOO_MUCH -> R.string.debts_error_too_much
                            DebtProblem.CLOSED, DebtProblem.NOT_FOUND -> R.string.debts_error_closed
                            DebtProblem.ACCOUNT, DebtProblem.CURRENCY -> R.string.debts_error_account
                            else -> R.string.debts_error_amount
                        },
                    )
                }
            }
        }
    }

    fun close() {
        viewModelScope.launch { closeDebt(debtId, clock.now()) }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            debts.deleteDebt(debtId)
            onDone()
        }
    }
}
