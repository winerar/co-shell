package app.coshell.transactions.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellFilterChip

/** A filter chip with a drop-down of choices: shows the chosen value and lights up while a filter is set. */
@Composable
internal fun FilterMenuChip(label: String, selected: Boolean, options: List<Pair<String, () -> Unit>>) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box {
        CoShellFilterChip(
            selected = selected,
            onClick = { open = true },
            label = { Text(label) },
            trailingIcon = { Icon(painterResource(DesignR.drawable.ic_keyboard_arrow_down), contentDescription = null) },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (text, pick) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { open = false; pick() })
            }
        }
    }
}
