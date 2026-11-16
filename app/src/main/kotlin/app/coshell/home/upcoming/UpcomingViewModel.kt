package app.coshell.home.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.recurring.MissedPaymentsUseCase
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.MoneyFormat
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * «Скоро · 7 дней» on «Обзоре» (#143). Apart from `HomeViewModel`: a family without regular payments
 * only reads its templates. The day is fixed when the screen subscribes, like the rest of «Обзор».
 */
@HiltViewModel
class UpcomingViewModel @Inject constructor(
    templates: TemplateRepository,
    accounts: AccountRepository,
    categories: CategoryRepository,
    moneyFormat: MoneyFormat,
    clock: Clock,
    private val timeZone: TimeZone,
    private val missed: MissedPaymentsUseCase,
) : ViewModel() {

    private val today = clock.todayIn(timeZone)

    val rows: StateFlow<List<UpcomingRow>> = combine(
        templates.observeTemplates(),
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
    ) { list, accountList, categoryList ->
        buildUpcomingRows(list, accountList.associateBy { it.id }, categoryList.associateBy { it.id }, moneyFormat, today)
    }
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
