package app.coshell.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.ui.list.ActionRow
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import app.coshell.core.designsystem.R as DesignR

// Menu tab: sections of the app (categories, rates, family, backup, feedback) and «Настройки».
// Device preferences live on the Settings screen (issue #30).

data class MoreActions(
    val onCategories: () -> Unit = {},
    val onRates: () -> Unit = {},
    val onFamily: () -> Unit = {},
    val onBackup: () -> Unit = {},
    val onFeedback: () -> Unit = {},
    val onSettings: () -> Unit = {},
)

@Composable
fun MoreScreen(actions: MoreActions) {
    val scroll = rememberTabScrollBehavior()
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_more), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { Row(DesignR.drawable.ic_category, R.string.categories_title, null, actions.onCategories) }
            item { Row(DesignR.drawable.ic_currency_exchange, R.string.rates_title, R.string.rates_menu_hint, actions.onRates) }
            item { Row(DesignR.drawable.ic_group, R.string.family_title, R.string.family_menu_hint, actions.onFamily) }
            item { Row(DesignR.drawable.ic_settings_backup_restore, R.string.backup_title, null, actions.onBackup) }
            item { Row(DesignR.drawable.ic_feedback, R.string.feedback_title, R.string.feedback_menu_hint, actions.onFeedback) }
            item { Row(DesignR.drawable.ic_settings, R.string.settings_title, R.string.settings_menu_hint, actions.onSettings) }
        }
    }
}

@Composable
private fun Row(icon: Int, title: Int, subtitle: Int?, onClick: () -> Unit) {
    ActionRow(
        icon = icon,
        title = stringResource(title),
        subtitle = subtitle?.let { stringResource(it) },
        onClick = onClick,
        opensScreen = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun MoreScreenPreview() {
    CoShellTheme { MoreScreen(MoreActions()) }
}
