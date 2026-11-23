package app.coshell.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.ChoiceOption
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.R as DesignR

/** «Ввод операции»: shake, the account the «+» sheet opens on, vibration. */
@Composable
fun InputSettingsRoute(
    onBack: () -> Unit,
    viewModel: ShakeSettingsViewModel = hiltViewModel(),
    screens: ScreenOptionsViewModel = hiltViewModel(),
    accounts: SettingsAccountsViewModel = hiltViewModel(),
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val options by screens.options.collectAsStateWithLifecycle()
    val list by accounts.list.collectAsStateWithLifecycle()
    InputSettingsScreen(enabled, viewModel::setEnabled, options, { f -> screens.change(change = f) }, list.map { AccountChoice(it.id, it.name, it.currency.value) }, onBack)
}

/** An account to pick in the settings: its currency goes with it. */
data class AccountChoice(val id: String, val name: String, val currency: String)

@Composable
fun InputSettingsScreen(
    shakeEnabled: Boolean,
    onShakeChange: (Boolean) -> Unit,
    options: ScreenOptions,
    change: ((ScreenOptions) -> ScreenOptions) -> Unit,
    accounts: List<AccountChoice>,
    onBack: () -> Unit,
) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val chosen = accounts.firstOrNull { it.id == options.entryAccountId }
    val always = options.entryAccountId != null
    GroupScaffold(R.string.settings_input_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_input_quick)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        leading = { MenuIcon(DesignR.drawable.ic_mobile_vibrate, categoryColor(CategoryPalette.colors[8])) },
                        title = stringResource(R.string.shake_to_add_title),
                        subtitle = stringResource(R.string.shake_to_add_hint),
                        checked = shakeEnabled,
                        onCheckedChange = onShakeChange,
                    )
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_input_sheet)) }
        item {
            PickCard(
                title = stringResource(R.string.settings_input_account_title),
                labels = listOf(stringResource(R.string.settings_input_last), stringResource(R.string.settings_input_always)),
                selected = if (always) 1 else 0,
                onSelect = { i ->
                    if (i == 0) change { it.copy(entryAccountId = null) }
                    else if (accounts.isNotEmpty()) {
                        // «Всегда этот»: the first account until the person picks another.
                        change { it.copy(entryAccountId = it.entryAccountId ?: accounts.first().id) }
                        picking = true
                    }
                },
            )
        }
        if (always) {
            item {
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.s)) {
                    row {
                        CoShellMenuRow(
                            DesignR.drawable.ic_account_balance_wallet, categoryColor(CategoryPalette.colors[0]),
                            stringResource(R.string.settings_input_account),
                            { picking = true },
                            subtitle = chosen?.let { "${it.name} · ${it.currency}" } ?: stringResource(R.string.settings_input_account_gone),
                        )
                    }
                }
            }
            item { CoShellHint(stringResource(R.string.settings_input_account_hint), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
        }
        item { SectionTitle(stringResource(R.string.settings_input_feedback)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_input_haptics),
                        subtitle = stringResource(R.string.settings_input_haptics_hint),
                        checked = options.haptics,
                        onCheckedChange = { v -> change { it.copy(haptics = v) } },
                    )
                }
            }
        }
    }
    if (picking) {
        ChoiceSheet(
            title = stringResource(R.string.settings_input_account),
            options = accounts.map { ChoiceOption(it.id, it.name, it.currency) },
            selected = options.entryAccountId,
            onPick = { id -> change { it.copy(entryAccountId = id) }; picking = false },
            onDismiss = { picking = false },
        )
    }
}
