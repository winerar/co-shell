package app.coshell.settings

import androidx.annotation.StringRes
import app.coshell.R

/**
 * «Настройки» as an index with search (poll 2026-11-28 «А», boards Set1Root, Set1RootSearch): where each place of the settings
 * leads ([SettingsTarget]) and what the search knows ([SETTINGS_ENTRIES]). The screen — `SettingsScreen.kt`; the graph opens a
 * target — `navigation/SettingsGraph.kt`.
 */
enum class SettingsTarget {
    LOOK, INPUT, NOTICES, SCREENS, PRIVACY, HELP, MONEY, DATA,
    CURRENCIES, HOME, BAR, OPS, REPORTS, ACCOUNTS, BALANCE, BACKUP, IMPORT, EXPORT, WIPE, TOUR, GESTURES,
}

/** One setting the search finds: its name as on its screen, the screen it is on ([group]) and where a tap leads. */
data class SettingsEntry(@StringRes val title: Int, @StringRes val group: Int, val target: SettingsTarget)

/** Every setting by its name. A new setting or a renamed one — its line here too (the search would miss it). */
val SETTINGS_ENTRIES: List<SettingsEntry> = listOf(
    SettingsEntry(R.string.theme_title, R.string.appearance_title, SettingsTarget.LOOK),
    SettingsEntry(R.string.settings_palette, R.string.appearance_title, SettingsTarget.LOOK),
    SettingsEntry(R.string.language_title, R.string.appearance_title, SettingsTarget.LOOK),
    SettingsEntry(R.string.density_section, R.string.appearance_title, SettingsTarget.LOOK),
    SettingsEntry(R.string.shake_to_add_title, R.string.settings_input_title, SettingsTarget.INPUT),
    SettingsEntry(R.string.settings_input_haptics, R.string.settings_input_title, SettingsTarget.INPUT),
    SettingsEntry(R.string.settings_input_account_title, R.string.settings_input_title, SettingsTarget.INPUT),
    SettingsEntry(R.string.reminders_title, R.string.settings_notices_title, SettingsTarget.NOTICES),
    SettingsEntry(R.string.settings_notices_time, R.string.settings_notices_title, SettingsTarget.NOTICES),
    SettingsEntry(R.string.notice_background_title, R.string.settings_notices_title, SettingsTarget.NOTICES),
    SettingsEntry(R.string.nav_home, R.string.settings_screens_title, SettingsTarget.HOME),
    SettingsEntry(R.string.settings_balance_title, R.string.settings_screens_title, SettingsTarget.BALANCE),
    SettingsEntry(R.string.settings_bar_title, R.string.settings_screens_title, SettingsTarget.BAR),
    SettingsEntry(R.string.nav_transactions, R.string.settings_screens_title, SettingsTarget.OPS),
    SettingsEntry(R.string.reports_title, R.string.settings_screens_title, SettingsTarget.REPORTS),
    SettingsEntry(R.string.report_cards_search, R.string.settings_screens_title, SettingsTarget.REPORTS),
    SettingsEntry(R.string.nav_accounts, R.string.settings_screens_title, SettingsTarget.ACCOUNTS),
    SettingsEntry(R.string.privacy_pin, R.string.privacy_title, SettingsTarget.PRIVACY),
    SettingsEntry(R.string.privacy_biometric, R.string.privacy_title, SettingsTarget.PRIVACY),
    SettingsEntry(R.string.privacy_hide_launch, R.string.privacy_title, SettingsTarget.PRIVACY),
    SettingsEntry(R.string.privacy_face_down, R.string.privacy_title, SettingsTarget.PRIVACY),
    SettingsEntry(R.string.privacy_screenshots, R.string.privacy_title, SettingsTarget.PRIVACY),
    SettingsEntry(R.string.privacy_recents, R.string.privacy_title, SettingsTarget.PRIVACY),
    SettingsEntry(R.string.privacy_relock, R.string.privacy_title, SettingsTarget.PRIVACY),
    SettingsEntry(R.string.hints_title, R.string.settings_help_title, SettingsTarget.HELP),
    SettingsEntry(R.string.hints_reset_title, R.string.settings_help_title, SettingsTarget.HELP),
    SettingsEntry(R.string.tour_title, R.string.settings_help_title, SettingsTarget.TOUR),
    SettingsEntry(R.string.gestures_title, R.string.settings_help_title, SettingsTarget.GESTURES),
    SettingsEntry(R.string.settings_invite_title, R.string.settings_help_title, SettingsTarget.HELP),
    SettingsEntry(R.string.settings_policy_title, R.string.settings_help_title, SettingsTarget.HELP),
    SettingsEntry(R.string.report_currencies_title, R.string.settings_money_title, SettingsTarget.CURRENCIES),
    SettingsEntry(R.string.settings_money_week, R.string.settings_money_title, SettingsTarget.MONEY),
    SettingsEntry(R.string.backup_title, R.string.settings_data_title, SettingsTarget.BACKUP),
    SettingsEntry(R.string.import_title, R.string.settings_data_title, SettingsTarget.IMPORT),
    SettingsEntry(R.string.export_title, R.string.settings_data_title, SettingsTarget.EXPORT),
    SettingsEntry(R.string.settings_data_wifi, R.string.settings_data_title, SettingsTarget.DATA),
    SettingsEntry(R.string.wipe_title, R.string.settings_data_title, SettingsTarget.WIPE),
)

/** How a query meets a setting's name: each word of the query is found inside the name, case and «ё» aside. */
object SettingsMatch {
    fun normalize(text: String): String = text.lowercase().replace('ё', 'е')

    fun matches(query: String, text: String): Boolean {
        val words = normalize(query).split(' ').filter { it.isNotBlank() }
        if (words.isEmpty()) return false
        val haystack = normalize(text)
        return words.all { it in haystack }
    }

    /** The entries for [query]: names that match first, then those found by their screen's name; each once. */
    fun <T> search(query: String, entries: List<T>, title: (T) -> String, group: (T) -> String): List<T> {
        val byTitle = entries.filter { matches(query, title(it)) }
        val byGroup = entries.filter { it !in byTitle && matches(query, group(it)) }
        return byTitle + byGroup
    }
}
