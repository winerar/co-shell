package app.coshell.reports

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.navigation.ReportCategoryDestination
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.core.domain.search.PeriodRange
import app.coshell.reports.category.reportCategoryDestination
import app.coshell.transactions.list.MemberPreset

/** Connects [ReportsScreen] to [ReportsViewModel]; «Все категории» shares the same ViewModel (the tab's entry). */
@Composable
fun ReportsRoute(
    onAllCategories: () -> Unit,
    /** «Из чего сложилось» of a bar's category (#188), in the shown period and currency. */
    onCategory: (ReportCategoryDestination) -> Unit,
    /** A row of «Кто сколько потратил» (#373): that member's operations in the shown period. */
    onMember: (MemberPreset) -> Unit = {},
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportsScreen(
        state,
        ReportsActions(
            onShift = viewModel::onShift,
            onApply = { viewModel.onApply(it.period, it.currency, it.compare) },
            onMonth = viewModel::onMonth,
            onAllCategories = onAllCategories,
            onCategory = { id -> state.drillTo(id)?.let(onCategory) },
            onMember = { id -> state.membersOperations(id)?.let(onMember) },
        ),
    )
}

/** The drill of [categoryId] in this report; `null` while the report is loading. */
internal fun ReportsUiState.drillTo(categoryId: String): ReportCategoryDestination? =
    period?.let { reportCategoryDestination(categoryId, it, startDay, currency, compare) }

/**
 * «Операции» of [memberId] (`null` — «Я») in this report's period; `null` while loading. A period that does not start on
 * the 1st (the family's month starts later) goes as days, so «Операции» names its real dates.
 */
internal fun ReportsUiState.membersOperations(memberId: String?): MemberPreset? = period?.let { p ->
    val step = if (p.start.day != 1) PeriodRange.Step.DAYS else when (p.kind) {
        ReportPeriodKind.MONTH -> PeriodRange.Step.MONTH
        ReportPeriodKind.QUARTER -> PeriodRange.Step.THREE_MONTHS
        ReportPeriodKind.YEAR -> PeriodRange.Step.YEAR
        ReportPeriodKind.CUSTOM -> PeriodRange.Step.DAYS
    }
    MemberPreset(memberId, p.start, p.endExclusive, step)
}
