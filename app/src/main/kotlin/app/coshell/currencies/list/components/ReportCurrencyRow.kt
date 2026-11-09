package app.coshell.currencies.list.components

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.R as DesignR

/** One report currency: "USD · Основная", or "ARS · Нажмите, чтобы сделать основной" with a bin. */
@Composable
internal fun ReportCurrencyRow(
    code: String,
    isMain: Boolean,
    canRemove: Boolean,
    onMakeMain: () -> Unit,
    onRemove: () -> Unit,
) {
    CoShellListRow(
        title = code,
        subtitle = stringResource(if (isMain) R.string.report_currencies_main else R.string.report_currencies_make_main),
        leading = if (isMain) {
            { Icon(painterResource(DesignR.drawable.ic_check), contentDescription = null) }
        } else {
            null
        },
        trailing = if (canRemove) {
            {
                CoShellIconButton(onClick = onRemove) {
                    Icon(painterResource(DesignR.drawable.ic_delete), stringResource(R.string.action_delete))
                }
            }
        } else {
            null
        },
        onClick = if (isMain) null else onMakeMain,
    )
}
