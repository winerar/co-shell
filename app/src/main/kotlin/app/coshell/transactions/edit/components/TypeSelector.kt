package app.coshell.transactions.edit.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSegmentedButton
import app.coshell.core.model.TransactionType

/** Expense / income / transfer switch at the top of the form. */
@Composable
internal fun TypeSelector(selected: TransactionType, onSelect: (TransactionType) -> Unit, modifier: Modifier = Modifier) {
    val types = TransactionType.entries
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        types.forEachIndexed { index, type ->
            CoShellSegmentedButton(
                selected = type == selected,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index, types.size),
                label = { Text(stringResource(type.labelRes)) },
            )
        }
    }
}

@get:StringRes
internal val TransactionType.labelRes: Int
    get() = when (this) {
        TransactionType.EXPENSE -> R.string.transaction_type_expense
        TransactionType.INCOME -> R.string.transaction_type_income
        TransactionType.TRANSFER -> R.string.transaction_type_transfer
    }
