package app.coshell.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.FanAction
import app.coshell.core.designsystem.components.TabDrag
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.motion.LocalAddConfirm
import kotlin.reflect.KClass
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.domain.hints.HintId
import app.coshell.core.domain.settings.BarTab
import app.coshell.hints.CoachHint

/**
 * Sections that can sit on the bottom bar. «Обзор» is always first and «Ещё» last; the two between them are chosen in
 * «Настройки → Экраны → Нижняя панель» (#154, [BarTab]); the bar itself — [BarTabs]. The "+" sits between the first two and the last two.
 */
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
    ACCOUNTS(AccountsDestination, AccountsDestination::class, R.string.nav_accounts, DesignR.drawable.ic_account_balance_wallet),
    BUDGETS(BudgetsDestination, BudgetsDestination::class, R.string.budgets_title, DesignR.drawable.ic_savings),
    GOALS(GoalsDestination, GoalsDestination::class, R.string.goals_title, DesignR.drawable.ic_flag),
    DEBTS(DebtsDestination, DebtsDestination::class, R.string.debts_title, DesignR.drawable.ic_person_add),
    MORE(MoreDestination, MoreDestination::class, R.string.nav_more, DesignR.drawable.ic_more_horiz),
}

/** The section of a chosen [BarTab]. */
internal val BarTab.topLevel: TopLevel
    get() = when (this) {
        BarTab.TRANSACTIONS -> TopLevel.TRANSACTIONS
        BarTab.REPORTS -> TopLevel.REPORTS
        BarTab.ACCOUNTS -> TopLevel.ACCOUNTS
        BarTab.BUDGETS -> TopLevel.BUDGETS
        BarTab.GOALS -> TopLevel.GOALS
        BarTab.DEBTS -> TopLevel.DEBTS
    }

/** The four tabs of the bar in their order: «Обзор», the two chosen ones, «Ещё». */
@Immutable
internal data class BarTabs(val second: TopLevel = TopLevel.TRANSACTIONS, val third: TopLevel = TopLevel.REPORTS) {
    val all: List<TopLevel> get() = listOf(TopLevel.HOME, second, third, TopLevel.MORE)

    operator fun contains(tab: TopLevel): Boolean = tab == TopLevel.HOME || tab == TopLevel.MORE || tab == second || tab == third

    /** The tab [destination] belongs to, if it is on the bar. */
    fun of(destination: NavDestination?): TopLevel? = all.firstOrNull { destination.isOn(it) }

    companion object {
        fun of(second: BarTab, third: BarTab) = BarTabs(second.topLevel, third.topLevel)
    }
}

/** The bar's tabs for the screens under the shell; the defaults outside it (previews, tests). */
internal val LocalBarTabs = staticCompositionLocalOf { BarTabs() }

internal fun NavDestination?.isOn(tab: TopLevel): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

/** Bottom bar of the app: Обзор · [tabs] · [+] · … · Ещё (#140, #154); the "+" opens a new transaction. Hidden off the tabs. */
@Composable
internal fun MainBottomBar(
    current: NavDestination?,
    tabs: BarTabs = BarTabs(),
    onTab: (TopLevel) -> Unit,
    onAdd: () -> Unit,
    onFan: (FanTarget) -> Unit = {},
    visible: Boolean = true,
    moreBadge: Boolean = false,
    reportsBadge: Boolean = false,
    addLocked: Boolean = false,
    drag: TabDrag? = null,
) {
    // `swipe_tabs`: after the second tab change by button the swipe is worth a mention (hints.md).
    var buttonChanges by rememberSaveable { mutableIntStateOf(0) }
    CoachHint(mapOf(HintId.SWIPE_TABS to R.string.hint_swipe_tabs), enabled = buttonChanges >= SWIPE_HINT_AFTER)
    // `hold_plus` (#623): after the second tap on «+» the fan under a hold is worth a mention (hints.md).
    var addTaps by rememberSaveable { mutableIntStateOf(0) }
    CoachHint(mapOf(HintId.HOLD_PLUS to R.string.hint_hold_plus), enabled = addTaps >= HOLD_HINT_AFTER)
    val fan = FanTarget.entries.map { target -> FanAction(target.icon, stringResource(target.label)) { onFan(target) } }
    CoShellBottomBar(
        tabs = tabs.all.map { CoShellTab(it.icon, stringResource(it.label), selected = current.isOn(it), badge = (moreBadge && it == TopLevel.MORE) || (reportsBadge && it == TopLevel.REPORTS)) },
        onTab = {
            if (!current.isOn(tabs.all[it])) buttonChanges++
            onTab(tabs.all[it])
        },
        addLabel = stringResource(if (addLocked) R.string.gate_add_locked else R.string.transaction_add),
        onAdd = {
            addTaps++
            onAdd()
        },
        visible = visible,
        addConfirm = LocalAddConfirm.current,
        addLocked = addLocked,
        drag = drag,
        fan = fan,
    )
}

private const val SWIPE_HINT_AFTER = 2
private const val HOLD_HINT_AFTER = 2
