package app.coshell.importer.wallet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAmountField
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.importer.wallet.AccountChoice
import app.coshell.core.domain.importer.wallet.WalletAccountInfo
import app.coshell.core.domain.importer.wallet.WalletAccountMatcher
import app.coshell.core.model.Account
import app.coshell.core.model.AmountSign
import app.coshell.importer.wallet.WalletImportDraft

/**
 * Step 3: a card per Wallet account — «Новый счёт» or one of the co-shell accounts in the same currency
 * (merging is only allowed there), and an optional «баланс в Wallet сейчас».
 */
fun LazyListScope.accountsStep(
    draft: WalletImportDraft,
    existing: List<Account>,
    onChoose: (String, AccountChoice) -> Unit,
    onBalance: (String, String) -> Unit,
) {
    item(key = "hint") { CoShellHint(stringResource(R.string.import_accounts_hint), Modifier.padding(horizontal = Spacing.l)) }
    items(draft.accounts, key = { "acc-${it.name}" }) { info ->
        WalletAccountCard(
            info = info,
            choice = draft.accountChoices[info.name],
            targets = existing.filter { WalletAccountMatcher.canMerge(info, it) },
            balance = draft.balanceTexts[info.name].orEmpty(),
            balanceInvalid = draft.balanceInvalid(info.name),
            onChoose = { onChoose(info.name, it) },
            onBalance = { onBalance(info.name, it) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WalletAccountCard(
    info: WalletAccountInfo,
    choice: AccountChoice?,
    targets: List<Account>,
    balance: String,
    balanceInvalid: Boolean,
    onChoose: (AccountChoice) -> Unit,
    onBalance: (String) -> Unit,
) {
    CoShellCard(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.xs)) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(info.name, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.import_account_records, info.records, info.currency.value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                CoShellFilterChip(
                    selected = choice is AccountChoice.CreateNew,
                    onClick = { onChoose(AccountChoice.CreateNew(info.name, info.currency)) },
                    label = { Text(stringResource(R.string.import_account_new)) },
                )
                targets.forEach { account ->
                    CoShellFilterChip(
                        selected = (choice as? AccountChoice.MergeInto)?.accountId == account.id,
                        onClick = { onChoose(AccountChoice.MergeInto(account.id)) },
                        label = { Text(account.name) },
                    )
                }
            }
            CoShellAmountField(
                value = balance,
                onValueChange = onBalance,
                label = stringResource(R.string.import_wallet_balance),
                currency = info.currency.value,
                error = if (balanceInvalid) stringResource(R.string.import_wallet_balance_error) else null,
                hint = stringResource(R.string.import_wallet_balance_hint),
                onToggleSign = { onBalance(AmountSign.toggle(balance)) },
                toggleSignLabel = stringResource(R.string.import_wallet_balance_toggle_sign),
            )
        }
    }
}
