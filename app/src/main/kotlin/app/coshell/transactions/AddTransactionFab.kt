package app.coshell.transactions

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR

/** The "+" that opens quick transaction entry; shared by the Home and Transactions tabs. */
@Composable
fun AddTransactionFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.transaction_add))
    }
}
