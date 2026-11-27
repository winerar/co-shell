package app.coshell.reports.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.recurring.fixed.FixedRow
import app.coshell.recurring.fixed.FixedTotal
import app.coshell.recurring.fixed.FixedUi

/**
 * «Постоянные траты» (#771, poll 2026-11-27 «В · А», board `Fixed771Reports`): what the repeating expenses cost in a year, a month
 * and their share of spending, then the [TOP] dearest. «Все N ›» opens «Регулярные» on «За год»; a row — its template.
 */
@Composable
internal fun FixedCard(fixed: FixedUi, onAll: () -> Unit, onRow: (String) -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column {
            Column(Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.s)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.fixed_title),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f).semantics { heading() },
                    )
                    CoShellTextButton(onClick = onAll) { Text(stringResource(R.string.fixed_all, fixed.count)) }
                }
                FixedTotal(fixed)
            }
            fixed.rows.take(TOP).forEachIndexed { index, row ->
                key(row.item.id) {
                    if (index > 0) CoShellDivider()
                    FixedRow(row) { onRow(row.item.id) }
                }
            }
        }
    }
}

/** Rows on the card; the rest are in «Регулярные → За год». */
private const val TOP = 3
