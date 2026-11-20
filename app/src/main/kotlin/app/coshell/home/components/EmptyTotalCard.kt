package app.coshell.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.RollingAmountText
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.theme.AmountHeroStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing

/** «Обзор» before the first account (board `HomeEmpty`): the same hero card with a zero total and «Счетов пока нет». */
@Composable
internal fun EmptyTotalCard(total: String) {
    CoShellCard(modifier = Modifier.fillMaxWidth(), tone = CardTone.Hero) {
        Column(Modifier.padding(Spacing.l)) {
            Text(stringResource(R.string.home_total_balance), style = MaterialTheme.typography.labelLarge)
            RollingAmountText(total, AmountHeroStyle, color = LocalHeroPalette.current.amount)
            CoShellHint(stringResource(R.string.home_total_no_accounts), Modifier.padding(top = Spacing.xs))
        }
    }
}
