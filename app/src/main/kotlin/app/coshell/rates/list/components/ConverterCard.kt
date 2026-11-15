package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.rates.list.ConverterUi

/**
 * «Сколько это» on top of «Курсы» (#319, board `Fb319A`): a hero on the theme gradient with the typed amount and
 * its currency; the rows below show it in their currencies. A tap on the card opens the keypad, on «USD ▾» —
 * the currency picker.
 */
@Composable
internal fun ConverterCard(converter: ConverterUi, onAmountClick: () -> Unit, onCurrencyClick: () -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.s), accent = true, onClick = onAmountClick) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(stringResource(R.string.rates_converter_title), style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Text(
                    converter.amountText,
                    style = AmountTitleStyle,
                    color = LocalHeroPalette.current.amount,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    stringResource(R.string.rates_converter_currency, converter.from),
                    style = RateTextStyle,
                    modifier = Modifier
                        .tactileClickable(Tactile.Control, MaterialTheme.shapes.small, role = Role.Button, onClick = onCurrencyClick)
                        .padding(horizontal = Spacing.xs, vertical = Spacing.s),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.rates_converter_today),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            converter.inMain?.let {
                Text(it, style = RateTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
