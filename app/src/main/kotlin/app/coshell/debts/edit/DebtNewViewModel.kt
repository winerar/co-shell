package app.coshell.debts.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.debts.DebtProblem
import app.coshell.core.domain.debts.DebtResult
import app.coshell.core.domain.debts.IssueDebtUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.DebtDirection
import app.coshell.transactions.edit.parsePositiveAmount
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.format.formatDayMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** «Новый долг» (#552): checks the form, then [IssueDebtUseCase] writes the event and moves the account. */
@HiltViewModel
class DebtNewViewModel @Inject constructor(
    private val accountsRepo: AccountRepository,
    private val issue: IssueDebtUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val form = MutableStateFlow(DebtNewUiState())
    private var accounts: List<Account> = emptyList()
    val state: StateFlow<DebtNewUiState> = form.asStateFlow()

    init {
        viewModelScope.launch {
            accounts = accountsRepo.observeAccounts().first().filter { !it.archived }
            val first = accounts.firstOrNull()
            form.update {
                it.copy(accounts = accounts.map { a -> ChoiceOption(a.id, a.name, a.currency.value, accountName = a.name) }).withAccount(first)
            }
        }
    }

    private fun DebtNewUiState.withAccount(account: Account?) =
        copy(accountId = account?.id, accountName = account?.name, currency = account?.currency?.value, accountError = false)

    fun onDirection(index: Int) = form.update { it.copy(direction = index) }
    fun onPerson(text: String) = form.update { it.copy(person = text, personError = false) }
    fun onAmount(text: String) = form.update { it.copy(amount = text, amountError = false) }
    fun onNote(text: String) = form.update { it.copy(note = text) }
    fun onAccount(id: String) = form.update { it.withAccount(accounts.firstOrNull { a -> a.id == id }) }

    fun onDue(millisUtc: Long?) = form.update {
        val text = millisUtc?.let { m -> formatDayMonth(Instant.fromEpochMilliseconds(m).toLocalDateTime(TimeZone.UTC).date) }
        it.copy(dueMillisUtc = millisUtc, dueText = text)
    }

    fun save() {
        val f = form.value
        val account = accounts.firstOrNull { it.id == f.accountId }
        val money = account?.let { parsePositiveAmount(f.amount, it.currency).money }
        form.update { it.copy(personError = f.person.isBlank(), amountError = money == null, accountError = account == null) }
        if (account == null || money == null || f.person.isBlank()) return
        viewModelScope.launch {
            val due = f.dueMillisUtc?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date }
            val direction = if (f.direction == 0) DebtDirection.LENT else DebtDirection.BORROWED
            when (val result = issue(direction, f.person, money, account.id, clock.now(), due, f.note)) {
                is DebtResult.Saved -> form.update { it.copy(isDone = true) }
                is DebtResult.Invalid -> form.update {
                    it.copy(
                        personError = result.problem == DebtProblem.PERSON,
                        amountError = result.problem == DebtProblem.AMOUNT,
                        accountError = result.problem == DebtProblem.ACCOUNT || result.problem == DebtProblem.CURRENCY,
                    )
                }
            }
        }
    }
}
