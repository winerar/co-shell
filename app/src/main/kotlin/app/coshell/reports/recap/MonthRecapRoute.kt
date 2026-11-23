package app.coshell.reports.recap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [MonthRecapScreen] to [MonthRecapViewModel]; «Подробнее» on the last story opens the review of the month. */
@Composable
fun MonthRecapRoute(onBack: () -> Unit, onDetails: () -> Unit, viewModel: MonthRecapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MonthRecapScreen(state, onBack, onDetails)
}
