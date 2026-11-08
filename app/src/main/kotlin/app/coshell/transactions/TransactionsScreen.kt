package app.coshell.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme

// Placeholder tab until the transaction list (stage 1, step 4) lands.

@Composable
fun TransactionsRoute() = TransactionsScreen()

@Composable
fun TransactionsScreen() {
    Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.transactions_coming_soon),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenPreview() {
    CoShellTheme { TransactionsScreen() }
}
