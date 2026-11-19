package app.coshell.home

import app.coshell.converter.ConverterEntry
import app.coshell.core.domain.hints.FirstStepsState
import app.coshell.hints.FirstStepsRow
import app.coshell.hints.FirstStepsSheet
import app.coshell.hints.HintsViewModel
import app.coshell.home.components.EmptyTotalCard
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.LocalBottomBarInset
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellEmptyState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.remember
import app.coshell.core.domain.settings.HomeLayout
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.itemsIndexed
import app.coshell.core.domain.settings.HomeCard
import app.coshell.settings.LocalScreenOptions
import app.coshell.settings.ScreenOptionsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.motion.listItemMotion
import app.coshell.core.designsystem.motion.staggeredEntrance
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.budget.HomeBudget
import app.coshell.home.budget.HomeBudgetViewModel
import app.coshell.home.components.HomeTiles
import app.coshell.home.components.HotBudgets
import app.coshell.home.components.MonthReviewCard
import app.coshell.home.components.RecentCard
import app.coshell.home.components.SyncBanner
import app.coshell.home.components.VersionGateBanner
import app.coshell.home.components.VersionGateBannerState
import app.coshell.gate.LocalInputLock
import app.coshell.home.components.SyncBannerActions
import app.coshell.home.components.TotalCard
import app.coshell.home.sync.SyncBannerState
import app.coshell.home.sync.SyncBannerViewModel
import app.coshell.reports.review.MonthReviewUiState
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import app.coshell.home.upcoming.UpcomingCard
import app.coshell.home.upcoming.UpcomingRow
import app.coshell.home.upcoming.UpcomingViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Everything «Обзор» can open: «Операции», the rates screen, an operation, a new account. */
data class HomeActions(
    val onTransactions: () -> Unit = {},
    val onRates: () -> Unit = {},
    val onConverter: () -> Unit = {},
    /** «Без категории» → «Операции» with the «Без категории · Этот месяц» filter. */
    val onUncategorized: () -> Unit = {},
    val onTransactionClick: (String) -> Unit = {},
    val onAddAccount: () -> Unit = {},
    /** «Все счета ›» on the balance card → «Счета». */
    val onAccounts: () -> Unit = {},
    /** Sync banner: «Войти» → «Семья», «Отзыв» → feedback with the error code typed in. */
    val onFamily: () -> Unit = {},
    val onFeedback: (prefill: String) -> Unit = {},
    /** The budget tile → «Бюджеты»; an envelope of «Горит в бюджете» → its screen in the current budget month. */
    val onBudgets: () -> Unit = {},
    val onBudget: (categoryId: String, month: YearMonth) -> Unit = { _, _ -> },
    /** «Итог октября» → its screen (#236). */
    val onReview: (month: YearMonth) -> Unit = {},
    /** «Провести» in «Скоро» → the entry sheet filled from the template on the payment's day (#143). */
    val onPostPayment: (templateId: String, date: LocalDate) -> Unit = { _, _ -> },
    /** «Все N ›» in «Скоро» → «Регулярные платежи» (#403). */
    val onRecurring: () -> Unit = {},
)

@Composable
fun HomeRoute(
    actions: HomeActions,
    viewModel: HomeViewModel = hiltViewModel(),
    syncViewModel: SyncBannerViewModel = hiltViewModel(),
    budgetViewModel: HomeBudgetViewModel = hiltViewModel(),
    upcomingViewModel: UpcomingViewModel = hiltViewModel(),
    screenOptions: ScreenOptionsViewModel = hiltViewModel(),
    hintsViewModel: HintsViewModel = hiltViewModel(),
    gate: VersionGateBannerState? = null,
    onUpdate: () -> Unit = {},
) {
    val lock = LocalInputLock.current
    val steps by hintsViewModel.steps.collectAsStateWithLifecycle()
    var stepsOpen by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val budget by budgetViewModel.state.collectAsStateWithLifecycle()
    val sync by syncViewModel.state.collectAsStateWithLifecycle()
    val upcoming by upcomingViewModel.rows.collectAsStateWithLifecycle()
    val upcomingTotal by upcomingViewModel.total.collectAsStateWithLifecycle()
    val syncActions = SyncBannerActions(
        onRetry = syncViewModel::retry,
        onSignIn = actions.onFamily,
        onFeedback = actions.onFeedback,
        onDismiss = syncViewModel::dismiss,
    )
    // Both counts in before the blocks show: the budget tile takes the place of «Без категории».
    HomeScreen(
        if (budget == null) HomeUiState(isLoading = true) else state,
        actions,
        sync,
        syncActions,
        budget?.budget,
        review = budget?.review,
        onHideReview = budgetViewModel::hideReview,
        upcoming = upcoming,
        upcomingTotal = upcomingTotal,
        onPostAll = lock.guard1 { row -> upcomingViewModel.postAll(row.item.id) { actions.onPostPayment(row.item.id, row.date) } },
        onSkipAll = lock.guard1(upcomingViewModel::skipAll),
        gate = gate,
        onUpdate = onUpdate,
        onLayoutChange = { layout -> screenOptions.change { it.copy(home = layout) } },
        firstSteps = steps,
        onFirstSteps = { stepsOpen = true },
    )
    if (stepsOpen) steps?.let { FirstStepsSheet(it, onHide = hintsViewModel::hideSteps, onClosed = { stepsOpen = false }) }
}

/**
 * «Обзор» (boards `Home`, `HomeAttention`, `BudHome`, `Mon236A`, `Rec143HomeA`): hero card with the total, «Итог месяца» in the
 * first days of a month, «Скоро · 7 дней» (regular payments, #143), two tiles, «Горит в бюджете»
 * when some limit needs a look, the latest operations. Blocks come in as a short cascade the first time the tab shows (Step 4).
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    actions: HomeActions = HomeActions(),
    sync: SyncBannerState? = null,
    syncActions: SyncBannerActions = SyncBannerActions(),
    budget: HomeBudget? = null,
    review: MonthReviewUiState? = null,
    onHideReview: (YearMonth) -> Unit = {},
    upcoming: List<UpcomingRow> = emptyList(),
    upcomingTotal: Int = upcoming.size,
    onPostAll: (UpcomingRow) -> Unit = {},
    onSkipAll: (String) -> Unit = {},
    gate: VersionGateBannerState? = null,
    onUpdate: () -> Unit = {},
    onLayoutChange: (HomeLayout) -> Unit = {},
    firstSteps: FirstStepsState? = null,
    onFirstSteps: () -> Unit = {},
) {
    val scroll = rememberTabScrollBehavior()
    val listState = rememberLazyListState()
    // Hold a card — editing right here: ⠿ drags it, ✕ hides it, «Готово» ends (board Set1HomeEdit).
    var editing by rememberSaveable { mutableStateOf(false) }
    val layout = LocalScreenOptions.current.home
    // The cards this phone shows, without those that have nothing to say now (no gap is left for them).
    val cards = LocalScreenOptions.current.home.visible.filter { card ->
        when (card) {
            HomeCard.REVIEW -> review?.month != null
            HomeCard.UPCOMING -> upcoming.isNotEmpty()
            HomeCard.HOT -> budget?.hot?.isNotEmpty() == true
            HomeCard.TILES, HomeCard.CONVERTER, HomeCard.RECENT -> true
        }
    }
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_home), scroll) },
        bottomBar = {
            if (editing) {
                Box(Modifier.fillMaxWidth().padding(bottom = LocalBottomBarInset.current).padding(Spacing.l)) {
                    CoShellButton(onClick = { editing = false }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.keypad_done)) }
                }
            }
        },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        LazyColumn(
            state = listState,
            // While editing, the «Готово» bar below ends the list; otherwise the list goes under both capsules.
            modifier = Modifier.fillMaxSize().then(if (editing) Modifier.padding(bottom = padding.calculateBottomPadding()) else Modifier),
            contentPadding = underBars(
                if (editing) PaddingValues(top = padding.calculateTopPadding()) else padding,
                PaddingValues(start = Spacing.l, top = Spacing.l, end = Spacing.l, bottom = Sizes.fabClearance),
                bottomBar = !editing,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            if (state.isLoading) return@LazyColumn
            gate?.let { banner -> item(key = "gate") { VersionGateBanner(banner, onUpdate, listItemMotion()) } }
            sync?.let { banner -> item(key = "sync") { SyncBanner(banner, syncActions, listItemMotion()) } }
            if (state.accounts.isEmpty()) {
                if (state.totals.isNotEmpty()) item(key = "total") { EmptyTotalCard(state.totals.first()) }
                firstSteps?.takeIf { it.visible }?.let { steps -> item(key = "steps") { FirstStepsRow(steps, onFirstSteps, listItemMotion()) } }
                item {
                    CoShellEmptyState(
                        iconRes = DesignR.drawable.ic_account_balance_wallet,
                        text = stringResource(R.string.home_no_accounts),
                        actionLabel = stringResource(R.string.accounts_add),
                        onAction = actions.onAddAccount,
                    )
                }
                return@LazyColumn
            }
            item { Box(Modifier.staggeredEntrance(0)) { TotalCard(state, actions.onAccounts) } }
            firstSteps?.takeIf { it.visible }?.let { steps -> item(key = "steps") { FirstStepsRow(steps, onFirstSteps, listItemMotion()) } }
            // «Настройки → Экраны → Обзор»: the cards below the balance in the order and the visibility of this phone.
            itemsIndexed(cards, key = { _, card -> card.name }) { index, card ->
                val entrance = Modifier.staggeredEntrance(minOf(index + 1, MAX_ENTRANCE))
                val body: @Composable () -> Unit = {
                    when (card) {
                        HomeCard.REVIEW -> review?.month?.let { month ->
                            MonthReviewCard(review, onOpen = { actions.onReview(month) }, onHide = { onHideReview(month) }, entrance)
                        }
                        HomeCard.UPCOMING -> if (upcoming.isNotEmpty()) {
                            UpcomingCard(upcoming, upcomingTotal, actions.onPostPayment, actions.onRecurring, entrance, onPostAll, onSkipAll)
                        }
                        HomeCard.TILES -> HomeTiles(
                            state,
                            onTransactions = actions.onTransactions,
                            onRates = actions.onRates,
                            onUncategorized = actions.onUncategorized,
                            modifier = entrance,
                            budget = budget?.summary,
                            onBudgets = actions.onBudgets,
                        )
                        HomeCard.CONVERTER -> ConverterEntry(actions.onConverter, entrance)
                        HomeCard.HOT -> budget?.takeIf { it.hot.isNotEmpty() }?.let { hot ->
                            HotBudgets(hot.hot, onOpen = { actions.onBudget(it, hot.month) }, modifier = entrance)
                        }
                        HomeCard.RECENT -> RecentCard(state.recent, actions.onTransactions, actions.onTransactionClick, entrance)
                    }
                }
                if (editing) {
                    val drag = rememberCardDrag(card, layout, listState, onLayoutChange)
                    Column(drag.shift, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        CardEditHeader(card, onHide = { onLayoutChange(layout.withShown(card, false)) }, dragModifier = drag.modifier)
                        Box {
                            body()
                            // The card shows what it is, but is not pressed while the layout is edited.
                            Box(Modifier.matchParentSize().pointerInput(Unit) { detectTapGestures { } })
                        }
                    }
                } else {
                    Box(Modifier.longPressToEdit(enabled = true) { editing = true }) { body() }
                }
            }
        }
    }
}

/** The cascade of the first showing stops growing after this many cards. */
private const val MAX_ENTRANCE = 3
