package app.coshell.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.ui.list.ActionRow
import app.coshell.core.designsystem.R as DesignR

// Stateless menu tab: links to categories, backup, app update and feedback. No ViewModel needed yet.

@Composable
fun MoreRoute(onCategories: () -> Unit, onBackup: () -> Unit, onUpdate: () -> Unit, onFeedback: () -> Unit) =
    MoreScreen(onCategories, onBackup, onUpdate, onFeedback)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(onCategories: () -> Unit, onBackup: () -> Unit, onUpdate: () -> Unit, onFeedback: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_more)) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_category,
                    title = stringResource(R.string.categories_title),
                    onClick = onCategories,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_settings_backup_restore,
                    title = stringResource(R.string.backup_title),
                    onClick = onBackup,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_deployed_code_update,
                    title = stringResource(R.string.update_title),
                    onClick = onUpdate,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_feedback,
                    title = stringResource(R.string.feedback_title),
                    subtitle = stringResource(R.string.feedback_menu_hint),
                    onClick = onFeedback,
                    opensScreen = true,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MoreScreenPreview() {
    CoShellTheme { MoreScreen(onCategories = {}, onBackup = {}, onUpdate = {}, onFeedback = {}) }
}
