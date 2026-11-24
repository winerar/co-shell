package app.coshell.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.coshell.core.model.Account
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.WeekStart
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.R as DesignR


/** «Счета в общем балансе»: every live account with a switch — the flag of the account editor, shared by the family. */
@Composable
fun BalanceAccountsRoute(onBack: () -> Unit, viewModel: SettingsAccountsViewModel = hiltViewModel()) {
    val list by viewModel.list.collectAsStateWithLifecycle()
    BalanceAccountsScreen(list, viewModel::setInTotal, onBack)
}

@Composable
fun BalanceAccountsScreen(accounts: List<Account>, onInTotal: (String, Boolean) -> Unit, onBack: () -> Unit) {
    GroupScaffold(R.string.settings_balance_title, onBack) {
        item { CoShellHint(stringResource(R.string.settings_balance_intro), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
        item { SectionTitle(stringResource(R.string.settings_balance_accounts)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                accounts.forEach { account ->
                    row {
                        CoShellSwitchRow(
                            title = account.name,
                            subtitle = account.currency.value,
                            checked = !account.excludedFromTotal,
                            onCheckedChange = { onInTotal(account.id, it) },
                        )
                    }
                }
            }
        }
    }
}


/** «Деньги и даты»: what is shared by the family and the first day of the week. */
@Composable
fun MoneySettingsRoute(onCurrencies: () -> Unit, onBack: () -> Unit, viewModel: ScreenOptionsViewModel = hiltViewModel()) {
    val options by viewModel.options.collectAsStateWithLifecycle()
    MoneySettingsScreen(options.weekStart, { w -> viewModel.change { it.copy(weekStart = w) } }, onCurrencies, onBack)
}

@Composable
fun MoneySettingsScreen(weekStart: WeekStart, onWeekStart: (WeekStart) -> Unit, onCurrencies: () -> Unit, onBack: () -> Unit) {
    val starts = WeekStart.entries
    GroupScaffold(R.string.settings_money_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_money_family)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellMenuRow(
                        DesignR.drawable.ic_payments,
                        stringResource(R.string.report_currencies_title), onCurrencies,
                    )
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_money_phone)) }
        item {
            PickCard(
                title = stringResource(R.string.settings_money_week),
                labels = listOf(R.string.settings_money_week_mon, R.string.settings_money_week_sun, R.string.settings_money_week_system).map { stringResource(it) },
                selected = starts.indexOf(weekStart),
                onSelect = { onWeekStart(starts[it]) },
            )
        }
    }
}
