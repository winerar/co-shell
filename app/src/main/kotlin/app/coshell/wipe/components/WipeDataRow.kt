package app.coshell.wipe.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.R as DesignR

/** «Стереть все данные» at the bottom of «Резервная копия» (#155): the one red line; it opens the confirmation, nothing is deleted here. */
@Composable
fun WipeDataRow(onClick: () -> Unit) {
    val danger = MaterialTheme.colorScheme.error
    CoShellListRow(
        title = { Text(stringResource(R.string.wipe_title), color = danger, fontWeight = FontWeight.SemiBold) },
        supporting = { Text(stringResource(R.string.wipe_menu_hint)) },
        leading = { Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = null, tint = danger) },
        trailing = { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) },
        onClick = onClick,
    )
}
