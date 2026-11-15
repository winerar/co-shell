package app.coshell.reports.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [ReportCategoryScreen] to [ReportCategoryViewModel]. */
@Composable
fun ReportCategoryRoute(
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: ReportCategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportCategoryScreen(state, onBack = onBack, onTransactionClick = onTransactionClick)
}
