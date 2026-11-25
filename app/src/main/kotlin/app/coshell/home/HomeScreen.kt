package app.coshell.home

import app.coshell.converter.ConverterEntry
import app.coshell.core.domain.hints.FirstStepsState
import app.coshell.core.domain.hints.HintId
import app.coshell.hints.FirstStepsRow
import app.coshell.hints.CoachHint
import app.coshell.hints.LocalHintHost
import app.coshell.hints.StepsFinale
import app.coshell.hints.WordHint
import app.coshell.home.components.DraftsStrip
import app.coshell.home.components.EmptyTotalCard
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellEmptyState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.motion.listItemMotion
import app.coshell.core.designsystem.motion.staggeredEntrance
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.budget.HomeBudget
import app.coshell.home.components.HomeTiles
import app.coshell.home.components.HotBudgets
import app.coshell.home.components.MonthReviewCard
import app.coshell.home.components.RecentCard
import app.coshell.home.components.SyncBanner
import app.coshell.home.components.VersionGateBanner
import app.coshell.home.components.VersionGateBannerState
import app.coshell.home.components.SyncBannerActions
import app.coshell.home.components.TotalCard
import app.coshell.home.sync.SyncBannerState
import app.coshell.reports.review.MonthReviewUiState
import app.coshell.ui.bar.aboveBottomBars
import app.coshell.ui.top.TabTopBar
import app.coshell.privacy.EyeButton
import app.coshell.release.ReleaseNotice
import app.coshell.release.ReleaseNoticeCard
import app.coshell.hints.TourCard
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import app.coshell.home.sync.SyncDot
import app.coshell.home.sync.SyncDotState
import app.coshell.home.upcoming.CreditPaymentRow
import app.coshell.home.upcoming.DebtDueRow
import app.coshell.home.upcoming.DepositEndRow
import app.coshell.home.upcoming.UpcomingCard
import app.coshell.home.upcoming.UpcomingRow
import kotlinx.datetime.YearMonth

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
    creditPayments: List<CreditPaymentRow> = emptyList(),
    debtDue: List<DebtDueRow> = emptyList(),
    depositEnds: List<DepositEndRow> = emptyList(),
    onPostAll: (UpcomingRow) -> Unit = {},
    onSkipAll: (String) -> Unit = {},
    gate: VersionGateBannerState? = null,
    onUpdate: () -> Unit = {},
    onLayoutChange: (HomeLayout) -> Unit = {},
    firstSteps: FirstStepsState? = null,
    onFirstSteps: () -> Unit = {},
    finale: Boolean = false,
    onFinale: () -> Unit = {},
    draftCount: Int = 0,
    syncDot: SyncDotState? = null,
    release: ReleaseNotice? = null,
    onReleaseOpen: () -> Unit = {},
    onReleaseDismiss: () -> Unit = {},
    tour: Boolean = false,
    onTourOpen: () -> Unit = {},
    onTourDismiss: () -> Unit = {},
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
            HomeCard.UPCOMING -> upcoming.isNotEmpty() || creditPayments.isNotEmpty() || debtDue.isNotEmpty() || depositEnds.isNotEmpty()
            HomeCard.HOT -> budget?.hot?.isNotEmpty() == true
            HomeCard.TILES, HomeCard.CONVERTER, HomeCard.RECENT -> true
        }
    }
    Scaffold(
        topBar = {
            TabTopBar(stringResource(R.string.nav_home), scroll) {
                syncDot?.let { SyncDot(it, actions.onFamily) }
                EyeButton()
            }
        },
        bottomBar = {
            if (editing) {
                Box(Modifier.fillMaxWidth().aboveBottomBars().padding(Spacing.l)) {
                    CoShellButton(onClick = { editing = false }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.keypad_done)) }
                }
            }
        },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        val hintsOn = LocalHintHost.current != null
        CoachHint(mapOf(HintId.FLIP_CARD to R.string.hint_flip_card), enabled = state.recent.isNotEmpty())
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
            release?.let { notice ->
                item(key = "release") { ReleaseNoticeCard(notice.versionName, onReleaseOpen, onReleaseDismiss, listItemMotion()) }
            }
            if (tour) item(key = "tour") { TourCard(onTourOpen, onTourDismiss, listItemMotion()) }
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
            if (finale) item(key = "steps-finale") { StepsFinale(onFinale, listItemMotion()) }
            item(key = "drafts") { DraftsStrip(draftCount, actions.onDrafts, actions.onNotes, listItemMotion()) }
            if (state.recent.isEmpty() && hintsOn) item(key = "word-hint") { WordHint(HintId.OVERVIEW_EMPTY, R.string.hint_overview_empty) }
            // «Настройки → Экраны → Обзор»: the cards below the balance in the order and the visibility of this phone.
            itemsIndexed(cards, key = { _, card -> card.name }) { index, card ->
                val entrance = Modifier.staggeredEntrance(minOf(index + 1, MAX_ENTRANCE))
                val body: @Composable () -> Unit = {
                    when (card) {
                        HomeCard.REVIEW -> review?.month?.let { month ->
                            MonthReviewCard(review, onOpen = { actions.onReview(month) }, onHide = { onHideReview(month) }, entrance)
                        }
                        HomeCard.UPCOMING -> if (upcoming.isNotEmpty() || creditPayments.isNotEmpty() || debtDue.isNotEmpty() || depositEnds.isNotEmpty()) {
                            UpcomingCard(upcoming, upcomingTotal, actions.onPostPayment, actions.onRecurring, entrance, onPostAll, onSkipAll, creditPayments, actions.onPostCredit, debtDue, actions.onOpenDebt, depositEnds, actions.onOpenAccount)
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
