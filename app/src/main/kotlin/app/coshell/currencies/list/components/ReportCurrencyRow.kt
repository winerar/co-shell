package app.coshell.currencies.list.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
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
    ListItem(
        headlineContent = { Text(code) },
        supportingContent = {
            Text(stringResource(if (isMain) R.string.report_currencies_main else R.string.report_currencies_make_main))
        },
        leadingContent = if (isMain) {
            { Icon(painterResource(DesignR.drawable.ic_check), contentDescription = null) }
        } else {
            null
        },
        trailingContent = if (canRemove) {
            {
                IconButton(onClick = onRemove) {
                    Icon(painterResource(DesignR.drawable.ic_delete), stringResource(R.string.action_delete))
                }
            }
        } else {
            null
        },
        modifier = if (isMain) Modifier else Modifier.clickable(onClick = onMakeMain),
    )
}
