package app.coshell.transactions.edit.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.model.TransactionType

/** Expense / income / transfer switch at the top of the form. */
@Composable
internal fun TypeSelector(selected: TransactionType, onSelect: (TransactionType) -> Unit, modifier: Modifier = Modifier) {
    val types = TransactionType.entries
    CoShellSegmented(
        labels = types.map { stringResource(it.labelRes) },
        selected = types.indexOf(selected),
        onSelect = { onSelect(types[it]) },
        modifier = modifier.fillMaxWidth(),
        // Too narrow for the words (paste, QR and templates share the row, #321) — the icons say it.
        icons = types.map { it.iconRes },
    )
}

@get:DrawableRes
internal val TransactionType.iconRes: Int
    get() = when (this) {
        TransactionType.EXPENSE -> DesignR.drawable.ic_call_made
        TransactionType.INCOME -> DesignR.drawable.ic_call_received
        TransactionType.TRANSFER -> DesignR.drawable.ic_swap_horiz
    }

@get:StringRes
internal val TransactionType.labelRes: Int
    get() = when (this) {
        TransactionType.EXPENSE -> R.string.transaction_type_expense
        TransactionType.INCOME -> R.string.transaction_type_income
        TransactionType.TRANSFER -> R.string.transaction_type_transfer
    }
