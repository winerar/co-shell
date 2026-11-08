package app.coshell.ui.form

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR

/** Top bar of an edit screen: back arrow, title and "Save". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTopBar(title: String, onBack: () -> Unit, onSave: () -> Unit, saveEnabled: Boolean = true) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
            }
        },
        actions = {
            TextButton(onClick = onSave, enabled = saveEnabled) {
                Text(stringResource(R.string.action_save))
            }
        },
    )
}
