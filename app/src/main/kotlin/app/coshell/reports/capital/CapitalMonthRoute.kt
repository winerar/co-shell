package app.coshell.reports.capital

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.navigation.CapitalMonthDestination
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

/** Connects [CapitalMonthScreen] to [CapitalMonthViewModel]. */
@Composable
fun CapitalMonthRoute(
    onBack: () -> Unit,
    onAccount: (String) -> Unit,
    viewModel: CapitalMonthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CapitalMonthScreen(
        state,
        CapitalMonthActions(
            onBack = onBack,
            onMonth = viewModel::onMonth,
            onQuery = viewModel::onQuery,
            onOrder = viewModel::onOrder,
            onToggleGroup = viewModel::onToggleGroup,
            onAccount = onAccount,
        ),
    )
}

/** The route to [month]'s accounts; the line ends at [last] — the last month of the report it was opened from. */
fun capitalMonthDestination(month: YearMonth, last: YearMonth, startDay: Int, currency: String) =
    CapitalMonthDestination(month.year, month.month.number, last.year, last.month.number, startDay, currency)
