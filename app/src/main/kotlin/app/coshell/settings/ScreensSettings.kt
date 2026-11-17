package app.coshell.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ReportsStart
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.R as DesignR

// «Настройки → Экраны» (boards Set1Screens, Set1Ops, Set1Reports, Set1Accounts): what each screen shows and how it opens.
// «Обзор» (cards, horizon, tiles) and «сумма в основной валюте» join with their own steps.

/** «Экраны»: the list of screens that have options. */
@Composable
fun ScreensSettingsScreen(onHome: () -> Unit, onOps: () -> Unit, onReports: () -> Unit, onAccounts: () -> Unit, onBack: () -> Unit) {
    val c = CategoryPalette.colors
    GroupScaffold(R.string.settings_screens_title, onBack) {
        item { CoShellHint(stringResource(R.string.settings_screens_intro), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
        item { SectionTitle(stringResource(R.string.settings_screens_title)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { CoShellMenuRow(DesignR.drawable.ic_home, categoryColor(c[8]), stringResource(R.string.nav_home), onHome, subtitle = stringResource(R.string.settings_home_items)) }
                row { CoShellMenuRow(DesignR.drawable.ic_receipt_long, categoryColor(c[13]), stringResource(R.string.nav_transactions), onOps, subtitle = stringResource(R.string.settings_ops_items)) }
                row { CoShellMenuRow(DesignR.drawable.ic_bar_chart, categoryColor(c[18]), stringResource(R.string.reports_title), onReports, subtitle = stringResource(R.string.settings_reports_items)) }
                row { CoShellMenuRow(DesignR.drawable.ic_account_balance_wallet, categoryColor(c[0]), stringResource(R.string.nav_accounts), onAccounts, subtitle = stringResource(R.string.settings_accounts_items)) }
            }
        }
    }
}

/** «Операции»: what a row of the list says. */
@Composable
fun OpsSettingsRoute(onBack: () -> Unit, viewModel: ScreenOptionsViewModel = hiltViewModel()) {
    val o by viewModel.options.collectAsStateWithLifecycle()
    OpsSettingsScreen(o, { f -> viewModel.change(change = f) }, onBack)
}

@Composable
fun OpsSettingsScreen(options: ScreenOptions, change: ((ScreenOptions) -> ScreenOptions) -> Unit, onBack: () -> Unit) {
    GroupScaffold(R.string.nav_transactions, onBack) {
        item { SectionTitle(stringResource(R.string.settings_ops_in_row)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_ops_account), subtitle = stringResource(R.string.settings_ops_account_hint),
                        checked = options.opsShowAccount, onCheckedChange = { v -> change { it.copy(opsShowAccount = v) } },
                    )
                }
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_ops_tags), subtitle = stringResource(R.string.settings_ops_tags_hint),
                        checked = options.opsShowTags, onCheckedChange = { v -> change { it.copy(opsShowTags = v) } },
                    )
                }
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_ops_note), subtitle = stringResource(R.string.settings_ops_note_hint),
                        checked = options.opsShowNote, onCheckedChange = { v -> change { it.copy(opsShowNote = v) } },
                    )
                }
            }
        }
    }
}

/** «Отчёты»: the period they open on and the comparison. */
@Composable
fun ReportsSettingsRoute(onBack: () -> Unit, viewModel: ScreenOptionsViewModel = hiltViewModel()) {
    val o by viewModel.options.collectAsStateWithLifecycle()
    ReportsSettingsScreen(o, { f -> viewModel.change(change = f) }, onBack)
}

@Composable
fun ReportsSettingsScreen(options: ScreenOptions, change: ((ScreenOptions) -> ScreenOptions) -> Unit, onBack: () -> Unit) {
    val starts = ReportsStart.entries
    GroupScaffold(R.string.reports_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_reports_period)) }
        item {
            PickCard(
                title = stringResource(R.string.settings_reports_open_on),
                labels = listOf(R.string.settings_reports_month, R.string.settings_reports_quarter, R.string.settings_reports_year).map { stringResource(it) },
                selected = starts.indexOf(options.reportsStart),
                onSelect = { i -> change { it.copy(reportsStart = starts[i]) } },
            )
        }
        item { SectionTitle(stringResource(R.string.settings_reports_compare_section)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_reports_compare), subtitle = stringResource(R.string.settings_reports_compare_hint),
                        checked = options.reportsCompare, onCheckedChange = { v -> change { it.copy(reportsCompare = v) } },
                    )
                }
            }
        }
    }
}

/** «Счета»: how the list opens. */
@Composable
fun AccountsSettingsRoute(onBack: () -> Unit, viewModel: ScreenOptionsViewModel = hiltViewModel()) {
    val o by viewModel.options.collectAsStateWithLifecycle()
    AccountsSettingsScreen(o, { f -> viewModel.change(change = f) }, onBack)
}

@Composable
fun AccountsSettingsScreen(options: ScreenOptions, change: ((ScreenOptions) -> ScreenOptions) -> Unit, onBack: () -> Unit) {
    GroupScaffold(R.string.nav_accounts, onBack) {
        item { SectionTitle(stringResource(R.string.settings_accounts_grouping)) }
        item {
            PickCard(
                title = stringResource(R.string.settings_accounts_grouped),
                labels = listOf(stringResource(R.string.settings_accounts_by_type), stringResource(R.string.settings_accounts_by_currency)),
                selected = if (options.accountsByCurrency) 1 else 0,
                onSelect = { i -> change { it.copy(accountsByCurrency = i == 1) } },
            )
        }
        item { SectionTitle(stringResource(R.string.settings_accounts_archive)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_accounts_show_archived), subtitle = stringResource(R.string.settings_accounts_archived_hint),
                        checked = options.accountsShowArchived, onCheckedChange = { v -> change { it.copy(accountsShowArchived = v) } },
                    )
                }
            }
        }
    }
}

/** A setting with a few values: its title over the segments, in a group. */
@Composable
internal fun PickCard(title: String, labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
        row {
            Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.m)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                CoShellSegmented(labels, selected, onSelect, Modifier.fillMaxWidth().padding(top = Spacing.m))
            }
        }
    }
}
