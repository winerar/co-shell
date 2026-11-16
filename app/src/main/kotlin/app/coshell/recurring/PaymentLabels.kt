package app.coshell.recurring

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.theme.AmountCellStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem

/**
 * When a regular payment is due (#143): «пропущен» / «пропущено 2» / «сегодня» as a label, a later day as grey
 * [dateText] — «Скоро» on «Обзоре» and «Регулярные платежи» show it the same way.
 */
@Composable
internal fun PaymentWhen(state: PaymentState, missed: Int, dateText: String) {
    when (state) {
        PaymentState.MISSED -> CoShellStatusChip(
            if (missed > 1) stringResource(R.string.recurring_state_missed_many, missed) else stringResource(R.string.recurring_state_missed),
            StatusTone.Work,
        )
        PaymentState.TODAY -> CoShellStatusChip(stringResource(R.string.recurring_state_today), StatusTone.New)
        PaymentState.SOON -> Text(dateText, style = MaterialTheme.typography.bodySmall)
    }
}

/** «−650 USD», «+1 200 USD» in the income colour, a transfer without a sign, or «при вводе» when asked every time. */
@Composable
internal fun PaymentAmount(item: TemplateItem) {
    if (item.amount.isEmpty()) {
        Text(stringResource(R.string.template_amount_on_entry_short), style = MaterialTheme.typography.bodySmall)
        return
    }
    when (item.type) {
        TransactionType.INCOME -> AmountText("+" + item.amount, style = AmountCellStyle, color = LocalAmountPalette.current.income)
        TransactionType.EXPENSE -> AmountText("−" + item.amount, style = AmountCellStyle)
        TransactionType.TRANSFER -> AmountText(item.amount, style = AmountCellStyle)
    }
}
