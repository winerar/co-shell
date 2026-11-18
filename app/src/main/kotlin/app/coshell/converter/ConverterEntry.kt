package app.coshell.converter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.R as DesignR

/**
 * The way into the converter from a screen (boards `Conv1Home`, `Conv1Rates`): one menu row in a card. [hint] — the grey line:
 * what it does on «Обзор» («Сколько это в других валютах»), «Нажмите, чтобы посчитать» on «Курсах».
 */
@Composable
fun ConverterEntry(onClick: () -> Unit, modifier: Modifier = Modifier, hint: String = stringResource(R.string.converter_hint)) {
    CoShellCard(modifier) {
        CoShellMenuRow(DesignR.drawable.ic_sync_alt, categoryColor(CategoryPalette.colors[4]), stringResource(R.string.converter_title), onClick, subtitle = hint)
    }
}
