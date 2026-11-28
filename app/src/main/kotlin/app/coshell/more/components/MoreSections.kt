package app.coshell.more.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.more.MoreActions
import app.coshell.newmark.NewFeatures
import app.coshell.core.designsystem.R as DesignR

/** A grid of «Ещё» under its [title] (poll 2026-11-28 «А»: sections as icons, four in a row). */
internal data class MoreGrid(@StringRes val title: Int, val items: List<MoreItem>)

/**
 * One section: icon (on the second colour's tone, #442 Б) and a short name; [newKey] — its key in `NewFeatures` when the place
 * can wear the «новое» pill (#335); [count] — a number on the icon (drafts waiting, #586).
 */
internal data class MoreItem(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    val onClick: () -> Unit,
    val newKey: String? = null,
    val count: Int? = null,
)

/** The two grids: money first (used most), then rates, family and data. The app's rows — [moreAppRows]. */
internal fun moreGrids(actions: MoreActions, draftCount: Int = 0): List<MoreGrid> = listOf(
    MoreGrid(
        R.string.more_section_money,
        listOf(
            MoreItem(DesignR.drawable.ic_account_balance_wallet, R.string.accounts_title, actions.onAccounts),
            MoreItem(DesignR.drawable.ic_category, R.string.categories_title, actions.onCategories),
            MoreItem(DesignR.drawable.ic_savings, R.string.budgets_title, actions.onBudgets),
            MoreItem(DesignR.drawable.ic_flag, R.string.goals_title, actions.onGoals, NewFeatures.GOALS),
            MoreItem(DesignR.drawable.ic_refresh, R.string.more_tile_recurring, actions.onRecurring, NewFeatures.RECURRING),
            MoreItem(DesignR.drawable.ic_person_add, R.string.debts_title, actions.onDebts, NewFeatures.DEBTS),
            MoreItem(DesignR.drawable.ic_edit_note, R.string.more_tile_notes, actions.onNotes),
            MoreItem(DesignR.drawable.ic_draft, R.string.drafts_title, actions.onDrafts, count = draftCount.takeIf { it > 0 }),
        ),
    ),
    MoreGrid(
        R.string.more_section_rates_family,
        listOf(
            MoreItem(DesignR.drawable.ic_currency_exchange, R.string.more_tile_rates, actions.onRates),
            MoreItem(DesignR.drawable.ic_sync_alt, R.string.converter_title, actions.onConverter),
            MoreItem(DesignR.drawable.ic_group, R.string.family_title, actions.onFamily),
            MoreItem(DesignR.drawable.ic_settings_backup_restore, R.string.more_tile_data, actions.onData),
        ),
    ),
)

/** A row of the «Приложение» group: settings, feedback, about with the version under it. */
internal data class MoreRow(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    val subtitle: String?,
    val onClick: () -> Unit,
    val newKey: String? = null,
    val fresh: Boolean = false,
)

@Composable
internal fun moreAppRows(actions: MoreActions, versionName: String, build: Int, availableVersion: String?): List<MoreRow> = listOf(
    MoreRow(DesignR.drawable.ic_settings, R.string.settings_title, null, actions.onSettings, NewFeatures.SETTINGS),
    MoreRow(DesignR.drawable.ic_feedback, R.string.feedback_title, null, actions.onFeedback),
    MoreRow(
        DesignR.drawable.ic_info, R.string.about_title,
        if (availableVersion != null) stringResource(R.string.more_version_available, availableVersion) else stringResource(R.string.more_version_quiet, versionName, build),
        actions.onOpenUpdate, NewFeatures.ABOUT, fresh = availableVersion != null,
    ),
)
