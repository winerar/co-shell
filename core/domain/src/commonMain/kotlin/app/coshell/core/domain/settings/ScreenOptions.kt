package app.coshell.core.domain.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow

/** «Деньги и даты → Неделя начинается с»: Monday, Sunday or what the phone says. */
enum class WeekStart {
    MONDAY,
    SUNDAY,
    SYSTEM,
    ;

    /** The seven days in the order of a week that starts as chosen; [systemFirst] is the phone's own first day. */
    fun days(systemFirst: DayOfWeek): List<DayOfWeek> {
        val first = when (this) {
            MONDAY -> DayOfWeek.MONDAY
            SUNDAY -> DayOfWeek.SUNDAY
            SYSTEM -> systemFirst
        }
        val all = DayOfWeek.entries
        return all.drop(first.ordinal) + all.take(first.ordinal)
    }

    companion object {
        /** Saved as [name]; unknown or missing → [SYSTEM]. */
        fun parse(stored: String?): WeekStart = entries.firstOrNull { it.name == stored } ?: SYSTEM
    }
}

/** The period «Отчёты» open on. */
enum class ReportsStart {
    MONTH,
    QUARTER,
    YEAR,
    ;

    companion object {
        /** Saved as [name]; unknown or missing → [MONTH]. */
        fun parse(stored: String?): ReportsStart = entries.firstOrNull { it.name == stored } ?: MONTH
    }
}

/**
 * How the screens look on this phone — «Настройки → Экраны» (boards `Set1Ops`, `Set1Reports`, `Set1Accounts`). Each phone has
 * its own; the defaults are what the app showed before the options existed.
 */
data class ScreenOptions(
    /** «Операции»: the account line, the tag pills and the note under the category. */
    val opsShowAccount: Boolean = true,
    val opsShowTags: Boolean = true,
    val opsShowNote: Boolean = true,
    /** «Операции»: under the amount, «≈ …» in the main report currency at the rate of the day of the operation. */
    val opsAmountInMain: Boolean = false,
    /** «Счета»: grouped by currency instead of type, and the archive shown at the start. */
    val accountsByCurrency: Boolean = false,
    val accountsShowArchived: Boolean = false,
    /** «Отчёты»: the period they open on and whether the previous period is compared. */
    val reportsStart: ReportsStart = ReportsStart.MONTH,
    val reportsCompare: Boolean = true,
    /** «Отчёты» (#826): the cards of the feed and their order. */
    val reports: ReportLayout = ReportLayout(),
    /** «Ввод операции → Отклик»: vibration on taps and on a saved transaction. */
    val haptics: Boolean = true,
    /** «Данные и копия → Сеть»: sync only on Wi-Fi (or another unmetered network), not on mobile data. */
    val syncWifiOnly: Boolean = false,
    /** «Обзор»: the cards and their order, how many days «Скоро» looks ahead, how many «Последние» show. */
    val home: HomeLayout = HomeLayout(),
    val soonDays: Int = 7,
    val recentCount: Int = 5,
    /** «Плитки · две»: the first and the second tile of «Обзор»; never the same one twice. */
    val tileFirst: HomeTile = HomeTile.SPENDING,
    val tileSecond: HomeTile = HomeTile.BUDGET,
    /** «Деньги и даты»: the first day of a week in «Повторе» of a template. */
    val weekStart: WeekStart = WeekStart.SYSTEM,
    /** «Ввод операции → Шторка «+»»: the account (and with it the currency) the sheet opens on; `null` — the one used last. */
    val entryAccountId: String? = null,
    /** «Уведомления → Трата партнёра»: tell about the other member's expenses from [partnerSpendMin] whole units of the main currency. */
    val partnerSpend: Boolean = false,
    val partnerSpendMin: Int = 50,
    /** «Экраны → Нижняя панель» (#154): the second and the third tab; never the same one twice. */
    val barSecond: BarTab = BarTab.TRANSACTIONS,
    val barThird: BarTab = BarTab.REPORTS,
) {
    /** [tile] goes to the first place; if it was the second one, the two swap. */
    fun withFirst(tile: HomeTile): ScreenOptions = if (tile == tileSecond) copy(tileFirst = tile, tileSecond = tileFirst) else copy(tileFirst = tile)

    fun withSecond(tile: HomeTile): ScreenOptions = if (tile == tileFirst) copy(tileSecond = tile, tileFirst = tileSecond) else copy(tileSecond = tile)

    /** [tab] goes to the second place of the bar; if it was the third one, the two swap. */
    fun withBarSecond(tab: BarTab): ScreenOptions = if (tab == barThird) copy(barSecond = tab, barThird = barSecond) else copy(barSecond = tab)

    fun withBarThird(tab: BarTab): ScreenOptions = if (tab == barSecond) copy(barThird = tab, barSecond = barThird) else copy(barThird = tab)

    companion object {
        /** What «Скоро» and «Последние» offer. */
        val SOON_DAYS = listOf(3, 7, 14, 30)
        val RECENT_COUNTS = listOf(3, 5, 10)

        /** The thresholds of «Трата партнёра» (0 — every expense). */
        val PARTNER_MINIMUMS = listOf(0, 10, 50, 100, 500, 1000)
    }
}

/** [ScreenOptions] of this phone: read as a flow, changed as a whole (the caller copies what it flips). */
interface ScreenOptionsStore {
    val options: Flow<ScreenOptions>
    suspend fun update(change: (ScreenOptions) -> ScreenOptions)
}

/** In memory — for tests and previews. */
class InMemoryScreenOptions(initial: ScreenOptions = ScreenOptions()) : ScreenOptionsStore {
    val state = MutableStateFlow(initial)
    override val options: Flow<ScreenOptions> = state
    override suspend fun update(change: (ScreenOptions) -> ScreenOptions) {
        state.value = change(state.value)
    }
}
