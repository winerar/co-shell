package app.coshell.reports

import app.coshell.core.designsystem.motion.listItemMotion
import app.coshell.hints.hintSlotShown
import app.coshell.hints.HintSlot
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.rememberSegmentSwipe
import app.coshell.core.designsystem.components.segmentSwipe
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.R as DesignR
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.domain.hints.HintId
import app.coshell.hints.LocalHintHost
import app.coshell.hints.WordHint
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.components.PeriodSwitch
import app.coshell.reports.components.ReportChoice
import app.coshell.reports.components.ReportPeriodSheet
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import kotlinx.datetime.LocalDate
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
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val swipe = rememberSegmentSwipe()
    val list = rememberLazyListState()
    // The period row went under the header: its title and arrows move into the capsule (#568, poll «Б»).
    val periodGone by remember { derivedStateOf { list.periodGone() } }
    Scaffold(
        topBar = {
            val inBar = periodGone && period != null
            TabTopBar(
                stringResource(R.string.reports_title),
                scroll,
                subtitle = if (inBar && period != null) stringResource(R.string.reports_bar_period, periodTitle(period), state.currency) else null,
                navigation = { BarArrow(inBar, next = false) { actions.onShift(-1) } },
                onTitleClick = if (inBar) ({ sheetOpen = true }) else null,
                actions = { BarArrow(inBar, next = true) { actions.onShift(1) } },
            )
        },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        val hintsOn = LocalHintHost.current != null
        val showHint = hintSlotShown()
        LazyColumn(
            Modifier.fillMaxSize().segmentSwipe(swipe, tab, 2, onSelect = { tab = it }),
            state = list,
            contentPadding = underBars(padding, PaddingValues(start = Spacing.l, end = Spacing.l, bottom = Sizes.fabClearance)),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            if (period == null) return@LazyColumn
            if (showHint) item(key = "hint") { HintSlot(listItemMotion()) }
            if (hintsOn) item(key = "word-hint") { WordHint(HintId.REPORTS, R.string.hint_reports) }
            item(key = "tabs") {
                CoShellSegmented(
                    listOf(stringResource(R.string.reports_tab_overview), stringResource(R.string.reports_tab_calendar)),
                    selected = tab,
                    onSelect = { tab = it },
                    swipe = swipe,
                )
            }
            item(key = PERIOD_KEY) {
                PeriodSwitch(
                    title = periodTitle(period),
                    currency = state.currency,
                    onPrevious = { actions.onShift(-1) },
                    onNext = { actions.onShift(1) },
                    onOpenSheet = { sheetOpen = true },
                )
            }
            if (tab == 1) calendarItems(state, actions) else overviewItems(state, period, actions)
        }
    }
}

/** Whether the «period» row is above the list's top edge (under the header capsule). */
private fun LazyListState.periodGone(): Boolean {
    val row = layoutInfo.visibleItemsInfo.firstOrNull { it.key == PERIOD_KEY }
    return if (row == null) firstVisibleItemIndex > 0 else row.offset + row.size <= 0
}

/** ‹ or › in the header; fades in and out with the period line. */
@Composable
private fun BarArrow(shown: Boolean, next: Boolean, onClick: () -> Unit) {
    AnimatedContent(shown, transitionSpec = { fadeIn(Motion.effects()).togetherWith(fadeOut(Motion.effectsFast())) }, label = "barArrow") { on ->
        if (on) {
            CoShellIconButton(onClick = onClick) {
                Icon(
                    painterResource(DesignR.drawable.ic_chevron_right),
                    stringResource(if (next) R.string.reports_next else R.string.reports_previous),
                    if (next) Modifier else Modifier.rotate(HALF_TURN),
                )
            }
        }
    }
}

private const val PERIOD_KEY = "period"
private const val HALF_TURN = 180f

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
    /** A day of «Календарь трат» (#553). */
    val onDay: (LocalDate) -> Unit = {},
    /** A row of the tapped day: the operation's id. */
    val onTransaction: (String) -> Unit = {},
)
