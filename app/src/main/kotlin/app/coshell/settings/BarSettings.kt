package app.coshell.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.BarTab
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.navigation.BarTabs
import app.coshell.navigation.TopLevel
import app.coshell.navigation.topLevel
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.list.SectionTitle

// «Настройки → Экраны → Нижняя панель» (#154, poll 2026-11-27 «А · А», board `Set1Bar`): the second and the third tab of the bar.

@Composable
fun BarSettingsRoute(onBack: () -> Unit, viewModel: ScreenOptionsViewModel = hiltViewModel()) {
    val o by viewModel.options.collectAsStateWithLifecycle()
    BarSettingsScreen(o, { f -> viewModel.change(change = f) }, onBack)
}

/** A live sample of the bar on top, then the two places that can change; a tap opens the six sections to pick from. */
@Composable
fun BarSettingsScreen(options: ScreenOptions, change: ((ScreenOptions) -> ScreenOptions) -> Unit, onBack: () -> Unit) {
    var picking by rememberSaveable { mutableIntStateOf(-1) } // 0 — the second tab, 1 — the third
    val second = options.barSecond.topLevel
    val third = options.barThird.topLevel
    GroupScaffold(R.string.settings_bar_title, onBack) {
        item { BarSample(BarTabs(second, third)) }
        item { SectionTitle(stringResource(R.string.settings_bar_section)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.s)) {
                row { CoShellMenuRow(second.icon, stringResource(R.string.settings_bar_second), { picking = 0 }, subtitle = stringResource(second.label)) }
                row { CoShellMenuRow(third.icon, stringResource(R.string.settings_bar_third), { picking = 1 }, subtitle = stringResource(third.label)) }
            }
        }
        item { CoShellHint(stringResource(R.string.settings_bar_note), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
    }
    if (picking >= 0) {
        val isThird = picking == 1
        ChoiceSheet(
            title = stringResource(if (isThird) R.string.settings_bar_third else R.string.settings_bar_second),
            options = BarTab.entries.map { ChoiceOption(it, stringResource(it.topLevel.label)) },
            selected = if (isThird) options.barThird else options.barSecond,
            onPick = { tab -> change { if (isThird) it.withBarThird(tab) else it.withBarSecond(tab) }; picking = -1 },
            onDismiss = { picking = -1 },
        )
    }
}

/** The bar as it will look, «Обзор» selected; only a picture — taps do nothing. */
@Composable
private fun BarSample(tabs: BarTabs) {
    // The capsule keeps clear of the system navigation bar on its own; here it sits in a list, not at the bottom.
    Box(Modifier.consumeWindowInsets(WindowInsets.navigationBars)) {
        CoShellBottomBar(
            tabs = tabs.all.map { CoShellTab(it.icon, stringResource(it.label), selected = it == TopLevel.HOME) },
            onTab = {},
            addLabel = stringResource(R.string.transaction_add),
            onAdd = {},
        )
    }
}
