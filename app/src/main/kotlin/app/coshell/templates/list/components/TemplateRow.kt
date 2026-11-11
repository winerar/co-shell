package app.coshell.templates.list.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem
import app.coshell.templates.components.TemplateBadge

/** A dense row of «Все шаблоны»: name, «Категория · Счёт» (a transfer — «Счёт → Счёт»), amount or «при вводе». */
@Composable
internal fun TemplateRow(item: TemplateItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    CoShellListRow(
        title = item.name,
        subtitle = item.subtitle(),
        subtitleMaxLines = 1,
        leading = { TemplateBadge(item) },
        trailing = {
            if (item.amount.isEmpty()) {
                Text(stringResource(R.string.template_amount_on_entry_short), style = MaterialTheme.typography.bodySmall)
            } else {
                AmountText(item.amount, style = AmountTextStyle)
            }
        },
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

@Composable
private fun TemplateItem.subtitle(): String = when {
    type == TransactionType.TRANSFER -> stringResource(R.string.template_row_transfer, accountName.orChoose(), toAccountName.orChoose())
    categoryName.isEmpty() -> accountName.orChoose()
    else -> stringResource(R.string.template_row_subtitle, categoryName, accountName.orChoose())
}

/** An account that was archived or deleted since: the entry sheet will ask for another. */
@Composable
private fun String.orChoose(): String = ifEmpty { stringResource(R.string.template_account_gone) }

/** «Все · Расходы · 27 · Доходы · 3 · Переводы · 2» — only the types that have templates. */
@Composable
internal fun TypeFilterChips(
    total: Int,
    counts: Map<TransactionType, Int>,
    selected: TransactionType?,
    onSelect: (TransactionType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        CoShellFilterChip(selected == null, { onSelect(null) }, { Text(stringResource(R.string.templates_filter_all, total)) })
        TransactionType.entries.forEach { type ->
            val count = counts[type] ?: return@forEach
            CoShellFilterChip(selected == type, { onSelect(type) }, { Text(stringResource(filterLabel(type), count)) })
        }
    }
}

private fun filterLabel(type: TransactionType): Int = when (type) {
    TransactionType.EXPENSE -> R.string.templates_filter_expense
    TransactionType.INCOME -> R.string.templates_filter_income
    TransactionType.TRANSFER -> R.string.templates_filter_transfer
}
