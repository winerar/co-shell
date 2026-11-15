package app.coshell.reports

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [ReportsScreen] to [ReportsViewModel]; «Все категории» shares the same ViewModel (the tab's entry). */
@Composable
fun ReportsRoute(onAllCategories: () -> Unit, viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportsScreen(
        state,
        ReportsActions(
            onShift = viewModel::onShift,
            onApply = { viewModel.onApply(it.period, it.currency, it.compare) },
            onMonth = viewModel::onMonth,
            onAllCategories = onAllCategories,
        ),
    )
}
