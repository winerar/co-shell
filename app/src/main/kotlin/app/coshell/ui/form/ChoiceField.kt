package app.coshell.ui.form

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellListRow

/** A form field that opens a list: label on top, the chosen value (or [placeholder]) under it, «›». */
@Composable
fun ChoiceField(label: String, value: String?, placeholder: String, isError: Boolean, onClick: () -> Unit) {
    CoShellListRow(
        title = { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        supporting = {
            Text(
                value ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    value == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        },
        trailing = { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) },
        onClick = onClick,
    )
}
