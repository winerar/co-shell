package app.coshell.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.motion.LocalAddConfirm
import kotlin.reflect.KClass
import app.coshell.core.designsystem.R as DesignR

/** Tabs of the bottom navigation bar; the "+" sits between the first two and the last two. */
internal enum class TopLevel(
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

internal fun NavDestination?.isOn(tab: TopLevel): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

/** Bottom bar of the app: Обзор · Операции · [+] · Счета · Ещё; the "+" opens a new transaction. Hidden off the tabs. */
@Composable
internal fun MainBottomBar(
    current: NavDestination?,
    onTab: (TopLevel) -> Unit,
    onAdd: () -> Unit,
    visible: Boolean = true,
) {
    val tabs = TopLevel.entries
    CoShellBottomBar(
        tabs = tabs.map { CoShellTab(it.icon, stringResource(it.label), selected = current.isOn(it)) },
        onTab = { onTab(tabs[it]) },
        addLabel = stringResource(R.string.transaction_add),
        onAdd = onAdd,
        visible = visible,
        addConfirm = LocalAddConfirm.current,
    )
}
