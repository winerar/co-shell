package app.coshell.home

import app.coshell.hints.FirstStepsSheet
import app.coshell.hints.HintsViewModel
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import app.coshell.settings.ScreenOptionsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.home.budget.HomeBudgetViewModel
import app.coshell.home.components.VersionGateBannerState
import app.coshell.gate.LocalInputLock
import app.coshell.home.components.SyncBannerActions
import app.coshell.home.sync.SyncBannerViewModel
import app.coshell.home.upcoming.UpcomingViewModel

@Composable
fun HomeRoute(
    actions: HomeActions,
    viewModel: HomeViewModel = hiltViewModel(),
    syncViewModel: SyncBannerViewModel = hiltViewModel(),
    budgetViewModel: HomeBudgetViewModel = hiltViewModel(),
    upcomingViewModel: UpcomingViewModel = hiltViewModel(),
    screenOptions: ScreenOptionsViewModel = hiltViewModel(),
    hintsViewModel: HintsViewModel = hiltViewModel(),
    draftsCount: DraftsCountViewModel = hiltViewModel(),
    gate: VersionGateBannerState? = null,
    onUpdate: () -> Unit = {},
) {
    val drafts by draftsCount.count.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    val steps by hintsViewModel.steps.collectAsStateWithLifecycle()
    val finale by hintsViewModel.finale.collectAsStateWithLifecycle()
    var stepsOpen by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val budget by budgetViewModel.state.collectAsStateWithLifecycle()
    val sync by syncViewModel.state.collectAsStateWithLifecycle()
    val syncDot by syncViewModel.dot.collectAsStateWithLifecycle()
    val upcoming by upcomingViewModel.rows.collectAsStateWithLifecycle()
    val upcomingTotal by upcomingViewModel.total.collectAsStateWithLifecycle()
    val creditPayments by upcomingViewModel.credit.collectAsStateWithLifecycle()
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
        creditPayments = creditPayments,
        onPostAll = lock.guard1 { row -> upcomingViewModel.postAll(row.item.id) { actions.onPostPayment(row.item.id, row.date) } },
        onSkipAll = lock.guard1(upcomingViewModel::skipAll),
        gate = gate,
        onUpdate = onUpdate,
        onLayoutChange = { layout -> screenOptions.change { it.copy(home = layout) } },
        firstSteps = steps,
        onFirstSteps = { stepsOpen = true },
        finale = finale,
        onFinale = hintsViewModel::dismissFinale,
        draftCount = drafts,
        syncDot = syncDot,
    )
    if (stepsOpen) steps?.let { FirstStepsSheet(it, onHide = hintsViewModel::hideSteps, onClosed = { stepsOpen = false }) }
}
