package app.coshell.budgets.list.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.ui.form.ChoiceDialog
import app.coshell.ui.form.ChoiceOption

/**
 * «Настройки бюджета» (#213): the day the budget month starts on, 1..31 (#139). Days past the end of a
 * short month fall on its last day — the calculator's rule (`budgetPeriod`), said in the hint.
 */
@Composable
internal fun MonthStartDialog(current: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val default = stringResource(R.string.budgets_start_default)
    val options = MonthStartSettings.DAYS.map { day ->
        ChoiceOption(day, stringResource(R.string.budgets_start_day, day), default.takeIf { day == MonthStartSettings.DEFAULT })
    }
    ChoiceDialog(
        title = stringResource(R.string.budgets_settings),
        hint = stringResource(R.string.budgets_start_hint, current),
        options = options,
        selected = current,
        onPick = onPick,
        onDismiss = onDismiss,
    )
}
