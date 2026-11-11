package app.coshell.templates.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.templates.SaveTemplateUseCase
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TemplateEditDestination
import app.coshell.templates.edit.TemplateEditUiState.AccountChoice
import app.coshell.templates.edit.TemplateEditUiState.CategoryChoice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** New or existing template (mockup `TplEdit`). Validation — [buildTemplate]; this class loads, keeps the state, saves. */
@HiltViewModel
class TemplateEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val templates: TemplateRepository,
    private val saveTemplate: SaveTemplateUseCase,
    private val ids: IdGenerator,
    private val moneyFormat: MoneyFormat,
) : ViewModel() {

    private val templateId: String? = savedStateHandle[TemplateEditDestination.ARG_TEMPLATE_ID]
    private var existing: TransactionTemplate? = null
    private var accountsById: Map<String, Account> = emptyMap()

    private val _state = MutableStateFlow(TemplateEditUiState(isLoading = true, isNew = templateId == null))
    val state: StateFlow<TemplateEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val accountList = accounts.observeAccounts().first()
        accountsById = accountList.associateBy { it.id }
        val categoryList = categories.observeCategories().first()
        val template = templateId?.let { templates.getTemplate(it) }
        existing = template
        val base = TemplateEditUiState(
            isNew = template == null,
            accounts = accountList.map { AccountChoice(it.id, it.name, it.currency.value) },
            categories = categoryList.toChoices(),
        )
        _state.value = if (template == null) {
            base.copy(accountId = accountList.firstOrNull()?.id)
        } else {
            // An account or category archived since is left empty: the form asks to choose again.
            val account = accountsById[template.accountId]?.takeIf { it.currency == template.currency }
            base.copy(
                type = template.type,
                name = template.name,
                accountId = account?.id,
                toAccountId = template.toAccountId?.takeIf { it in accountsById },
                amount = template.amount?.takeIf { account != null }?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
                categoryId = template.categoryId?.takeIf { id -> base.categories.any { it.id == id } },
                note = template.note,
            )
        }
    }

    fun onTypeChange(type: TransactionType) = _state.update {
        val next = it.copy(type = type, toAccountError = false)
        next.copy(categoryId = next.categoryId?.takeIf { id -> next.categoryChoices.any { choice -> choice.id == id } })
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = false) }

    fun onAccountSelect(id: String) = _state.update { it.copy(accountId = id, accountError = false, amountError = false) }

    fun onToAccountSelect(id: String) = _state.update { it.copy(toAccountId = id, toAccountError = false) }

    fun onAmountChange(value: String) = _state.update { it.copy(amount = value, amountError = false) }

    /** `null` — «Не выбрана»: the entry sheet will ask. */
    fun onCategorySelect(id: String?) = _state.update { it.copy(categoryId = id) }

    fun onNoteChange(value: String) = _state.update { it.copy(note = value) }

    fun save() {
        val current = _state.value
        if (current.isLoading || current.isDone) return
        viewModelScope.launch {
            val sortOrder = existing?.sortOrder ?: ((templates.maxSortOrder() ?: -1) + 1)
            when (val result = buildTemplate(current, accountsById, existing?.id ?: ids.newId(), sortOrder)) {
                is TemplateFormResult.Invalid -> _state.update {
                    it.copy(
                        nameError = result.nameError,
                        accountError = result.accountError,
                        toAccountError = result.toAccountError,
                        amountError = result.amountError,
                    )
                }
                is TemplateFormResult.Valid -> when (val saved = saveTemplate(result.template)) {
                    SaveTemplateUseCase.Result.Saved -> _state.update { it.copy(isDone = true) }
                    is SaveTemplateUseCase.Result.Invalid -> _state.update { it.withError(saved.reason) }
                }
            }
        }
    }

    fun delete() {
        val template = existing ?: return
        viewModelScope.launch {
            templates.deleteTemplate(template.id)
            _state.update { it.copy(isDone = true) }
        }
    }

    /** Refusals the form could not foresee: something was archived on the other phone meanwhile. */
    private fun TemplateEditUiState.withError(reason: SaveTemplateUseCase.Reason) = when (reason) {
        SaveTemplateUseCase.Reason.ACCOUNT_NOT_FOUND,
        SaveTemplateUseCase.Reason.CURRENCY_MISMATCH,
        -> copy(accountError = true)
        SaveTemplateUseCase.Reason.TARGET_ACCOUNT_NOT_FOUND -> copy(toAccountError = true)
        SaveTemplateUseCase.Reason.CATEGORY_NOT_FOUND,
        SaveTemplateUseCase.Reason.CATEGORY_TYPE_MISMATCH,
        -> copy(categoryId = null)
    }

    /** Folders first with their subcategories right under them, as «Папка / Категория». */
    private fun List<Category>.toChoices(): List<CategoryChoice> {
        val byId = associateBy { it.id }
        return map { category ->
            val parent = category.parentId?.let { byId[it] }
            CategoryChoice(category.id, if (parent != null) "${parent.name} / ${category.name}" else category.name, category.type)
        }.sortedBy { it.label.lowercase() }
    }
}
