package app.coshell.reports.all

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.navigation.ReportCategoryDestination
import app.coshell.reports.ReportsViewModel
import app.coshell.reports.drillTo

/** «Все категории» on the ViewModel of the «Отчёты» tab it was opened from: the same period, currency and data. */
@Composable
fun ReportCategoriesRoute(viewModel: ReportsViewModel, onBack: () -> Unit, onCategory: (ReportCategoryDestination) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportCategoriesScreen(state, onBack, onCategory = { id -> state.drillTo(id)?.let(onCategory) })
}
