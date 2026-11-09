package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Section title with an optional muted explanation under it. */
@Composable
internal fun RatesSectionHeader(title: String, hint: String? = null) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
