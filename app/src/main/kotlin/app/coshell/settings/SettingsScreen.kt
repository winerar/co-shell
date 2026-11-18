package app.coshell.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** Where the rows of «Настройки» lead. */
data class SettingsActions(
    val onBack: () -> Unit = {},
    val onLook: () -> Unit = {},
    val onScreens: () -> Unit = {},
    val onMoney: () -> Unit = {},
    val onInput: () -> Unit = {},
    val onNotices: () -> Unit = {},
    val onData: () -> Unit = {},
)

/**
 * «Ещё → Настройки» (board Set1Root): the groups of settings, each with the line of its main items — no current values
 * (owner, 2026-11-17). «Экраны» joins when its settings exist (`docs/design/settings-customization.md`).
 */
@Composable
fun SettingsScreen(actions: SettingsActions) {
    val c = CategoryPalette.colors
    Scaffold(topBar = { ScreenTopBar(stringResource(R.string.settings_title), onBack = actions.onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = underBars(padding)) {
            item { CoShellHint(stringResource(R.string.settings_intro), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
            item { SectionTitle(stringResource(R.string.settings_title)) }
            item {
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                    group(DesignR.drawable.ic_contrast, R.string.appearance_title, c[18], R.string.settings_look_items, actions.onLook)
                    group(DesignR.drawable.ic_home, R.string.settings_screens_title, c[8], R.string.settings_screens_items, actions.onScreens)
                    group(DesignR.drawable.ic_payments, R.string.settings_money_title, c[17], R.string.settings_money_items, actions.onMoney)
                    group(DesignR.drawable.ic_add, R.string.settings_input_title, c[8], R.string.settings_input_items, actions.onInput)
                    group(DesignR.drawable.ic_refresh, R.string.settings_notices_title, c[14], R.string.settings_notices_items, actions.onNotices)
                    group(DesignR.drawable.ic_archive, R.string.settings_data_title, c[5], R.string.settings_data_items, actions.onData)
                }
            }
        }
    }
}

private fun app.coshell.core.designsystem.components.RowGroupScope.group(icon: Int, title: Int, color: Long, items: Int, onClick: () -> Unit) {
    row {
        CoShellMenuRow(icon, categoryColor(color), stringResource(title), onClick, subtitle = stringResource(items))
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CoShellTheme { SettingsScreen(SettingsActions()) }
}
