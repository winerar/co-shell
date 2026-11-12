package app.coshell.budgets.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.budgets.RemoveBudgetUseCase
import app.coshell.core.domain.budgets.SaveBudgetUseCase
import app.coshell.core.domain.budgets.budgetCategoryIds
import app.coshell.core.domain.budgets.forCategory
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
import app.coshell.navigation.BudgetEditDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

/**
 * Limit editor (`BudEdit`): what the limit is on, the amount and its currency with the 3-month average,
 * rollover, a note; «Сохранить» and «Убрать лимит». Changes apply from [fromMonth] on (`SaveBudgetUseCase`).
 * Choosing a category that already has a limit fills the form with it. Pure parts — `BudgetForm.kt`.
 */
@HiltViewModel
class BudgetEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val source: BudgetSource,
    private val accounts: AccountRepository,
    private val reportCurrencies: ReportCurrencySettings,
    private val saveBudget: SaveBudgetUseCase,
    private val removeBudget: RemoveBudgetUseCase,
    moneyFormat: MoneyFormat,
) : ViewModel() {

    private val categoryArg: String? = savedStateHandle[BudgetEditDestination.ARG_CATEGORY_ID]
    private val fromMonth = YearMonth(
        checkNotNull(savedStateHandle.get<Int>(BudgetEditDestination.ARG_YEAR)),
        Month(checkNotNull(savedStateHandle.get<Int>(BudgetEditDestination.ARG_MONTH))),
    )
    private val format = moneyFormat.copy(trimZeroFraction = true)
    private var snapshot: BudgetSource.Snapshot? = null

    private val _state = MutableStateFlow(BudgetEditUiState(isLoading = true, month = fromMonth, locked = categoryArg != null))
    val state: StateFlow<BudgetEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val data = source.observe().first().also { snapshot = it }
        val (folders, plain) = budgetChoices(data.categories)
        val accountCurrencies = accounts.observeAccounts().first().map { it.currency }
        val existing = categoryArg?.let { data.budgets.forCategory(it, fromMonth) }
        val base = BudgetEditUiState(
            month = fromMonth,
            locked = categoryArg != null,
            kind = if (categoryArg != null && plain.any { it.id == categoryArg }) BudgetKind.CATEGORY else BudgetKind.FOLDER,
            folders = folders,
            categories = plain,
            currency = data.mainCurrency.value,
            currencies = limitCurrencies(reportCurrencies.currencies.first(), accountCurrencies, existing?.limit?.currency),
        )
        // A family without folders starts on «Категория»: «Папка» would be an empty list.
        val start = if (categoryArg == null && folders.isEmpty()) base.copy(kind = BudgetKind.CATEGORY) else base
        _state.value = categoryArg?.let { start.withCategory(it) } ?: start
    }

    fun onKindChange(kind: BudgetKind) = _state.update {
        if (it.locked || it.kind == kind) it else it.copy(kind = kind, categoryId = null, inside = emptyList(), average = null, hasLimit = false)
    }

    fun onCategorySelect(id: String) = _state.update { it.withCategory(id).copy(categoryError = false) }

    fun onAmountChange(value: String) = _state.update { it.copy(amount = value, amountError = false) }

    fun onCurrencySelect(code: String) = _state.update { it.copy(currency = code, amountError = false).withAverage() }

    fun onRolloverChange(value: Boolean) = _state.update { it.copy(rollover = value) }

    fun onNoteChange(value: String) = _state.update { it.copy(note = value) }

    fun save() {
        val current = _state.value
        if (current.isLoading || current.isDone) return
        val categoryId = current.categoryId
        val limit = parseLimit(current.amount, current.currency)
        if (categoryId == null || limit == null) {
            _state.update { it.copy(categoryError = categoryId == null, amountError = limit == null) }
            return
        }
        viewModelScope.launch {
            when (saveBudget(categoryId, fromMonth, limit, current.note, current.rollover)) {
                is SaveBudgetUseCase.Result.Saved -> _state.update { it.copy(isDone = true) }
                is SaveBudgetUseCase.Result.Invalid -> _state.update { it.copy(saveFailed = true) }
            }
        }
    }

    fun remove() {
        val categoryId = _state.value.categoryId ?: return
        viewModelScope.launch {
            removeBudget(categoryId, fromMonth)
            _state.update { it.copy(isDone = true) }
        }
    }

    /** Picks [id]; its limit in the month (if any) fills the form, otherwise the typed amount stays. */
    private fun BudgetEditUiState.withCategory(id: String): BudgetEditUiState {
        val data = snapshot ?: return this
        val existing = data.budgets.forCategory(id, fromMonth)
        val picked = copy(
            categoryId = id,
            hasLimit = existing != null,
            inside = if (kind == BudgetKind.FOLDER) insideFolder(id, data.categories, data.budgets, fromMonth, format) else emptyList(),
            saveFailed = false,
        )
        val filled = existing?.let {
            picked.copy(amount = format.formatForEditing(it.limit), currency = it.limit.currency.value, rollover = it.rollover, note = it.note.orEmpty())
        } ?: picked
        return filled.withAverage()
    }

    private fun BudgetEditUiState.withAverage(): BudgetEditUiState {
        val data = snapshot ?: return this
        val id = categoryId ?: return copy(average = null)
        val code = CurrencyCode.parseOrNull(currency) ?: return copy(average = null)
        val average = data.ledger.averageSpent(budgetCategoryIds(id, data.categories), fromMonth, code, data.startDay)
        return copy(average = average?.let(format::format))
    }
}
