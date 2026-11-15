package app.coshell.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.components.CapitalCard
import app.coshell.reports.components.IncomeCard
import app.coshell.reports.components.MembersCard
import app.coshell.reports.components.MonthsCard
import app.coshell.reports.components.PeriodSwitch
import app.coshell.reports.components.ReportChoice
import app.coshell.reports.components.ReportPeriodSheet
import app.coshell.reports.components.SpendingCard
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import kotlinx.datetime.YearMonth

/**
 * «Отчёты» (#140, boards `RepA`, `RepPeriod`): a feed of cards for one period and one currency — «На что ушли деньги»,
 * «По месяцам», «Доходы и расходы», «Капитал» and «Кто сколько потратил» (#189). The arrows step through periods; the title or «USD ▾» opens «Период и валюта»; a month column
 * opens that month.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(state: ReportsUiState, actions: ReportsActions) {
    val scroll = rememberTabScrollBehavior()
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    val period = state.period
    if (sheetOpen && period != null && state.today != null) {
        ReportPeriodSheet(
            initial = ReportChoice(period, state.currency, state.compare),
            currencies = state.currencies,
            startDay = state.startDay,
            today = state.today,
            onApply = actions.onApply,
            onDismiss = { sheetOpen = false },
        )
    }
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.reports_title), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, bottom = Sizes.fabClearance),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            if (period == null) return@LazyColumn
            item(key = "period") {
                PeriodSwitch(
                    title = periodTitle(period),
                    currency = state.currency,
                    onPrevious = { actions.onShift(-1) },
                    onNext = { actions.onShift(1) },
                    onOpenSheet = { sheetOpen = true },
                )
            }
            val spending = state.spending
            if (spending == null) {
                item(key = "empty") { Empty(stringResource(R.string.reports_period_title)) }
            } else {
                item(key = "spending") {
                    SpendingCard(
                        spending,
                        state.currency,
                        ghostCaption = if (state.compare) ghostCaption(period, state.startDay) else null,
                        onAll = actions.onAllCategories,
                        onBar = actions.onCategory,
                    )
                }
            }
            state.months?.let { months -> item(key = "months") { MonthsCard(months, onMonth = actions.onMonth) } }
            state.income?.let { income -> item(key = "income") { IncomeCard(income, onMonth = actions.onMonth) } }
            state.capital?.let { capital -> item(key = "capital") { CapitalCard(capital, state.currency, onPoint = actions.onCapital) } }
            if (state.members.isNotEmpty()) item(key = "members") { MembersCard(state.members, onMember = actions.onMember) }
            item(key = "note") {
                val missing = (spending?.missing.orEmpty() + state.capital?.missing.orEmpty()).distinct().sorted()
                CoShellHint(
                    if (missing.isEmpty()) stringResource(R.string.reports_rate_note, state.currency)
                    else stringResource(R.string.reports_rate_note, state.currency) + ". " + stringResource(R.string.reports_missing, missing.joinToString()),
                )
            }
        }
    }
}

@Composable
private fun Empty(sheetTitle: String) {
    CoShellCard {
        Text(
            stringResource(R.string.reports_empty, sheetTitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.l),
        )
    }
}

/** Everything the screen can ask for; defaults make previews short. */
data class ReportsActions(
    val onShift: (Int) -> Unit = {},
    val onApply: (ReportChoice) -> Unit = {},
    val onMonth: (YearMonth) -> Unit = {},
    val onAllCategories: () -> Unit = {},
    /** A bar of «На что ушли деньги»: what its category is made of. */
    val onCategory: (String) -> Unit = {},
    /** A row of «Кто сколько потратил»: the member's id, `null` — «Я». */
    val onMember: (String?) -> Unit = {},
    /** A point of «Капитал»: the accounts at that month end. */
    val onCapital: (YearMonth) -> Unit = {},
)
