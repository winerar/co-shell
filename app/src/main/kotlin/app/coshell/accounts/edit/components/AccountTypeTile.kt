package app.coshell.accounts.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.labelRes
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.model.AccountType
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ChoiceSheet

/**
 * «Тип» of an account as a tile in «Деньги» (#818, poll «А», board `AccountEdit`): it names the type; a tap opens the list
 * of all seven — five [AccountType]s, «Проездной» (#645) and «Кредитка» (#686), which are stored as flags.
 */
@Composable
internal fun AccountTypeTile(
    selected: AccountType,
    prepaid: Boolean,
    credit: Boolean,
    onSelect: (AccountType) -> Unit,
    onPrepaid: () -> Unit,
    onCredit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val current = when {
        credit -> CREDIT
        prepaid -> PREPAID
        else -> selected.name
    }
    val options = AccountType.entries.map { ChoiceOption(it.name, stringResource(it.labelRes)) } +
        ChoiceOption(PREPAID, stringResource(R.string.account_type_prepaid)) +
        ChoiceOption(CREDIT, stringResource(R.string.account_type_credit))
    CoShellDetailTile(
        icon = painterResource(DesignR.drawable.ic_category),
        label = stringResource(R.string.account_type),
        value = options.first { it.value == current }.label,
        onClick = { open = true },
        modifier = modifier,
    )
    if (open) {
        ChoiceSheet(
            title = stringResource(R.string.account_type),
            options = options,
            selected = current,
            onPick = { key ->
                open = false
                when (key) {
                    PREPAID -> onPrepaid()
                    CREDIT -> onCredit()
                    else -> onSelect(AccountType.valueOf(key))
                }
            },
            onDismiss = { open = false },
        )
    }
}

private const val PREPAID = "prepaid"
private const val CREDIT = "credit"
