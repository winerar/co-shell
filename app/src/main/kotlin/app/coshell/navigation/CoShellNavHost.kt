package app.coshell.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.coshell.R
import app.coshell.accounts.edit.AccountEditRoute
import app.coshell.accounts.list.AccountsRoute
import app.coshell.backup.BackupRoute
import app.coshell.feedback.FeedbackRoute
import app.coshell.categories.edit.CategoryEditRoute
import app.coshell.categories.list.CategoriesRoute
import app.coshell.home.HomeRoute
import app.coshell.more.MoreRoute
import app.coshell.rates.list.RatesRoute
import app.coshell.currencies.list.ReportCurrenciesRoute
import app.coshell.shake.ShakeEffect
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.transactions.edit.TransactionEditRoute
import app.coshell.transactions.list.TransactionsRoute
import app.coshell.update.UpdateRoute
import app.coshell.update.startup.StartupUpdateDialogs
import kotlin.reflect.KClass
import app.coshell.core.designsystem.R as DesignR

/** Tabs of the bottom navigation bar. */
private enum class TopLevel(
    val route: Any,
    val routeClass: KClass<*>,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    HOME(HomeDestination, HomeDestination::class, R.string.nav_home, DesignR.drawable.ic_home),
    TRANSACTIONS(
        TransactionsDestination,
        TransactionsDestination::class,
        R.string.nav_transactions,
        DesignR.drawable.ic_receipt_long,
    ),
    ACCOUNTS(
        AccountsDestination,
        AccountsDestination::class,
        R.string.nav_accounts,
        DesignR.drawable.ic_account_balance_wallet,
    ),
    MORE(MoreDestination, MoreDestination::class, R.string.nav_more, DesignR.drawable.ic_more_horiz),
}

@Composable
fun CoShellNavHost(
    navController: NavHostController = rememberNavController(),
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination
    val showBottomBar = TopLevel.entries.any { current.isOn(it) }

    StartupUpdateDialogs(onOpenUpdate = { navController.navigate(UpdateDestination) { launchSingleTop = true } })

    // Shake → new transaction, only on the tabs: never interrupts a form or another screen.
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    ShakeEffect(
        enabled = shakeEnabled && showBottomBar,
        onShake = { navController.navigate(TransactionEditDestination()) { launchSingleTop = true } },
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevel.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = current.isOn(tab),
                            onClick = { navController.navigateToTab(tab) },
                            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        // Inner screens have their own Scaffold and handle the status bar themselves, so only the
        // bottom inset is applied and consumed here; consuming all of `padding` would hide the top one.
        val bottomInset = PaddingValues(bottom = padding.calculateBottomPadding())
        Box(Modifier.padding(bottomInset).consumeWindowInsets(bottomInset)) {
            NavHost(navController = navController, startDestination = HomeDestination) {
                composable<HomeDestination> {
                    HomeRoute(
                        onAccountClick = { navController.navigate(AccountEditDestination(it)) },
                        onAddAccount = { navController.navigate(AccountEditDestination()) },
                        onAddTransaction = { navController.navigate(TransactionEditDestination()) },
                    )
                }
                composable<TransactionsDestination> {
                    TransactionsRoute(
                        onAddTransaction = { navController.navigate(TransactionEditDestination()) },
                        onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
                    )
                }
                composable<TransactionEditDestination> {
                    TransactionEditRoute(
                        onDone = { navController.popBackStack() },
                        onAddAccount = { navController.navigate(AccountEditDestination()) },
                    )
                }
                composable<AccountsDestination> {
                    AccountsRoute(
                        onAccountClick = { navController.navigate(AccountEditDestination(it)) },
                        onAddAccount = { navController.navigate(AccountEditDestination()) },
                    )
                }
                composable<AccountEditDestination> {
                    AccountEditRoute(onDone = { navController.popBackStack() })
                }
                composable<MoreDestination> {
                    MoreRoute(
                        onCategories = { navController.navigate(CategoriesDestination) },
                        onRates = { navController.navigate(RatesDestination) },
                        onReportCurrencies = { navController.navigate(ReportCurrenciesDestination) },
                        onBackup = { navController.navigate(BackupDestination) },
                        onUpdate = { navController.navigate(UpdateDestination) },
                        onFeedback = { navController.navigate(FeedbackDestination) },
                    )
                }
                composable<UpdateDestination> {
                    UpdateRoute(onBack = { navController.popBackStack() })
                }
                composable<FeedbackDestination> {
                    FeedbackRoute(onBack = { navController.popBackStack() })
                }
                composable<RatesDestination> {
                    RatesRoute(onBack = { navController.popBackStack() })
                }
                composable<ReportCurrenciesDestination> {
                    ReportCurrenciesRoute(onBack = { navController.popBackStack() })
                }
                composable<BackupDestination> {
                    BackupRoute(onBack = { navController.popBackStack() })
                }
                composable<CategoriesDestination> {
                    CategoriesRoute(
                        onBack = { navController.popBackStack() },
                        onOpen = { navController.navigate(it) },
                    )
                }
                composable<CategoryEditDestination> {
                    CategoryEditRoute(onDone = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun NavDestination?.isOn(tab: TopLevel): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

private fun NavHostController.navigateToTab(tab: TopLevel) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
