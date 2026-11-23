package app.coshell.more.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.importer.wallet.components.ImportColors
import app.coshell.more.MoreActions
import app.coshell.more.MoreState
import app.coshell.newmark.NewFeatures
import app.coshell.core.designsystem.R as DesignR

/** A group of «Ещё» under its [title]. */
internal data class MoreSection(@StringRes val title: Int, val items: List<MoreItem>)

/**
 * One row: icon on a square of a palette [color] (stored value), title, a line about the section; [newKey] — its key in
 * `NewFeatures` when the place can wear the «новое» pill (#335).
 */
internal data class MoreItem(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    val color: Long,
    val subtitle: String?,
    val onClick: () -> Unit,
    val newKey: String? = null,
    /** A count after the title («Черновики · 5»), `null` — none. */
    val count: Int? = null,
    /** The «новое» pill without a stored key (an update is out). */
    val fresh: Boolean = false,
)

/**
 * The four groups of «Ещё» (poll 2026-11-20, Б; boards MoreB, MoreBEnd): money, currencies, family and data (who shares
 * it, where it is kept, files in and out), the app itself. Colours come from the category palette, one per row, as on the old bento tiles.
 */
@Composable
internal fun moreSections(actions: MoreActions, draftCount: Int = 0, versionName: String = "", build: Int = 0, availableVersion: String? = null): List<MoreSection> {
    val c = CategoryPalette.colors
    return listOf(
        MoreSection(
            R.string.more_section_money,
            listOf(
                MoreItem(DesignR.drawable.ic_account_balance_wallet, R.string.accounts_title, c[0], stringResource(R.string.accounts_menu_hint), actions.onAccounts),
                MoreItem(DesignR.drawable.ic_category, R.string.categories_title, c[8], stringResource(R.string.categories_menu_hint), actions.onCategories),
                MoreItem(DesignR.drawable.ic_savings, R.string.budgets_title, c[20], stringResource(R.string.budgets_menu_hint), actions.onBudgets),
                MoreItem(DesignR.drawable.ic_refresh, R.string.recurring_list_title, c[14], stringResource(R.string.recurring_menu_hint), actions.onRecurring, NewFeatures.RECURRING),
                MoreItem(DesignR.drawable.ic_edit_note, R.string.notes_title, c[16], stringResource(R.string.notes_menu_hint), actions.onNotes),
                MoreItem(DesignR.drawable.ic_draft, R.string.drafts_title, c[13], stringResource(R.string.drafts_menu_hint), actions.onDrafts, count = draftCount.takeIf { it > 0 }),
            ),
        ),
        MoreSection(
            R.string.more_section_rates,
            listOf(
                MoreItem(DesignR.drawable.ic_currency_exchange, R.string.rates_title, c[13], stringResource(R.string.rates_menu_hint), actions.onRates),
                MoreItem(DesignR.drawable.ic_sync_alt, R.string.converter_title, c[4], stringResource(R.string.converter_hint), actions.onConverter),
            ),
        ),
        MoreSection(
            R.string.more_section_family_data,
            listOf(
                MoreItem(DesignR.drawable.ic_group, R.string.family_title, c[10], stringResource(R.string.family_menu_hint), actions.onFamily),
                MoreItem(DesignR.drawable.ic_settings_backup_restore, R.string.backup_title, c[5], stringResource(R.string.backup_menu_hint), actions.onBackup),
                MoreItem(DesignR.drawable.ic_download, R.string.import_title, c[ImportColors.MENU], stringResource(R.string.import_menu_hint), actions.onImport),
                MoreItem(DesignR.drawable.ic_upload, R.string.export_title, c[18], stringResource(R.string.more_export_hint), actions.onExport),
            ),
        ),
        MoreSection(
            R.string.more_section_app,
            listOf(
                MoreItem(DesignR.drawable.ic_settings, R.string.settings_title, c[16], stringResource(R.string.settings_menu_hint), actions.onSettings),
                MoreItem(DesignR.drawable.ic_shield_lock, R.string.privacy_title, c[12], stringResource(R.string.privacy_menu_hint), actions.onPrivacy),
                MoreItem(DesignR.drawable.ic_feedback, R.string.feedback_title, c[1], stringResource(R.string.feedback_menu_hint), actions.onFeedback),
                MoreItem(
                    DesignR.drawable.ic_info, R.string.about_title, c[3],
                    if (availableVersion != null) stringResource(R.string.more_version_available, availableVersion) else stringResource(R.string.more_version_quiet, versionName, build),
                    actions.onOpenUpdate, fresh = availableVersion != null,
                ),
            ),
        ),
    )
}
