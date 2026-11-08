package app.coshell.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
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
import app.coshell.accounts.AccountEditRoute
import app.coshell.accounts.AccountsRoute
import app.coshell.home.HomeRoute
import app.coshell.transactions.TransactionsRoute
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
    ACCOUNTS(AccountsDestination, AccountsDestination::class, R.string.nav_accounts, DesignR.drawable.ic_account_balance_wallet),
}

@Composable
fun CoShellNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination
    val showBottomBar = TopLevel.entries.any { current.isOn(it) }

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
        // Inner screens have their own Scaffold; only the bottom bar inset is applied here.
        Box(Modifier.padding(bottom = padding.calculateBottomPadding()).consumeWindowInsets(padding)) {
            NavHost(navController = navController, startDestination = HomeDestination) {
                composable<HomeDestination> {
                    HomeRoute(
                        onAccountClick = { navController.navigate(AccountEditDestination(it)) },
                        onAddAccount = { navController.navigate(AccountEditDestination()) },
                    )
                }
                composable<TransactionsDestination> { TransactionsRoute() }
                composable<AccountsDestination> {
                    AccountsRoute(
                        onAccountClick = { navController.navigate(AccountEditDestination(it)) },
                        onAddAccount = { navController.navigate(AccountEditDestination()) },
                    )
                }
                composable<AccountEditDestination> {
                    AccountEditRoute(onDone = { navController.popBackStack() })
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
