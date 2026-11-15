package app.coshell.reports.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.navigation.ReportCategoryDestination
import app.coshell.reports.category.reportCategoryDestination

/** Connects [MonthReviewScreen] to [MonthReviewViewModel]; a change opens «Из чего сложилось» of the reviewed month. */
@Composable
fun MonthReviewRoute(
    onBack: () -> Unit,
    onCategory: (ReportCategoryDestination) -> Unit,
    onLimit: (categoryId: String) -> Unit,
    onAllLimits: () -> Unit,
    viewModel: MonthReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = MonthReviewActions(
        onBack = onBack,
        onChange = { id ->
            state.month?.let { month ->
                val period = ReportPeriod.month(month, state.startDay)
                onCategory(reportCategoryDestination(id, period, state.startDay, state.currency, compare = true))
            }
        },
        onLimit = onLimit,
        onAllLimits = onAllLimits,
    )
    MonthReviewScreen(state, actions)
}

/** «Лимиты октября» on the ViewModel of the review under it. */
@Composable
fun MonthLimitsRoute(viewModel: MonthReviewViewModel, onBack: () -> Unit, onLimit: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MonthLimitsScreen(state, onBack, onLimit)
}
