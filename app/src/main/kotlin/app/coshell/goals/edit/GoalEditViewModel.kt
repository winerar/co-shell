package app.coshell.goals.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.designsystem.components.RibbonAccount
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.goals.GoalBook
import app.coshell.core.domain.goals.GoalRepository
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.Goal
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.navigation.GoalEditDestination
import app.coshell.transactions.edit.parsePositiveAmount
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.format.formatDayMonthYear
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/** The goal sheet (#768): a new goal or an existing one; the plan a month is recounted on every change ([GoalBook]). */
@HiltViewModel
class GoalEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goals: GoalRepository,
    private val accountsRepo: AccountRepository,
    private val ids: IdGenerator,
    moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val goalId: String? = savedStateHandle[GoalEditDestination.ARG_GOAL_ID]
    private val format = moneyFormat.copy(trimZeroFraction = true)
    private val form = MutableStateFlow(GoalEditUiState(isNew = goalId == null))
    val state: StateFlow<GoalEditUiState> = form.asStateFlow()

    private var original: Goal? = null
    private var balances: List<AccountWithBalance> = emptyList()
    private var others: List<Goal> = emptyList()

    init {
        viewModelScope.launch {
            balances = accountsRepo.observeAccountsWithBalance(includeArchived = true).first()
            val all = goals.observe().first()
            original = goalId?.let { id -> all.firstOrNull { it.id == id } }
            others = all.filter { it.id != goalId }
            val goal = original
            val shown = balances.filter { !it.account.archived || it.account.id == goal?.accountId }.map { it.account }
            form.update {
                it.copy(
                    isLoading = false,
                    ribbon = shown.map { a -> RibbonAccount(a.id, a.name, a.color) },
                    accounts = shown.map { a -> ChoiceOption(a.id, a.name, a.currency.value, accountName = a.name) },
                ).let { s ->
                    if (goal == null) s.withAccount(shown.firstOrNull()?.id)
                    else s.copy(
                        name = goal.name,
                        amount = format.formatForEditing(goal.target),
                        yearly = goal.yearly,
                    ).withAccount(goal.accountId).withDeadline(goal.deadline)
                }.recount()
            }
        }
    }

    fun onName(text: String) = form.update { it.copy(name = text, errors = it.errors - GoalError.NAME) }
    fun onAmount(text: String) = form.update { it.copy(amount = text, errors = it.errors - GoalError.TARGET).recount() }
    fun onAccount(id: String) = form.update { it.withAccount(id).recount() }
    fun onYearly(on: Boolean) = form.update { it.copy(yearly = on, errors = it.errors - GoalError.YEARLY).recount() }

    fun onDeadline(millisUtc: Long?) = form.update {
        it.withDeadline(millisUtc?.let { m -> Instant.fromEpochMilliseconds(m).toLocalDateTime(TimeZone.UTC).date }).recount()
    }

    private fun GoalEditUiState.withAccount(id: String?): GoalEditUiState {
        val account = balances.firstOrNull { it.account.id == id }?.account
        return copy(accountId = account?.id, currency = account?.currency?.value, errors = errors - GoalError.ACCOUNT)
    }

    private fun GoalEditUiState.withDeadline(day: LocalDate?): GoalEditUiState = copy(
        deadlineMillisUtc = day?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
        deadlineText = day?.let(::formatDayMonthYear),
        errors = if (day != null) errors - GoalError.YEARLY else errors,
    )

    private fun GoalEditUiState.deadline(): LocalDate? =
        deadlineMillisUtc?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date }

    /** This goal as typed, or null while the amount or the account is missing. */
    private fun GoalEditUiState.draft(): Goal? {
        val account = balances.firstOrNull { it.account.id == accountId }?.account ?: return null
        val money = parsePositiveAmount(amount, account.currency).money ?: return null
        val day = deadline()
        return Goal(goalId ?: DRAFT_ID, name.ifBlank { DRAFT_ID }, money, account.id, day, yearly && day != null, original?.icon, original?.color)
    }

    /** The plan and the progress, counted together with the other goals that share the account. */
    private fun GoalEditUiState.recount(): GoalEditUiState {
        val goal = draft() ?: return copy(planPerMonth = "", planDeadline = "", planReached = false, topUpMinor = 0)
        val today = clock.todayIn(timeZone)
        val line = GoalBook.of(others + goal, balances.associate { it.account.id to it.balance }, today).first { it.goal.id == goal.id }
        val p = line.progress
        val target = goal.target.amountMinor
        return copy(
            planPerMonth = p.perMonth?.let(format::format).orEmpty(),
            planDeadline = p.deadline?.let { if (it.year == today.year) formatDayMonth(it) else formatDayMonthYear(it) }.orEmpty(),
            planReached = p.reached,
            progress = if (original == null) null else GoalProgressUi(
                saved = format.format(p.saved),
                target = format.format(goal.target),
                left = format.format(p.left),
                fraction = (p.saved.amountMinor.toFloat() / target).coerceIn(0f, 1f),
                reached = p.reached,
            ),
            topUpMinor = p.perMonth?.amountMinor ?: p.left.amountMinor,
        )
    }

    fun save() {
        val f = form.value
        val account = balances.firstOrNull { it.account.id == f.accountId }?.account
        val money: Money? = account?.let { parsePositiveAmount(f.amount, it.currency).money }
        val day = f.deadline()
        val errors = buildSet {
            if (f.name.isBlank()) add(GoalError.NAME)
            if (money == null) add(GoalError.TARGET)
            if (account == null) add(GoalError.ACCOUNT)
            if (f.yearly && day == null) add(GoalError.YEARLY)
        }
        form.update { it.copy(errors = errors) }
        if (errors.isNotEmpty() || account == null || money == null) return
        val goal = Goal(goalId ?: ids.newId(), f.name.trim(), money, account.id, day, f.yearly, original?.icon, original?.color)
        viewModelScope.launch {
            goals.save(goal)
            form.update { it.copy(isDone = true) }
        }
    }

    fun delete() {
        val id = goalId ?: return
        viewModelScope.launch {
            goals.delete(id)
            form.update { it.copy(isDone = true) }
        }
    }

    private companion object {
        const val DRAFT_ID = "draft"
    }
}
