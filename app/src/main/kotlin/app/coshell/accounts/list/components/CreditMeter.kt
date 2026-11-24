package app.coshell.accounts.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.accounts.list.AccountsUiState
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Spacing

/** Under a credit card in «Счета» (#686, board `AccountsCredit`): the share of the limit spent and «Доступно … из …». */
@Composable
internal fun CreditMeter(credit: AccountsUiState.Row.Credit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoShellMeter(credit.used, progressColors().indicator, Modifier.fillMaxWidth())
        Text(
            stringResource(R.string.account_credit_available, credit.available, credit.limit),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
