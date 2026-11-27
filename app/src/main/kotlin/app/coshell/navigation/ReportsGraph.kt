package app.coshell.navigation

import androidx.compose.runtime.CompositionLocalProvider
import app.coshell.ui.top.LocalTabBack
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import app.coshell.reports.ReportsRoute
import app.coshell.reports.all.ReportCategoriesRoute
import app.coshell.reports.capital.CapitalMonthRoute
import app.coshell.reports.category.ReportCategoryRoute
import app.coshell.reports.recap.MonthRecapRoute
import app.coshell.reports.review.MonthLimitsRoute
import app.coshell.reports.review.MonthReviewRoute
import app.coshell.transactions.list.PRESET_MEMBER

/** Destinations of «Отчёты»: the tab, «Все категории», months, recaps (split out of `CoShellNavHost`). */
internal fun NavGraphBuilder.reportsGraph(navController: NavHostController, tabs: BarTabs) {
    screen<ReportsDestination> {
        CompositionLocalProvider(LocalTabBack provides navController.sectionBack(TopLevel.REPORTS, tabs)) { ReportsRoute(
            onAllCategories = { navController.navigate(ReportCategoriesDestination) { launchSingleTop = true } },
            onCategory = { navController.navigate(it) { launchSingleTop = true } },
            onMember = { preset ->
                navController.openSection(TopLevel.TRANSACTIONS, tabs)
                navController.getBackStackEntry<TransactionsDestination>()
                    .savedStateHandle[PRESET_MEMBER] = preset.encode()
            },
            onCapital = { navController.navigate(it) { launchSingleTop = true } },
            onTransaction = { navController.navigate(TransactionEditDestination(transactionId = it)) },
            onAllFixed = { navController.navigate(RecurringListDestination(year = true)) { launchSingleTop = true } },
            onTemplate = { navController.navigate(TemplateEditDestination(it)) { launchSingleTop = true } },
        ) }
    }
    screen<CapitalMonthDestination> {
        CapitalMonthRoute(
            onBack = { navController.popBackStack() },
            onAccount = { navController.navigate(AccountEditDestination(it)) },
        )
    }
    screen<ReportCategoriesDestination> { entry ->
        // The tab's entry sits under this one: «Все категории» shows its period, currency and data.
        val tab = remember(entry) { navController.getBackStackEntry<ReportsDestination>() }
        ReportCategoriesRoute(
            viewModel = hiltViewModel(tab),
            onBack = { navController.popBackStack() },
            onCategory = { navController.navigate(it) { launchSingleTop = true } },
        )
    }
    screen<MonthRecapDestination> { entry ->
        val route = entry.toRoute<MonthRecapDestination>()
        MonthRecapRoute(
            onBack = { navController.popBackStack() },
            onDetails = { navController.navigate(MonthReviewDestination(route.year, route.month)) { launchSingleTop = true } },
        )
    }
    screen<MonthReviewDestination> { entry ->
        val route = entry.toRoute<MonthReviewDestination>()
        MonthReviewRoute(
            onBack = { navController.popBackStack() },
            onCategory = { navController.navigate(it) { launchSingleTop = true } },
            onLimit = { id -> navController.navigate(BudgetDetailDestination(id, route.year, route.month)) { launchSingleTop = true } },
            onAllLimits = { navController.navigate(MonthReviewLimitsDestination) { launchSingleTop = true } },
        )
    }
    screen<MonthReviewLimitsDestination> { entry ->
        // The review's entry sits under this one: the limits are of its month, from its data.
        val review = remember(entry) { navController.getBackStackEntry<MonthReviewDestination>() }
        val route = review.toRoute<MonthReviewDestination>()
        MonthLimitsRoute(
            viewModel = hiltViewModel(review),
            onBack = { navController.popBackStack() },
            onLimit = { id -> navController.navigate(BudgetDetailDestination(id, route.year, route.month)) { launchSingleTop = true } },
        )
    }
    screen<ReportCategoryDestination> {
        ReportCategoryRoute(
            onBack = { navController.popBackStack() },
            onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
        )
    }
}
