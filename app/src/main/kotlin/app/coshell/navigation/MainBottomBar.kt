package app.coshell.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.TabDrag
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.motion.LocalAddConfirm
import kotlin.reflect.KClass
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.domain.hints.HintId
import app.coshell.hints.CoachHint

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
    REPORTS(ReportsDestination, ReportsDestination::class, R.string.nav_reports, DesignR.drawable.ic_bar_chart),
    MORE(MoreDestination, MoreDestination::class, R.string.nav_more, DesignR.drawable.ic_more_horiz),
}

internal fun NavDestination?.isOn(tab: TopLevel): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

/** Bottom bar of the app: Обзор · Операции · [+] · Отчёты · Ещё (#140, «Счета» moved to «Ещё»); the "+" opens a new transaction. Hidden off the tabs. */
@Composable
internal fun MainBottomBar(
    current: NavDestination?,
    onTab: (TopLevel) -> Unit,
    onAdd: () -> Unit,
    visible: Boolean = true,
    moreBadge: Boolean = false,
    reportsBadge: Boolean = false,
    addLocked: Boolean = false,
    drag: TabDrag? = null,
) {
    val tabs = TopLevel.entries
    // `swipe_tabs`: after the second tab change by button the swipe is worth a mention (hints.md).
    var buttonChanges by rememberSaveable { mutableIntStateOf(0) }
    CoachHint(mapOf(HintId.SWIPE_TABS to R.string.hint_swipe_tabs), enabled = buttonChanges >= SWIPE_HINT_AFTER)
    CoShellBottomBar(
        tabs = tabs.map { CoShellTab(it.icon, stringResource(it.label), selected = current.isOn(it), badge = (moreBadge && it == TopLevel.MORE) || (reportsBadge && it == TopLevel.REPORTS)) },
        onTab = {
            if (!current.isOn(tabs[it])) buttonChanges++
            onTab(tabs[it])
        },
        addLabel = stringResource(if (addLocked) R.string.gate_add_locked else R.string.transaction_add),
        onAdd = onAdd,
        visible = visible,
        addConfirm = LocalAddConfirm.current,
        addLocked = addLocked,
        drag = drag,
    )
}

private const val SWIPE_HINT_AFTER = 2
