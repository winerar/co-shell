package app.coshell.ui.top

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.R as DesignR

/**
 * Header of a list in selection mode (mockup 19В): «✕» ends the mode, the title counts the chosen rows.
 * Takes the place of [TabTopBar] while something is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(count: Int, onClear: () -> Unit, scroll: TabScroll) {
    TopAppBar(
        navigationIcon = {
            CoShellIconButton(onClick = onClear) {
                Icon(painterResource(DesignR.drawable.ic_close), contentDescription = stringResource(R.string.transactions_selection_clear))
            }
        },
        title = { Text(stringResource(R.string.transactions_selected, count), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        scrollBehavior = scroll.behavior,
    )
}
