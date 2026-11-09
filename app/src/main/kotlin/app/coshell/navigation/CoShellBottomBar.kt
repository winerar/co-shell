package app.coshell.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFab
import app.coshell.core.designsystem.components.CoShellNavigationBarItem
import app.coshell.core.designsystem.theme.LocalDarkTheme
import app.coshell.core.designsystem.motion.Tactile
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

/**
 * Bottom bar: Обзор · Операции · [+] · Счета · Ещё. The "+" (new transaction, the most frequent
 * action) is always under the thumb, whichever tab is open (design-questions §5).
 */
@Composable
internal fun CoShellBottomBar(current: NavDestination?, onTab: (TopLevel) -> Unit, onAdd: () -> Unit) {
    // Active tab: outlined icon on a neon pill (design-questions §4) — bright mint in both themes.
    val dark = LocalDarkTheme.current
    val scheme = MaterialTheme.colorScheme
    val tabColors = NavigationBarItemDefaults.colors(
        indicatorColor = if (dark) scheme.primary else scheme.primaryContainer,
        selectedIconColor = if (dark) scheme.onPrimary else scheme.onPrimaryContainer,
        selectedTextColor = scheme.onSurface,
    )
    NavigationBar {
        val tabs = TopLevel.entries
        tabs.forEachIndexed { index, tab ->
            if (index == tabs.size / 2) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    CoShellFab(
                        onClick = onAdd,
                        shape = CircleShape,
                        tactile = Tactile.Primary,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    ) {
                        Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.transaction_add))
                    }
                }
            }
            CoShellNavigationBarItem(
                colors = tabColors,
                selected = current.isOn(tab),
                onClick = { onTab(tab) },
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
            )
        }
    }
}
