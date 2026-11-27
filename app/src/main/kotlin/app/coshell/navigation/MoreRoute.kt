package app.coshell.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.coshell.BuildConfig
import app.coshell.home.DraftsCountViewModel
import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
import app.coshell.more.MoreState
import app.coshell.newmark.NewMarksViewModel

/** «Ещё»: where each row leads, the «новое» pills and «О приложении» with the version. */
@Composable
internal fun MoreRoute(
    navController: NavHostController,
    availableVersion: String?,
    newMarks: NewMarksViewModel = hiltViewModel(),
    draftsCount: DraftsCountViewModel = hiltViewModel(),
) {
    val drafts by draftsCount.count.collectAsStateWithLifecycle()
    val marks by newMarks.current.collectAsStateWithLifecycle()
    // A section that is on the bar (#154) opens as that tab.
    val tabs = LocalBarTabs.current
    MoreScreen(
        actions = MoreActions(
            onAccounts = { navController.openSection(TopLevel.ACCOUNTS, tabs) },
            onCategories = { navController.navigate(CategoriesDestination) },
            onBudgets = { navController.openSection(TopLevel.BUDGETS, tabs) },
            onRecurring = { navController.navigate(RecurringListDestination()) { launchSingleTop = true } },
            onRates = { navController.navigate(RatesDestination) },
            onConverter = { navController.navigate(ConverterDestination) { launchSingleTop = true } },
            onFamily = { navController.navigate(FamilyDestination) },
            onBackup = { navController.navigate(BackupDestination) },
            onImport = { navController.navigate(WalletImportDestination) },
            onExport = { navController.navigate(ExportDestination) },
            onSettings = { navController.navigate(SettingsDestination) },
            onPrivacy = { navController.navigate(PrivacyDestination) },
            onFeedback = { navController.navigate(FeedbackDestination()) },
            onNotes = { navController.navigate(NotesDestination) { launchSingleTop = true } },
            onDebts = { navController.openSection(TopLevel.DEBTS, tabs) },
            onGoals = { navController.openSection(TopLevel.GOALS, tabs) },
            onDrafts = { navController.navigate(DraftsDestination) { launchSingleTop = true } },
            onOpenUpdate = { navController.navigate(UpdateDestination) },
            onOpenedNew = newMarks::opened,
        ),
        state = MoreState(
            versionName = BuildConfig.VERSION_NAME,
            build = BuildConfig.VERSION_CODE,
            availableVersion = availableVersion,
            newMarks = marks,
            draftCount = drafts,
        ),
    )
}
