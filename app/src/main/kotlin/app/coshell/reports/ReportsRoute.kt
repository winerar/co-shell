package app.coshell.reports

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.navigation.ReportCategoryDestination
import app.coshell.reports.category.reportCategoryDestination

/** Connects [ReportsScreen] to [ReportsViewModel]; «Все категории» shares the same ViewModel (the tab's entry). */
@Composable
fun ReportsRoute(
    onAllCategories: () -> Unit,
    /** «Из чего сложилось» of a bar's category (#188), in the shown period and currency. */
    onCategory: (ReportCategoryDestination) -> Unit,
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
        ),
    )
}

/** The drill of [categoryId] in this report; `null` while the report is loading. */
internal fun ReportsUiState.drillTo(categoryId: String): ReportCategoryDestination? =
    period?.let { reportCategoryDestination(categoryId, it, startDay, currency, compare) }
