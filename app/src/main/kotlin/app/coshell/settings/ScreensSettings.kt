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
import app.coshell.newmark.newMarkPill
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ReportsStart
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.ui.list.SectionTitle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import app.coshell.core.designsystem.components.CoShellReorderColumn
import app.coshell.core.designsystem.components.rememberReorderState
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.domain.settings.ReportCard
import app.coshell.core.domain.settings.ReportLayout
import app.coshell.reports.edit.ReportCardRows
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

// «Настройки → Экраны» (boards Set1Screens, Set1Ops, Set1Reports, Set1Accounts): what each screen shows and how it opens.
// «Нижняя панель» — BarSettings.kt (#154). «Обзор» (cards, horizon, tiles) and «сумма в основной валюте» join with their own steps.

/** «Экраны»: the list of screens that have options. */
@Composable
fun ScreensSettingsScreen(
    onHome: () -> Unit,
    onBar: () -> Unit,
    onOps: () -> Unit,
    onReports: () -> Unit,
    onAccounts: () -> Unit,
    onBack: () -> Unit,
    /** «Нижняя панель» wears «новое» (#154; the mark leads here from «Ещё → Настройки»). */
    barNew: Boolean = false,
) {
    val c = CategoryPalette.colors
    GroupScaffold(R.string.settings_screens_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_screens_title)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { CoShellMenuRow(DesignR.drawable.ic_home, stringResource(R.string.nav_home), onHome, subtitle = stringResource(R.string.settings_home_items)) }
                row { CoShellMenuRow(DesignR.drawable.ic_more_horiz, stringResource(R.string.settings_bar), onBar, subtitle = stringResource(R.string.settings_bar_items), mark = newMarkPill(barNew)) }
                row { CoShellMenuRow(DesignR.drawable.ic_receipt_long, stringResource(R.string.nav_transactions), onOps, subtitle = stringResource(R.string.settings_ops_items)) }
                row { CoShellMenuRow(DesignR.drawable.ic_bar_chart, stringResource(R.string.reports_title), onReports, subtitle = stringResource(R.string.settings_reports_items)) }
                row { CoShellMenuRow(DesignR.drawable.ic_account_balance_wallet, stringResource(R.string.nav_accounts), onAccounts, subtitle = stringResource(R.string.settings_accounts_items)) }
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
        item { SectionTitle(stringResource(R.string.settings_ops_amount_section)) }
        item {
            PickCard(
                title = stringResource(R.string.settings_ops_amount_title),
                labels = listOf(stringResource(R.string.settings_ops_amount_account), stringResource(R.string.settings_ops_amount_main)),
                selected = if (options.opsAmountInMain) 1 else 0,
                onSelect = { i -> change { it.copy(opsAmountInMain = i == 1) } },
            )
        }
        item { CoShellHint(stringResource(R.string.settings_ops_amount_hint), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
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
    val layout = options.reports
    val reorder = rememberReorderState()
    // One scrolling column, not GroupScaffold: the cards are dragged right here (#826), and a drag list can't live in a LazyColumn.
    Scaffold(topBar = { ScreenTopBar(stringResource(R.string.reports_title), onBack = onBack) }) { padding ->
        CoShellReorderColumn(
            state = reorder,
            keys = layout.visible,
            onDrop = { drop -> (drop.key as? ReportCard)?.let { card -> change { it.copy(reports = it.reports.moveShown(card, drop.index)) } } },
            modifier = Modifier.fillMaxSize(),
            contentPadding = underBars(padding),
        ) {
            SectionTitle(stringResource(R.string.settings_reports_period))
            PickCard(
                title = stringResource(R.string.settings_reports_open_on),
                labels = listOf(R.string.settings_reports_month, R.string.settings_reports_quarter, R.string.settings_reports_year).map { stringResource(it) },
                selected = starts.indexOf(options.reportsStart),
                onSelect = { i -> change { it.copy(reportsStart = starts[i]) } },
            )
            SectionTitle(stringResource(R.string.settings_reports_compare_section))
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_reports_compare), subtitle = stringResource(R.string.settings_reports_compare_hint),
                        checked = options.reportsCompare, onCheckedChange = { v -> change { it.copy(reportsCompare = v) } },
                    )
                }
            }
            SectionTitle(stringResource(R.string.report_cards_section))
            CoShellHint(stringResource(R.string.report_cards_help), Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.s))
            ReportCardRows(layout, reorder, onChange = { l -> change { it.copy(reports = l) } }, rowModifier = Modifier.padding(horizontal = Spacing.l))
            ResetLink(enabled = layout != ReportLayout()) { change { it.copy(reports = ReportLayout()) } }
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
