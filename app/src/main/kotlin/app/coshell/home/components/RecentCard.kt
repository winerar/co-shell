package app.coshell.home.components

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
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.TransactionsUiState
import app.coshell.transactions.list.components.TransactionRow

/**
 * «Последние операции» on «Обзор»: a header with «Все» (opens the Transactions tab) and the newest rows,
 * the same rows as on that tab; a tap opens the operation.
 */
@Composable
internal fun RecentCard(
    rows: List<TransactionsUiState.Row>,
    onAll: () -> Unit,
    onRowClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.home_recent), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (rows.isNotEmpty()) CoShellTextButton(onClick = onAll) { Text(stringResource(R.string.home_recent_all) + " →") }
        }
        if (rows.isEmpty()) {
            CoShellHint(stringResource(R.string.home_recent_empty), Modifier.padding(top = Spacing.s))
            return@Column
        }
        CoShellCard(Modifier.fillMaxWidth().padding(top = Spacing.s)) {
            rows.forEach { row -> key(row.id) { TransactionRow(row, onRowClick) } }
        }
    }
}
