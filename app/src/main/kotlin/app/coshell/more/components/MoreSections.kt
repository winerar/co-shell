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
import app.coshell.core.designsystem.R as DesignR

/** A group of «Ещё» under its [title]. */
internal data class MoreSection(@StringRes val title: Int, val items: List<MoreItem>)

/** One row: icon on a square of a palette [color] (stored value), title, a line about the section. */
internal data class MoreItem(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    val color: Long,
    val subtitle: String?,
    val onClick: () -> Unit,
)

/**
 * The three groups of board 154А: money (what is counted and how), family and data (who shares it, where it is
 * kept), the app itself. Colours come from the category palette, one per row, as on the old bento tiles.
 */
@Composable
internal fun moreSections(state: MoreState, actions: MoreActions): List<MoreSection> {
    val c = CategoryPalette.colors
    return listOf(
        MoreSection(
            R.string.more_section_money,
            listOf(
                MoreItem(DesignR.drawable.ic_category, R.string.categories_title, c[9], stringResource(R.string.categories_menu_hint), actions.onCategories),
                MoreItem(DesignR.drawable.ic_savings, R.string.budgets_title, c[1], stringResource(R.string.budgets_menu_hint), actions.onBudgets),
                MoreItem(DesignR.drawable.ic_currency_exchange, R.string.rates_title, c[6], stringResource(R.string.rates_menu_hint), actions.onRates),
                MoreItem(
                    DesignR.drawable.ic_payments,
                    R.string.report_currencies_title,
                    c[4],
                    state.mainCurrency?.let { stringResource(R.string.report_currencies_menu_hint, it) },
                    actions.onReportCurrencies,
                ),
            ),
        ),
        MoreSection(
            R.string.more_section_family_data,
            listOf(
                MoreItem(DesignR.drawable.ic_group, R.string.family_title, c[8], stringResource(R.string.family_menu_hint), actions.onFamily),
                MoreItem(DesignR.drawable.ic_settings_backup_restore, R.string.backup_title, c[11], stringResource(R.string.backup_menu_hint), actions.onBackup),
                MoreItem(DesignR.drawable.ic_download, R.string.import_title, c[ImportColors.MENU], stringResource(R.string.import_menu_hint), actions.onImport),
            ),
        ),
        MoreSection(
            R.string.more_section_app,
            listOf(
                MoreItem(DesignR.drawable.ic_contrast, R.string.appearance_title, c[2], state.appearance, actions.onAppearance),
                MoreItem(DesignR.drawable.ic_feedback, R.string.feedback_title, c[18], stringResource(R.string.feedback_menu_hint), actions.onFeedback),
            ),
        ),
    )
}
