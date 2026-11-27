package app.coshell.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import app.coshell.BuildConfig
import androidx.compose.runtime.remember
import app.coshell.alarm.BackgroundAskDialog
import app.coshell.alarm.BackgroundAskViewModel
import app.coshell.alarm.BackgroundCheck
import app.coshell.notices.NoticeOfferViewModel
import app.coshell.recurring.notify.PaymentNotices
import app.coshell.release.ReleaseNotice
import app.coshell.release.ReleaseNoticeViewModel
import app.coshell.release.ReleaseStories
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
import app.coshell.home.goals.HomeGoalsViewModel
import app.coshell.home.daily.HomeDailyViewModel
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
    goalsViewModel: HomeGoalsViewModel = hiltViewModel(),
    dailyViewModel: HomeDailyViewModel = hiltViewModel(),
    screenOptions: ScreenOptionsViewModel = hiltViewModel(),
    hintsViewModel: HintsViewModel = hiltViewModel(),
    noticeOffer: NoticeOfferViewModel = hiltViewModel(),
    draftsCount: DraftsCountViewModel = hiltViewModel(),
    releaseNotice: ReleaseNoticeViewModel = hiltViewModel(),
    backgroundAsk: BackgroundAskViewModel = hiltViewModel(),
    gate: VersionGateBannerState? = null,
    onUpdate: () -> Unit = {},
) {
    val drafts by draftsCount.count.collectAsStateWithLifecycle()
    val release by releaseNotice.visible.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    val steps by hintsViewModel.steps.collectAsStateWithLifecycle()
    val finale by hintsViewModel.finale.collectAsStateWithLifecycle()
    val tour by hintsViewModel.tour.collectAsStateWithLifecycle()
    val offerVisible by noticeOffer.visible.collectAsStateWithLifecycle()
    // Android 13+: the notification is asked for when the evening reminder is turned on.
    val context = LocalContext.current.applicationContext
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    var stepsOpen by rememberSaveable { mutableStateOf(false) }
    // #745: «Разрешить работу в фоне?» — once after the update for those whose reminders are on, and on «Включить» in the offer.
    val background = remember { BackgroundCheck.advice(context) }
    val askPending by backgroundAsk.pending.collectAsStateWithLifecycle()
    val anythingOn by backgroundAsk.anythingOn.collectAsStateWithLifecycle()
    var askNow by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val budget by budgetViewModel.state.collectAsStateWithLifecycle()
    val sync by syncViewModel.state.collectAsStateWithLifecycle()
    val syncDot by syncViewModel.dot.collectAsStateWithLifecycle()
    val upcoming by upcomingViewModel.rows.collectAsStateWithLifecycle()
    val upcomingTotal by upcomingViewModel.total.collectAsStateWithLifecycle()
    val creditPayments by upcomingViewModel.credit.collectAsStateWithLifecycle()
    val debtDue by upcomingViewModel.debtDue.collectAsStateWithLifecycle()
    val depositEnds by upcomingViewModel.deposits.collectAsStateWithLifecycle()
    val goals by goalsViewModel.rows.collectAsStateWithLifecycle()
    val daily by dailyViewModel.state.collectAsStateWithLifecycle()
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
        debtDue = debtDue,
        depositEnds = depositEnds,
        goals = goals,
        daily = daily,
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
        release = if (release) ReleaseNotice(BuildConfig.VERSION_NAME) else null,
        onReleaseOpen = { releaseNotice.done(); actions.onReleaseStories() },
        onReleaseDismiss = releaseNotice::done,
        tour = tour,
        onTourOpen = { hintsViewModel.tourDone(); actions.onTour() },
        onTourDismiss = hintsViewModel::tourDone,
        noticeOffer = offerVisible,
        onNoticeOfferEnable = {
            if (BackgroundCheck.askOnEnable(background, anythingOn)) askNow = true
            noticeOffer.enable()
            if (!PaymentNotices.canNotify(context)) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        onNoticeOfferLater = noticeOffer::later,
    )
    if (askNow || (askPending && background != BackgroundCheck.Advice.NONE)) {
        BackgroundAskDialog(background) { askNow = false; backgroundAsk.answered() }
    }
    if (stepsOpen) steps?.let { FirstStepsSheet(it, onHide = hintsViewModel::hideSteps, onClosed = { stepsOpen = false }) }
}
